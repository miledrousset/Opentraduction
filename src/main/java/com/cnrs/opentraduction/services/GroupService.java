package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.entities.Groups;
import com.cnrs.opentraduction.models.GroupModel;
import com.cnrs.opentraduction.repositories.GroupRepository;
import com.cnrs.opentraduction.utils.MessageUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.faces.application.FacesMessage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class GroupService {

    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public Groups getGroupById(Integer groupId) {
        var group = groupRepository.findById(groupId);
        return group.orElse(null);
    }

    public void deleteGroup(Integer groupId) {
        groupRepository.deleteById(groupId);
    }

    public void saveGroup(Groups groupSelected) {

        if (StringUtils.isEmpty(groupSelected.getName())) {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "Le nom du group est obligatoire !");
            return;
        }

        groupSelected.setModified(LocalDateTime.now());
        if (ObjectUtils.isEmpty(groupSelected.getId())) {
            groupSelected.setCreated(LocalDateTime.now());
        }

        groupRepository.save(groupSelected);
    }

    public List<GroupModel> getAllGroups() {
        var groups = groupRepository.findAll();

        List<GroupModel> groupsModel = new ArrayList<>();

        if (!CollectionUtils.isEmpty(groups)) {

            groups.forEach(group -> {
                if (!ObjectUtils.isEmpty(group.getReferenceInstances())) {
                    var groupModel = new GroupModel();
                    groupModel.setId(group.getId());
                    groupModel.setName(group.getName());
                    groupModel.setThesaurusId(group.getReferenceInstances().getThesaurus().getIdThesaurus());
                    groupModel.setThesaurusName(group.getReferenceInstances().getThesaurus().getName());
                    groupModel.setThesaurusUrl(group.getReferenceInstances().getUrl() + "/?idt="
                            + group.getReferenceInstances().getThesaurus().getIdThesaurus());
                    groupModel.setCollectionId(group.getReferenceInstances().getThesaurus().getIdCollection());
                    groupModel.setCollectionName(group.getReferenceInstances().getThesaurus().getCollection());
                    groupModel.setReference(true);
                    groupModel.setConsultation(false);
                    groupsModel.add(groupModel);
                }

                if (!CollectionUtils.isEmpty(group.getConsultationInstances())) {
                    group.getConsultationInstances().forEach(instance -> {

                        if (!CollectionUtils.isEmpty(instance.getThesauruses())) {
                            instance.getThesauruses().forEach(thesaurus -> {
                                var groupModel = new GroupModel();
                                groupModel.setId(group.getId());
                                groupModel.setName(group.getName());
                                groupModel.setThesaurusId(thesaurus.getIdThesaurus());
                                groupModel.setThesaurusName(thesaurus.getName());
                                groupModel.setThesaurusUrl(instance.getUrl() + "/?idt=" + thesaurus.getIdThesaurus());
                                groupModel.setCollectionId(thesaurus.getIdCollection());
                                groupModel.setCollectionName(thesaurus.getCollection());
                                groupModel.setConsultation(true);
                                groupModel.setReference(false);
                                groupsModel.add(groupModel);
                            });
                        }
                    });
                }
            });

            return groupsModel;
        } else {
            return List.of();
        }
    }

}
