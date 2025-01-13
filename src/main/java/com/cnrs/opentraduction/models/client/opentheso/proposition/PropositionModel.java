package com.cnrs.opentraduction.models.client.opentheso.proposition;

import lombok.Builder;
import lombok.Data;
import java.util.List;


@Data
@Builder
public class PropositionModel {

    private String IdTheso;
    private String conceptID;

    private String commentaire;

    private List<TraductionPropModel> traductionsProp;
    private List<SynonymPropModel> synonymsProp;
    private List<NotePropModel> definitions;
    private List<NotePropModel> notes;

}
