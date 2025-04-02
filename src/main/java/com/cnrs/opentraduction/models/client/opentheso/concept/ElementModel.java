package com.cnrs.opentraduction.models.client.opentheso.concept;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ElementModel {

    private String id;
    private String value;
    private String lang;

}
