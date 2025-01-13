package com.cnrs.opentraduction.models.client.opentheso.collection;

import com.cnrs.opentraduction.models.client.opentheso.concept.LabelModel;
import lombok.Data;

import java.util.List;


@Data
public class SubCollectionModel {

    private String idGroup;
    private List<LabelModel> labels;

}
