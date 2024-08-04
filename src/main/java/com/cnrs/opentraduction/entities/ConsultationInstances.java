
package com.cnrs.opentraduction.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;


@Entity(name = "consultation_instances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationInstances implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String url;

    @ManyToMany(mappedBy = "consultationInstances")
    private Set<Groups> groups;

    @OneToMany(mappedBy = "consultationInstances", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Thesaurus> thesauruses;

    private LocalDateTime created;

    private LocalDateTime modified;
}
