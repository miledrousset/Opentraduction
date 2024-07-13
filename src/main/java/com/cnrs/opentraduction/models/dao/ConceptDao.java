package com.cnrs.opentraduction.models.dao;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class ConceptDao {

    private String conceptId;
    private String status;

    private String thesaurusId;
    private String thesaurusName;

    private List<CollectionDao> collections;

    private String labelFr;
    private String labelAr;

    private String definitionFr;
    private String definitionAr;

    private String noteFr;
    private String noteAr;

    private String varianteFr;
    private String varianteAr;

    private String url;

}
