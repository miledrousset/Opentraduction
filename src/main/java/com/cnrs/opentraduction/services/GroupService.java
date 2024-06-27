package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.entities.ConsultationInstances;
import com.cnrs.opentraduction.entities.Groups;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.entities.UsersThesaurus;
import com.cnrs.opentraduction.models.dao.CollectionDao;
import com.cnrs.opentraduction.models.dao.ConsultationInstanceDao;
import com.cnrs.opentraduction.models.dao.GroupDao;
import com.cnrs.opentraduction.models.dao.ReferenceInstanceDao;
import com.cnrs.opentraduction.repositories.GroupConsultationInstancesRepository;
import com.cnrs.opentraduction.repositories.GroupRepository;
import com.cnrs.opentraduction.repositories.ReferenceInstanceRepository;
import com.cnrs.opentraduction.repositories.UserThesaurusRepository;
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
import java.util.stream.Collectors;


@Slf4j
@Data
@Service
@AllArgsConstructor
public class GroupService {

    private final MessageService messageService;
    private final GroupRepository groupRepository;
    private final UserThesaurusRepository userThesaurusRepository;
    private final GroupConsultationInstancesRepository groupConsultationInstancesRepository;
    private final ReferenceInstanceRepository referenceInstanceRepository;
    private final ConsultationInstanceService consultationInstanceService;


    public Groups getGroupById(Integer groupId) {
        var group = groupRepository.findById(groupId);
        return group.orElse(null);
    }

    public void deleteGroup(Integer groupId) {
        groupRepository.deleteById(groupId);
    }

    public void saveGroup(GroupDao groupDao, ReferenceInstanceDao referenceProjects,
                          List<ConsultationInstanceDao> consultationProjects) {

        var isUpdateCase = false;
        var groupToSave = new Groups();
        List<Users> users = new ArrayList<>();

        if (ObjectUtils.isEmpty(groupDao.getId())) {
            log.info("Cas de création d'un nouveau groupe");
            groupToSave.setCreated(LocalDateTime.now());
        } else {
            log.info("Cas de mise à jour de groupe");
            isUpdateCase = true;
            var groupSaved = groupRepository.findById(groupDao.getId()).get();
            groupToSave.setCreated(groupSaved.getCreated());

            log.info("Suppression du thésaurus de référence");
            if (!ObjectUtils.isEmpty(groupSaved.getReferenceInstances())) {
                groupRepository.updateReferenceProject(groupSaved.getId(), null);
            }

            log.info("Suppression des thésaurus de collection");
            if (!CollectionUtils.isEmpty(groupSaved.getConsultationInstances())) {
                groupConsultationInstancesRepository.deleteAllByGroupId(groupSaved.getId());
            }

            log.info("Suppression des associations groupe/thésaurus");
            users = groupSaved.getUsers();
            if (!CollectionUtils.isEmpty(users)) {
                users.forEach(user -> userThesaurusRepository.deleteByUserId(user.getId()));
            }
        }

        groupToSave.setId(groupDao.getId());
        groupToSave.setName(groupDao.getName());
        groupToSave.setModified(LocalDateTime.now());

        var reference = referenceInstanceRepository.findById(referenceProjects.getId());
        groupToSave.setReferenceInstances(reference.get());

        Set<ConsultationInstances> consultationTmp = new HashSet<>();
        if (!CollectionUtils.isEmpty(consultationProjects)) {
            consultationProjects.forEach(consultation -> consultationTmp.add(consultationInstanceService.getInstanceById(consultation.getId())));
        }
        groupToSave.setConsultationInstances(consultationTmp);

        log.info("Mise à jour de la base de donnée");
        groupRepository.save(groupToSave);

        if (isUpdateCase) {
            log.info("Mise à jour des thésaurus de consultations pour chaque utilisateur");
            for (Users user : users) {
                for (ConsultationInstances consultationInstances : groupToSave.getConsultationInstances()) {
                    for (Thesaurus thesaurus : consultationInstances.getThesauruses()) {
                        userThesaurusRepository.save(UsersThesaurus.builder()
                                .userId(user.getId())
                                .thesaurusId(thesaurus.getId())
                                .collectionId(thesaurus.getIdCollection())
                                .collection(thesaurus.getCollection())
                                .created(LocalDateTime.now())
                                .modified(LocalDateTime.now())
                                .build());
                    }
                }
            }
        }
    }

    public List<GroupDao> getAllGroups() {
        var groups = groupRepository.findAll();

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
                                                        .collectionId(thesaurus.getIdCollection())
                                                        .collectionName(thesaurus.getCollection())
                                                        .build());
                                            });
                                        }

                                        consultationProject.setCollectionList(collectionsList);
                                        return consultationProject;
                                    })
                                    .collect(Collectors.toList()));
                }

                groupsModel.add(groupModel);
            });

            return groupsModel;
        } else {
            return List.of();
        }
    }

}
