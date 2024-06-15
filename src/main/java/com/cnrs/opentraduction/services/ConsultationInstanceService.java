package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.clients.OpenthesoClient;
import com.cnrs.opentraduction.entities.ConsultationInstances;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.models.CollectionElementModel;
import com.cnrs.opentraduction.models.InstanceModel;
import com.cnrs.opentraduction.models.ThesaurusElementModel;
import com.cnrs.opentraduction.repositories.ConsultationInstanceRepository;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@Service
@AllArgsConstructor
public class ConsultationInstanceService {

    private final ConsultationInstanceRepository consultationInstanceRepository;
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
        consultationInstanceRepository.deleteById(idInstance);
    }

    public void saveInstance(ConsultationInstances instanceSelected, Thesaurus thesaurus) {

        if (StringUtils.isEmpty(instanceSelected.getName())) {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "Le nom de l'instance est obligatoire !");
            return;
        }

        if (ObjectUtils.isEmpty(instanceSelected.getCreated())) {
            log.info("Enregistrement d'une nouvelle instance dans la base");
            instanceSelected.setCreated(LocalDateTime.now());
        } else if (!CollectionUtils.isEmpty(instanceSelected.getThesauruses())) {
            log.info("Mise à jour d'une instance dans la base");
            log.info("Suppression de l'ancien thésaurus");
            thesaurusRepository.deleteAll(instanceSelected.getThesauruses());
        }

        instanceSelected.setModified(LocalDateTime.now());

        thesaurus.setCreated(LocalDateTime.now());
        thesaurus.setModified(LocalDateTime.now());
        thesaurus.setConsultationInstances(instanceSelected);
        instanceSelected.setThesauruses(Set.of(thesaurus));

        log.info("Enregistrement dans la base");
        var instanceSaved = consultationInstanceRepository.save(instanceSelected);

        thesaurus.setConsultationInstances(instanceSaved);
        thesaurusRepository.save(thesaurus);
    }

    public List<InstanceModel> getAllInstances() {
        var consultationInstances = consultationInstanceRepository.findAll();

        if(!CollectionUtils.isEmpty(consultationInstances)) {
            return consultationInstances.stream()
                    .map(element -> {
                        var instance = new InstanceModel();
                        instance.setId(element.getId());
                        instance.setName(element.getName());
                        instance.setUrl(element.getUrl());
                        if (!CollectionUtils.isEmpty(element.getThesauruses())) {
                            var thesaurus = element.getThesauruses().stream().findFirst();
                            instance.setThesaurusId(thesaurus.get().getIdThesaurus());
                            instance.setThesaurusName(thesaurus.get().getName());
                            instance.setCollectionId(thesaurus.get().getIdCollection());
                            instance.setCollectionName(thesaurus.get().getCollection());
                        }
                        return instance;
                    })
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    public ConsultationInstances getInstanceById(Integer instanceId) {

        var instance = consultationInstanceRepository.findById(instanceId);
        return instance.orElse(null);
    }
}
