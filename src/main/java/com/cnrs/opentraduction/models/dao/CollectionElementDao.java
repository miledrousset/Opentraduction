package com.cnrs.opentraduction.models.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
@AllArgsConstructor
public class CollectionElementDao implements Serializable, Comparable<CollectionElementDao> {

    private String id;
    private String label;

    public CollectionElementDao() {
        id = "ALL";
        label = "Toutes les collections";
    }

    @Override
    public int compareTo(CollectionElementDao o) {
        return label.compareTo(o.label);
    }
}
