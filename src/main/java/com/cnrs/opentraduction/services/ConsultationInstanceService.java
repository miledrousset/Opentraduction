package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.entities.ConsultationInstances;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.models.dao.ConsultationInstanceDao;
import com.cnrs.opentraduction.models.dao.CollectionDao;
import com.cnrs.opentraduction.repositories.ConsultationInstanceRepository;
import com.cnrs.opentraduction.repositories.ThesaurusRepository;
import com.cnrs.opentraduction.utils.MessageService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.faces.application.FacesMessage;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@AllArgsConstructor
public class ConsultationInstanceService {

    private final ConsultationInstanceRepository consultationInstanceRepository;
    private final ThesaurusRepository thesaurusRepository;
    private final MessageService messageService;


    public boolean deleteInstance(Integer idInstance) {
        try {
            consultationInstanceRepository.deleteById(idInstance);
            return true;
        } catch(DataIntegrityViolationException ex) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "system.consultation.failed.msg3");
            return false;
        }
    }


    public boolean saveConsultationInstance(ConsultationInstances consultationInstances, List<Thesaurus> thesaurusToSave) {

        if (ObjectUtils.isEmpty(consultationInstances.getId())) {
            log.info("Enregistrement d'une nouvelle instance dans la base");
            consultationInstances.setCreated(LocalDateTime.now());
        } else {
            log.info("Mise à jour d'une instance dans la base");
            log.info("Suppression de l'ancien thésaurus");
            thesaurusRepository.deleteByConsultationInstanceId(consultationInstances.getId());
        }

        log.info("Enregistrement de l'instance de référence dans la base");
        consultationInstances.setModified(LocalDateTime.now());
        consultationInstances.setThesauruses(new HashSet<>(thesaurusToSave));
        consultationInstanceRepository.save(consultationInstances);
        return true;
    }

    public List<ConsultationInstanceDao> getAllConsultationInstances() {

        var consultationInstances = consultationInstanceRepository.findAllByOrderByName();

        if(!CollectionUtils.isEmpty(consultationInstances)) {

            return consultationInstances.stream()
                    .filter(element -> !CollectionUtils.isEmpty(element.getThesauruses()))
                    .map(instance -> {
                        var collections = instance.getThesauruses().stream()
                                .map(thesaurus -> CollectionDao.builder()
                                        .collectionId(thesaurus.getIdCollection())
                                        .collectionName(thesaurus.getCollection())
                                        .build())
                                .collect(Collectors.toList());

                        return ConsultationInstanceDao.builder()
                                .id(instance.getId())
                                .name(instance.getName())
                                .url(instance.getUrl())
                                .thesaurusId(instance.getThesauruses().stream().findFirst().get().getIdThesaurus())
                                .thesaurusName(instance.getThesauruses().stream().findFirst().get().getName())
                                .collectionList(collections)
                                .build();
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

    public boolean checkExistingName(String nameToCheck) {
        return consultationInstanceRepository.findByName(nameToCheck).isPresent();
    }
}
