package com.cnrs.opentraduction.models.client;

import lombok.Data;

import java.util.List;


@Data
public class TopCollectionModel {

    private String idConcept;
    private List<LabelModel> labels;

}
