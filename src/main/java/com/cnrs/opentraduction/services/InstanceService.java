package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.clients.OpenthesoClient;
import com.cnrs.opentraduction.entities.Instances;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.models.CollectionElementModel;
import com.cnrs.opentraduction.models.InstanceModel;
import com.cnrs.opentraduction.models.ThesaurusElementModel;
import com.cnrs.opentraduction.repositories.InstanceRepository;
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
public class InstanceService {

    private final InstanceRepository instanceRepository;
    private final ThesaurusRepository thesaurusRepository;

    private final OpenthesoClient openthesoClient;


    public List<ThesaurusElementModel> searchThesaurus(String baseUrl) {
        var thesaurusResponse = openthesoClient.getThesoInfo(baseUrl);
        if (thesaurusResponse.length > 0) {
            return List.of(thesaurusResponse).stream()
                    .filter(element ->  element.getLabels().stream().filter(tmp -> "fr".equals(tmp.getLang())).findAny().isPresent())
                    .map(element -> new ThesaurusElementModel(element.getIdTheso(),
                            element.getLabels().stream().filter(tmp -> "fr".equals(tmp.getLang())).findFirst().get().getTitle()))
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    public List<CollectionElementModel> searchCollections(String baseUrl, String idThesaurus) {
        var collectionsResponse = openthesoClient.getCollectionsByThesaurus(baseUrl, idThesaurus);
        if (collectionsResponse.length > 0) {
            return Stream.of(collectionsResponse)
                    .filter(element ->  element.getLabels().stream().filter(tmp -> "fr".equals(tmp.getLang())).findAny().isPresent())
                    .map(element -> new CollectionElementModel(element.getIdGroup(),
                            element.getLabels().stream().filter(tmp -> "fr".equals(tmp.getLang())).findFirst().get().getTitle()))
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    public void deleteInstance(Integer idInstance) {
        instanceRepository.deleteById(idInstance);
    }

    public void saveInstance(Instances instanceSelected, Thesaurus thesaurus) {

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
        thesaurus.setInstance(instanceSelected);
        instanceSelected.setThesauruses(Set.of(thesaurus));

        log.info("Enregistrement dans la base");
        var instanceSaved = instanceRepository.save(instanceSelected);

        thesaurus.setInstance(instanceSaved);
        thesaurusRepository.save(thesaurus);
    }

    public List<InstanceModel> getAllInstances() {
        var instances = instanceRepository.findAll();

        if(!CollectionUtils.isEmpty(instances)) {
            return instances.stream()
                    .map(element -> {
                        var instance = new InstanceModel();
                        instance.setId(element.getId());
                        instance.setName(element.getName());
                        instance.setUrl(element.getUrl());
                        if (!CollectionUtils.isEmpty(element.getThesauruses())) {
                            var thesaurus = element.getThesauruses().stream().findFirst();
                            if (thesaurus.isPresent()) {
                                instance.setThesaurusId(thesaurus.get().getIdThesaurus());
                                instance.setThesaurusName(thesaurus.get().getName());

                                instance.setCollectionId(thesaurus.get().getIdCollection());
                                instance.setCollectionName(thesaurus.get().getCollection());
                            }
                        }
                        return instance;
                    })
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    public Instances getInstanceFromId(Integer instanceId) {

        var instance = instanceRepository.findById(instanceId);
        return instance.isPresent() ? instance.get() : null;
    }
}
