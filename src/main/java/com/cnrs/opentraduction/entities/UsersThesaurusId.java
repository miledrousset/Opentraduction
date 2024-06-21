package com.cnrs.opentraduction.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsersThesaurusId implements Serializable {

    private Integer thesaurusId;
    private Integer userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsersThesaurusId that = (UsersThesaurusId) o;
        return Objects.equals(thesaurusId, that.thesaurusId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(thesaurusId, userId);
    }
}
