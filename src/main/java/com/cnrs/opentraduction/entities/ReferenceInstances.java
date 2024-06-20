package com.cnrs.opentraduction.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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
