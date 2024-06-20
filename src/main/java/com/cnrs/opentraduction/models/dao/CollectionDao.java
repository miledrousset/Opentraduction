package com.cnrs.opentraduction.models.dao;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class CollectionDao {

    private String collectionId;
    private String collectionName;

}
