package com.cnrs.opentraduction.models.client.opentheso.concept;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class NodeModel {
    private String id;
    private String value;
    private String lang;
}
