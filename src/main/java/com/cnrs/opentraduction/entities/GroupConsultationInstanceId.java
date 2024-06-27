package com.cnrs.opentraduction.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Objects;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupConsultationInstanceId implements Serializable {

    private Integer groupId;
    private Integer consultationInstanceId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupConsultationInstanceId that = (GroupConsultationInstanceId) o;
        return Objects.equals(groupId, that.groupId) &&
                Objects.equals(consultationInstanceId, that.consultationInstanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, consultationInstanceId);
    }
}
