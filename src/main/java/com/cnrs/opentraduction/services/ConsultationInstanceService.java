package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.clients.OpenthesoClient;
import com.cnrs.opentraduction.entities.ConsultationInstances;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.models.InstanceModel;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Slf4j
@Service
@AllArgsConstructor
public class ConsultationInstanceService {

    private final ConsultationInstanceRepository consultationInstanceRepository;
    private final ThesaurusRepository thesaurusRepository;

    private final OpenthesoClient openthesoClient;

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

    public List<InstanceModel> getAllConsultationInstances() {
        var consultationInstances = consultationInstanceRepository.findAll();

        if(!CollectionUtils.isEmpty(consultationInstances)) {

            List<InstanceModel> consultationInstancesList = new ArrayList<>();

            consultationInstances.forEach(instance -> {
                if (!CollectionUtils.isEmpty(instance.getThesauruses())) {
                    instance.getThesauruses().forEach(thesaurus -> {
                        var instanceModel = new InstanceModel();
                        instanceModel.setId(instance.getId());
                        instanceModel.setName(instance.getName());
                        instanceModel.setUrl(instance.getUrl());

                        instanceModel.setThesaurusId(thesaurus.getIdThesaurus());
                        instanceModel.setThesaurusName(thesaurus.getName());
                        instanceModel.setCollectionId(thesaurus.getIdCollection());
                        instanceModel.setCollectionName(thesaurus.getCollection());

                        consultationInstancesList.add(instanceModel);
                    });
                }
            });

            return consultationInstancesList;
        } else {
            return List.of();
        }
    }

    public ConsultationInstances getInstanceById(Integer instanceId) {

        var instance = consultationInstanceRepository.findById(instanceId);
        return instance.orElse(null);
    }
}
