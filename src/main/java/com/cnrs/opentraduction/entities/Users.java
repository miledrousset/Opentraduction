package com.cnrs.opentraduction.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


@Data
@Entity(name = "users")
@AllArgsConstructor
@NoArgsConstructor
public class Users implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String login;

    private String password;

    @Column(unique = true)
    private String mail;

    private String firstName;

    private String lastName;

    private boolean active;

    private boolean admin;

    private LocalDateTime created;

    private LocalDateTime modified;

    @ManyToOne
    @JoinColumn(name = "id_group_user", referencedColumnName = "id")
    private Groups group;

    @ManyToMany(mappedBy = "users", fetch = FetchType.EAGER)
    private Set<Thesaurus> thesauruses;

    public String getFullName() {
        return String.join(" ", List.of(firstName, lastName));
    }

}
