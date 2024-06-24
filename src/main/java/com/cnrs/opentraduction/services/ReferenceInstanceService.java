package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.entities.ReferenceInstances;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.models.dao.ReferenceInstanceDao;
import com.cnrs.opentraduction.repositories.ReferenceInstanceRepository;
import com.cnrs.opentraduction.repositories.ThesaurusRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@AllArgsConstructor
public class ReferenceInstanceService {

    private final ReferenceInstanceRepository referenceInstanceRepository;
    private final ThesaurusRepository thesaurusRepository;


    @Transactional
    public void deleteInstance(Integer idInstance) {

        var reference = referenceInstanceRepository.findById(idInstance);
        if (reference.isPresent()) {
            referenceInstanceRepository.delete(reference.get());
            log.info("Instance with ID " + idInstance + " deleted successfully.");
        } else {
            log.info("Instance with ID " + idInstance + " does not exist.");
        }
    }

    @Transactional
    public Boolean saveInstance(ReferenceInstances referenceInstances, Thesaurus thesaurus) {

        if (ObjectUtils.isEmpty(referenceInstances.getId())) {
            log.info("Enregistrement d'une nouvelle instance dans la base");
            referenceInstances.setCreated(LocalDateTime.now());
        } else {
            log.info("Mise à jour d'une instance dans la base");
            log.info("Suppression de l'ancien thésaurus");
            thesaurusRepository.delete(referenceInstances.getThesaurus());
        }

        log.info("Enregistrement de l'instance de référence dans la base");
        referenceInstances.setModified(LocalDateTime.now());

        log.info("Enregistrement du thésaurus dans la base");
        if (ObjectUtils.isEmpty(referenceInstances.getThesaurus())) {
            thesaurus.setModified(LocalDateTime.now());
            referenceInstances.setThesaurus(thesaurus);
        } else {
            thesaurus.setCreated(LocalDateTime.now());
            thesaurus.setModified(LocalDateTime.now());
            referenceInstances.setThesaurus(thesaurus);
        }

        referenceInstanceRepository.save(referenceInstances);

        return true;
    }

    public List<ReferenceInstanceDao> getAllInstances() {
        var consultationInstances = referenceInstanceRepository.findAll();

        if(!CollectionUtils.isEmpty(consultationInstances)) {
            return consultationInstances.stream()
                    .map(element -> {
                        var instance = new ReferenceInstanceDao();
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
