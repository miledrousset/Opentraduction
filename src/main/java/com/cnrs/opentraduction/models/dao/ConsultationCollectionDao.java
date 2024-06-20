package com.cnrs.opentraduction.models.dao;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class ConsultationCollectionDao {

    private Integer id;
    private String name;
    private String url;

    private String thesaurusId;
    private String thesaurusName;

    private String collectionId;
    private String collectionName;

}
