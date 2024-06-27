package com.cnrs.opentraduction.entities;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;


@Entity(name = "group_consultation_instances")
@Getter
@Setter
@IdClass(GroupConsultationInstanceId.class)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GroupConsultationInstances implements Serializable {

    @Id
    @Column(name = "group_id")
    private Integer groupId;

    @Id
    @Column(name = "consultation_instance_id")
    private Integer consultationInstanceId;

}
