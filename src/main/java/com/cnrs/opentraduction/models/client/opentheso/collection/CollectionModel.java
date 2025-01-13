package com.cnrs.opentraduction.models.client.opentheso.collection;

import com.cnrs.opentraduction.models.client.opentheso.concept.ConceptGroup;
import lombok.Data;


@Data
public class CollectionModel {

    private ConceptGroup conceptGroup;
    private String lexicalValue;

}
