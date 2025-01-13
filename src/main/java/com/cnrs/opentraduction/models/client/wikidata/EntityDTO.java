package com.cnrs.opentraduction.models.client.wikidata;

import lombok.Data;
import java.util.Map;
import java.util.List;


@Data
public class EntityDTO {

    private Map<String, Map<String, String>> labels;
    private Map<String, Map<String, String>> descriptions;
    private Map<String, List<AliasDTO>> aliases;

}
