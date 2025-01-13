package com.cnrs.opentraduction.models.client.wikidata;

import lombok.Data;
import java.util.Map;


@Data
public class WikidataResponseDTO {

    private Map<String, EntityDTO> entities;

}
