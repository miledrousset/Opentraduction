package com.cnrs.opentraduction.entities;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Entity(name = "groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Groups implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Users> users;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "group_consultation_instances",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "consultation_instance_id"))
    private Set<ConsultationInstances> consultationInstances;

    @ManyToOne
    @JoinColumn(name = "id_reference_instance", referencedColumnName = "id")
    private ReferenceInstances referenceInstances;

    private LocalDateTime created;

    private LocalDateTime modified;

    public List<ConsultationInstances> getConsultationInstances() {
        return consultationInstances.stream().collect(Collectors.toList());
    }

}
