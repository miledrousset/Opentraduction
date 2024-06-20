package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.entities.ConsultationInstances;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.models.dao.ConsultationInstanceDao;
import com.cnrs.opentraduction.models.dao.CollectionDao;
import com.cnrs.opentraduction.repositories.ConsultationInstanceRepository;
import com.cnrs.opentraduction.repositories.ThesaurusRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@AllArgsConstructor
public class ConsultationInstanceService {

    private final ConsultationInstanceRepository consultationInstanceRepository;
    private final ThesaurusRepository thesaurusRepository;


    public void deleteInstance(Integer idInstance) {
        consultationInstanceRepository.deleteById(idInstance);
    }


    @Transactional
    public boolean saveConsultationInstance(ConsultationInstances consultationInstances, List<Thesaurus> thesaurusList) {

        if (ObjectUtils.isEmpty(consultationInstances.getId())) {
            log.info("Enregistrement d'une nouvelle instance dans la base");
            consultationInstances.setCreated(LocalDateTime.now());
        } else {
            log.info("Mise à jour d'une instance dans la base");
            log.info("Suppression de l'ancien thésaurus");
            thesaurusRepository.deleteAll(consultationInstances.getThesauruses());
        }

        log.info("Enregistrement de l'instance de référence dans la base");
        consultationInstances.setModified(LocalDateTime.now());
        var instanceSaved = consultationInstanceRepository.save(consultationInstances);

        log.info("Enregistrement du thésaurus dans la base");
        thesaurusList.forEach(thesaurus -> {
            thesaurus.setConsultationInstances(instanceSaved);
            thesaurus.setCreated(LocalDateTime.now());
            thesaurus.setModified(LocalDateTime.now());
            thesaurusRepository.save(thesaurus);
        });

        return true;
    }

    public List<ConsultationInstanceDao> getAllConsultationInstances() {
        var consultationInstances = consultationInstanceRepository.findAll();

        if(!CollectionUtils.isEmpty(consultationInstances)) {

            List<ConsultationInstanceDao> consultationInstancesList = new ArrayList<>();

            consultationInstances.forEach(instance -> {
                if (!CollectionUtils.isEmpty(instance.getThesauruses())) {

                    var collections = instance.getThesauruses().stream()
                            .map(thesaurus -> CollectionDao.builder()
                                    .collectionId(thesaurus.getIdCollection())
                                    .collectionName(thesaurus.getCollection())
                                    .build())
                            .collect(Collectors.toList());

                    consultationInstancesList.add(ConsultationInstanceDao.builder()
                            .id(instance.getId())
                            .name(instance.getName())
                            .url(instance.getUrl())
                            .thesaurusId(instance.getThesauruses().stream().findFirst().get().getIdThesaurus())
                            .thesaurusName(instance.getThesauruses().stream().findFirst().get().getName())
                            .collectionList(collections)
                            .build());
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
