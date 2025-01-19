package com.cnrs.opentraduction.models.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeIdValue {

    private String idConcept;
    private String prefLabel;

}
