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
import java.util.List;
import java.util.stream.Collectors;


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
        if (!CollectionUtils.isEmpty(groups)) {
            return groups.stream()
                    .map(element -> {
                        var group = new GroupModel();
                        group.setId(element.getId());
                        group.setName(element.getName());

                        if (!CollectionUtils.isEmpty(element.getConsultationInstances())) {
                            var instance = element.getConsultationInstances().get(0);
                            if (!CollectionUtils.isEmpty(instance.getThesauruses())) {
                                var thesaurus = instance.getThesauruses().stream().findFirst();
                                group.setThesaurusId(thesaurus.get().getIdThesaurus());
                                group.setThesaurusName(thesaurus.get().getName());
                                group.setThesaurusUrl(thesaurus.get().getInstance().getUrl() + "/?idt=" + thesaurus.get().getIdThesaurus());
                                group.setCollectionId(thesaurus.get().getIdCollection());
                                group.setCollectionName(thesaurus.get().getCollection());
                            }
                        }

                        return group;
                    })
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

}
