package com.cnrs.opentraduction.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;
import java.time.LocalDateTime;


@Getter
@Setter
@Entity(name = "user_thesaurus")
@IdClass(UsersThesaurusId.class)
@AllArgsConstructor
@NoArgsConstructor
public class UsersThesaurus implements Serializable {

    @Id
    @Column(name = "thesaurus_id")
    private Integer thesaurusId;

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "collection")
    private String collection;

    @Column(name = "id_collection")
    private String collectionId;

    private LocalDateTime created;

    private LocalDateTime modified;
}
