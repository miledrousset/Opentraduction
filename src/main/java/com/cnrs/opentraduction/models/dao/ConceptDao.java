package com.cnrs.opentraduction.models.dao;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class ConceptDao {

    private String uri;
    private String conceptId;
    private String thesaurusId;
    private String thesaurusName;

    private String labelFr;
    private String labelAr;

    private String definitionFr;
    private String definitionAr;

    private String noteFr;
    private String noteAr;

    private String varianteFr;
    private String varianteAr;

}
