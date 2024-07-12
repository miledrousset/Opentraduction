package com.cnrs.opentraduction.models.client;

import lombok.Data;
import java.util.List;


@Data
public class ConceptModel {

    private String idConcept;
    private String idTerm;
    private String status;
    private List<ElementModel> terms;
    private List<ElementModel> collections;
    private List<ElementModel> synonymes;
    private List<ElementModel> notes;
    private List<ElementModel> definitions;

}
