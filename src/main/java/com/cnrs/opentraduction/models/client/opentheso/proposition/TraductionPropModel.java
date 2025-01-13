package com.cnrs.opentraduction.models.client.opentheso.proposition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TraductionPropModel {

    private String lang;
    private String lexicalValue;
    private String oldValue;

    private boolean toAdd;
    private boolean toRemove;
    private boolean toUpdate;

}

