package com.cnrs.opentraduction.models.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CollectionElementDao implements Serializable, Comparable<CollectionElementDao> {

    private String id;
    private String label;

    @Override
    public int compareTo(CollectionElementDao o) {
        return label.compareTo(o.label);
    }
}
