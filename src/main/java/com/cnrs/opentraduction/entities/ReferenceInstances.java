package com.cnrs.opentraduction.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Entity(name = "reference_instances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceInstances implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String url;

    @OneToMany(mappedBy = "referenceInstances")
    private Set<Groups> groups;

    @OneToOne(mappedBy = "referenceInstances", cascade = CascadeType.ALL)
    private Thesaurus thesaurus;

    private LocalDateTime created;

    private LocalDateTime modified;
}
