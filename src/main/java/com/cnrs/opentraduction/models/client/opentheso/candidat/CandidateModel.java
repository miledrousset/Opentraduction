package com.cnrs.opentraduction.models.client.opentheso.candidat;

import com.cnrs.opentraduction.models.client.opentheso.concept.ElementModel;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;


@Data
@Builder
@ToString
public class CandidateModel {

    private String thesoId;
    private String collectionId;
    private String conceptGenericId;
    private List<ElementModel> terme;
    private List<ElementModel> synonymes;
    private List<ElementModel> definition;
    private List<ElementModel> note;
    private String source;
    private String comment;
}
