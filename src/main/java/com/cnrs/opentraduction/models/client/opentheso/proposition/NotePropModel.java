package com.cnrs.opentraduction.models.client.opentheso.proposition;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class NotePropModel {

    private String lang;
    private String lexicalvalue;
    private String oldValue;
    private boolean toAdd;
    private boolean toRemove;
    private boolean toUpdate;
}
