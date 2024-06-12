package com.cnrs.opentraduction.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;


@Getter
@Setter
@Entity(name = "user_thesaurus")
@AllArgsConstructor
@NoArgsConstructor
public class UsersThesaurus implements Serializable {

    @Id
    @Column(name = "thesaurus_id")
    private Integer idThesaurus;

    @Id
    @Column(name = "user_id")
    private Integer idUser;

    @Column(name = "collection")
    private String collection;

    @Column(name = "id_collection")
    private String idCollection;
}
