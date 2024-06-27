package com.cnrs.opentraduction.models.dao;

import lombok.Data;

import java.util.List;


@Data
public class GroupDao {

    private Integer id;
    private String name;
    private ReferenceInstanceDao referenceProject;
    private List<ConsultationInstanceDao> consultationProjectsList;

}
