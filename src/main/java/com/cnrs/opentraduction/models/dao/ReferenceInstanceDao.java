package com.cnrs.opentraduction.models.dao;

import lombok.Data;


@Data
public class ReferenceInstanceDao {

    private Integer id;
    private String name;
    private String url;
    private String thesaurusId;
    private String thesaurusName;
    private String collectionId;
    private String collectionName;

}
