package com.cnrs.opentraduction.models.dao;

import lombok.Builder;
import lombok.Data;
import java.util.List;


@Data
@Builder
public class ConsultationInstanceDao {

    private Integer id;
    private String name;
    private String url;

    private String thesaurusId;
    private String thesaurusName;
    private String thesaurusUrl;

    private List<CollectionDao> collectionList;

}
