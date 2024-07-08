package com.cnrs.opentraduction.models.client;

import lombok.Builder;
import lombok.Data;
import java.util.List;


@Data
@Builder
public class CandidateModel {

    private String thesoId;
    private String collectionId;
    private List<ElementModel> terme;
    private List<ElementModel> synonymes;
    private List<ElementModel> definition;
    private List<ElementModel> note;
    private String source;
    private String comment;

}
