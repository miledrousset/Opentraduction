package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.clients.OpenthesoClient;
import com.cnrs.opentraduction.entities.ReferenceInstances;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.models.CollectionElementModel;
import com.cnrs.opentraduction.models.InstanceModel;
import com.cnrs.opentraduction.models.ThesaurusElementModel;
import com.cnrs.opentraduction.repositories.ReferenceInstanceRepository;
import com.cnrs.opentraduction.repositories.ThesaurusRepository;
import com.cnrs.opentraduction.utils.MessageUtil;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.faces.application.FacesMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@Service
@AllArgsConstructor
public class ReferenceInstanceService {

    private final ReferenceInstanceRepository referenceInstanceRepository;
    private final ThesaurusRepository thesaurusRepository;

    private final OpenthesoClient openthesoClient;


    public List<ThesaurusElementModel> searchThesaurus(String baseUrl) {
        var thesaurusResponse = openthesoClient.getThesoInfo(baseUrl);
        if (thesaurusResponse.length > 0) {
            return Stream.of(thesaurusResponse)
                    .filter(element -> element.getLabels().stream().anyMatch(tmp -> "fr".equals(tmp.getLang())))
                    .map(element -> {
                        var label = element.getLabels().stream()
                                .filter(tmp -> "fr".equals(tmp.getLang()))
                                .findFirst().orElse(null);
                        return new ThesaurusElementModel(element.getIdTheso(),
                                ObjectUtils.isEmpty(label) ? "" : label.getTitle());
                    })
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    public List<CollectionElementModel> searchCollections(String baseUrl, String idThesaurus) {
        var collectionsResponse = openthesoClient.getCollectionsByThesaurus(baseUrl, idThesaurus);
        if (collectionsResponse.length > 0) {
            return Stream.of(collectionsResponse)
                    .filter(element -> element.getLabels().stream().anyMatch(tmp -> "fr".equals(tmp.getLang())))
                    .map(element -> {
                        var label = element.getLabels().stream()
                                .filter(tmp -> "fr".equals(tmp.getLang()))
                                .findFirst().orElse(null);

                        return new CollectionElementModel(element.getIdGroup(),
                                ObjectUtils.isEmpty(label) ? "" : label.getTitle());
                    })
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    public void deleteInstance(Integer idInstance) {
        referenceInstanceRepository.deleteById(idInstance);
    }

    public void saveInstance(ReferenceInstances referenceInstances, Thesaurus thesaurus) {

        if (StringUtils.isEmpty(referenceInstances.getName())) {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "Le nom de l'instance est obligatoire !");
            return;
        }

        if (ObjectUtils.isEmpty(referenceInstances.getCreated())) {
            log.info("Enregistrement d'une nouvelle instance dans la base");
            referenceInstances.setCreated(LocalDateTime.now());
        } else if (!ObjectUtils.isEmpty(referenceInstances.getThesaurus())) {
            log.info("Mise à jour d'une instance dans la base");
            log.info("Suppression de l'ancien thésaurus");
            thesaurusRepository.delete(referenceInstances.getThesaurus());
        }

        referenceInstances.setModified(LocalDateTime.now());

        thesaurus.setCreated(LocalDateTime.now());
        thesaurus.setModified(LocalDateTime.now());
        thesaurus.setReferenceInstances(referenceInstances);
        referenceInstances.setThesaurus(thesaurus);

        log.info("Enregistrement dans la base");
        var instanceSaved = referenceInstanceRepository.save(referenceInstances);

        thesaurus.setReferenceInstances(instanceSaved);
        thesaurusRepository.save(thesaurus);
    }

    public List<InstanceModel> getAllInstances() {
        var consultationInstances = referenceInstanceRepository.findAll();

        if(!CollectionUtils.isEmpty(consultationInstances)) {
            return consultationInstances.stream()
                    .map(element -> {
                        var instance = new InstanceModel();
                        instance.setId(element.getId());
                        instance.setName(element.getName());
                        instance.setUrl(element.getUrl());
                        if (!ObjectUtils.isEmpty(element.getThesaurus())) {
                            instance.setThesaurusId(element.getThesaurus().getIdThesaurus());
                            instance.setThesaurusName(element.getThesaurus().getName());
                            instance.setCollectionId(element.getThesaurus().getIdCollection());
                            instance.setCollectionName(element.getThesaurus().getCollection());
                        }
                        return instance;
                    })
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    public ReferenceInstances getInstanceById(Integer instanceId) {

        var instance = referenceInstanceRepository.findById(instanceId);
        return instance.orElse(null);
    }
}
