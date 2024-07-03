package com.cnrs.opentraduction.models.client;

import lombok.Data;

import java.util.List;


@Data
public class SubCollectionModel {

    private String idConcept;
    private List<LabelModel> labels;

}
