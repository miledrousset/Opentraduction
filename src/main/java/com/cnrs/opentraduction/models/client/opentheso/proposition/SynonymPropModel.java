package com.cnrs.opentraduction.models.client.opentheso.proposition;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class SynonymPropModel {

    private String lexical_value;
    private boolean hiden;
    private String lang;
    private String oldValue;
    private boolean toAdd;
    private boolean toRemove;
    private boolean toUpdate;
}
