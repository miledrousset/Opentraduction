package com.cnrs.opentraduction.models.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceInstanceDao {

    private Integer id;
    private String name;
    private String url;
    private String thesaurusId;
    private String thesaurusName;
    private String thesaurusUrl;
    private String collectionId;
    private String collectionName;

}
