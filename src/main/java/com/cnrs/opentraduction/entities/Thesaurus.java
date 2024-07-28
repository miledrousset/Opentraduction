package com.cnrs.opentraduction.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.time.LocalDateTime;
import java.util.Set;

@Entity(name = "thesaurus")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Thesaurus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(name = "id_thesaurus")
    private String idThesaurus;

    private String collection;

    @Column(name = "id_collection")
    private String idCollection;

    private LocalDateTime created;

    private LocalDateTime modified;

    @ManyToOne
    @JoinColumn(name = "consultation_instance_id", referencedColumnName = "id")
    private ConsultationInstances consultationInstances;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "reference_instance_id", referencedColumnName = "id")
    private ReferenceInstances referenceInstances;
}
