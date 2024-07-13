package com.cnrs.opentraduction.models.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElementModel {

    private String id;
    private String value;
    private String lang;

}
