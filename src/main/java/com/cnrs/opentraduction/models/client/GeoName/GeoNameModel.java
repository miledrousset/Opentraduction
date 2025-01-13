package com.cnrs.opentraduction.models.client.GeoName;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class GeoNameModel {

    private String geonameId;
    private String toponymName;
    private String alternateNameAr;
    private String alternateNameFr;

}
