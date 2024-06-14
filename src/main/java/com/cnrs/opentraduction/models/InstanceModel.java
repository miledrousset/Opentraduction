package com.cnrs.opentraduction.models;

import lombok.Data;


@Data
public class InstanceModel {

    private Integer id;
    private String name;
    private String url;
    private String thesaurusId;
    private String thesaurusName;
    private String collectionId;
    private String collectionName;

}
