package com.cnrs.opentraduction.models.client.wikidata;

import lombok.Data;
import java.util.List;
import java.util.Map;


@Data
public class EntityDetailsDTO {

    private String id;
    private Map<String, String> labels;
    private Map<String, String> descriptions;
    private Map<String, List<AliasDTO>> aliases;

}
