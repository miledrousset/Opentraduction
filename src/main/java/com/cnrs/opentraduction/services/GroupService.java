package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.clients.OpenthesoClient;
import com.cnrs.opentraduction.entities.ConsultationInstances;
import com.cnrs.opentraduction.entities.Groups;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.models.dao.CollectionDao;
import com.cnrs.opentraduction.models.dao.ConsultationInstanceDao;
import com.cnrs.opentraduction.models.dao.GroupDao;
import com.cnrs.opentraduction.models.dao.NodeIdValue;
import com.cnrs.opentraduction.models.dao.ReferenceInstanceDao;
import com.cnrs.opentraduction.repositories.GroupConsultationInstancesRepository;
import com.cnrs.opentraduction.repositories.GroupRepository;
import com.cnrs.opentraduction.repositories.ReferenceInstanceRepository;
import com.cnrs.opentraduction.utils.MessageService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Slf4j
@Data
@Service
@AllArgsConstructor
public class GroupService {

    private final MessageService messageService;
    private final GroupRepository groupRepository;
    private final OpenthesoClient openthesoClient;
    private final GroupConsultationInstancesRepository groupConsultationInstancesRepository;
    private final ReferenceInstanceRepository referenceInstanceRepository;
    private final ConsultationService consultationInstanceService;


    public List<NodeIdValue> searchConceptByGroup(Users userConnected, String idGroup) {
        return openthesoClient.searchConceptByGroup(
                userConnected.getGroup().getReferenceInstances().getUrl(),
                userConnected.getGroup().getReferenceInstances().getThesaurus().getIdThesaurus(),
                idGroup);
    }

    public void deleteGroup(Integer groupId) {
        groupRepository.deleteById(groupId);
    }

    public void saveGroup(GroupDao groupDao, ReferenceInstanceDao referenceProjects,
                          List<ConsultationInstanceDao> consultationProjects) {

        var groupToSave = new Groups();

        if (ObjectUtils.isEmpty(groupDao.getId())) {
            log.info("Cas de création d'un nouveau groupe");
            groupToSave.setCreated(LocalDateTime.now());
        } else {
            log.info("Cas de mise à jour de groupe");
            var groupSaved = groupRepository.findById(groupDao.getId());
            if (groupSaved.isPresent()) {
                groupToSave.setCreated(groupSaved.get().getCreated());

                log.info("Suppression du thésaurus de référence");
                if (!ObjectUtils.isEmpty(groupSaved.get().getReferenceInstances())) {
                    groupRepository.updateReferenceProject(groupSaved.get().getId(), null);
                }

                log.info("Suppression des thésaurus de collection");
                if (!CollectionUtils.isEmpty(groupSaved.get().getConsultationInstances())) {
                    groupConsultationInstancesRepository.deleteAllByGroupId(groupSaved.get().getId());
                }
            }
        }

        groupToSave.setId(groupDao.getId());
        groupToSave.setName(groupDao.getName());
        groupToSave.setModified(LocalDateTime.now());

        referenceInstanceRepository.findById(referenceProjects.getId()).ifPresent(groupToSave::setReferenceInstances);

        Set<ConsultationInstances> consultationTmp = new HashSet<>();
        if (!CollectionUtils.isEmpty(consultationProjects)) {
            consultationProjects.forEach(consultation -> consultationTmp.add(consultationInstanceService.getInstanceById(consultation.getId())));
        }
        groupToSave.setConsultationInstances(consultationTmp);

        log.info("Mise à jour de la base de donnée");
        groupRepository.save(groupToSave);
    }

    public List<GroupDao> getAllGroups() {

        var groups = getGroups();

        List<GroupDao> groupsModel = new ArrayList<>();

        if (!CollectionUtils.isEmpty(groups)) {

            groups.forEach(group -> {
                var groupModel = new GroupDao();
                groupModel.setId(group.getId());
                groupModel.setName(group.getName());

                if (!ObjectUtils.isEmpty(group.getReferenceInstances())) {

                    var referenceProject = new ReferenceInstanceDao();
                    referenceProject.setId(group.getReferenceInstances().getId());
                    referenceProject.setName(group.getReferenceInstances().getName());
                    referenceProject.setThesaurusName(group.getReferenceInstances().getThesaurus().getName());
                    referenceProject.setThesaurusId(group.getReferenceInstances().getThesaurus().getIdThesaurus());
                    referenceProject.setThesaurusUrl(group.getReferenceInstances().getUrl() + "/?idt="
                            + group.getReferenceInstances().getThesaurus().getIdThesaurus());
                    referenceProject.setCollectionId(group.getReferenceInstances().getThesaurus().getIdCollection());
                    referenceProject.setCollectionName(group.getReferenceInstances().getThesaurus().getCollection());
                    groupModel.setReferenceProject(referenceProject);
                }

                groupModel.setConsultationProjectsList(new ArrayList<>());
                if (!CollectionUtils.isEmpty(group.getConsultationInstances())) {
                    groupModel.getConsultationProjectsList()
                            .addAll(group.getConsultationInstances().stream()
                                    .map(instance -> {
                                        var consultationProject = ConsultationInstanceDao.builder()
                                                .id(instance.getId())
                                                .name(instance.getName())
                                                .build();

                                        List<CollectionDao> collectionsList = new ArrayList<>();

                                        if (!CollectionUtils.isEmpty(instance.getThesauruses())) {
                                            instance.getThesauruses().forEach(thesaurus -> {
                                                consultationProject.setThesaurusName(thesaurus.getName());
                                                consultationProject.setThesaurusId(thesaurus.getIdThesaurus());
                                                consultationProject.setThesaurusUrl(instance.getUrl() + "/?idt=" + thesaurus.getIdThesaurus());

                                                collectionsList.add(CollectionDao.builder()
                                                        .id(thesaurus.getIdCollection())
                                                        .name(thesaurus.getCollection())
                                                        .build());
                                            });
                                        }

                                        consultationProject.setCollectionList(collectionsList);
                                        return consultationProject;
                                    })
                                    .toList());
                }

                groupsModel.add(groupModel);
            });

            return groupsModel;
        } else {
            return List.of();
        }
    }

    public List<Groups> getGroups() {
        return groupRepository.findAllByOrderByName();
    }


    public Groups getGroupById(Integer groupId) {

        return groupRepository.findById(groupId).orElse(null);
    }


    public Groups getGroupByName(String name) {

        return groupRepository.findGroupsByName(name).orElse(null);
    }

}
