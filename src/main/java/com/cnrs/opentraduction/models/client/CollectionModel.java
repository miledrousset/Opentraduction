package com.cnrs.opentraduction.models.client;

import lombok.Data;
import java.util.List;


@Data
public class CollectionModel {

    private String idGroup;
    private List<LabelModel> labels;

}
