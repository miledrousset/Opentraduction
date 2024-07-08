package com.cnrs.opentraduction.models.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CandidatDao {

    private String thesoId;
    public String collectionId;

    private String titleFr;
    private String titleAr;

    private String varianteFr;
    private String varianteAr;

    private String definitionFr;
    private String definitionAr;

    private String noteFr;
    private String noteAr;

    private String comment;
    private String source;

}
