package com.cnrs.opentraduction.models.client.opentheso.collection;

import com.cnrs.opentraduction.models.client.opentheso.concept.LabelModel;
import lombok.Data;

import java.util.List;


@Data
public class TopCollectionModel {

    private String idConcept;
    private List<LabelModel> labels;

}
