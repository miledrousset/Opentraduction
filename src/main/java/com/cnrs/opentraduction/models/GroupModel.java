package com.cnrs.opentraduction.models;

import lombok.Data;


@Data
public class GroupModel {

    private Integer id;
    private String name;
    private String thesaurusId;
    private String thesaurusName;
    private String thesaurusUrl;
    private String collectionId;
    private String collectionName;

}
