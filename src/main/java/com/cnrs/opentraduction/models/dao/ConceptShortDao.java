package com.cnrs.opentraduction.models.dao;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class ConceptShortDao {

    private String identifier;
    private String uri;
    private String label;
}
