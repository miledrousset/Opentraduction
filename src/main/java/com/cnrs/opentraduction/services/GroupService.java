package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.repositories.GroupRepository;
import org.springframework.stereotype.Service;


@Service
public class GroupService {

    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

}
