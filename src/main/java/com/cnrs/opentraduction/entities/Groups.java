package com.cnrs.opentraduction.entities;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
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
