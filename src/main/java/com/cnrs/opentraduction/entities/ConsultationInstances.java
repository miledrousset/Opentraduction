
package com.cnrs.opentraduction.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
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

    @OneToMany(mappedBy = "consultationInstances", fetch = FetchType.EAGER)
    private Set<Thesaurus> thesauruses;

    private LocalDateTime created;

    private LocalDateTime modified;
}
