package com.cnrs.opentraduction.models.client;

import lombok.Data;


@Data
public class ConceptModel {

    private String uri;
    private String label;
    private boolean isAltLabel;
    private String definition;

}
