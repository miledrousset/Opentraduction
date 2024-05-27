package com.cnrs.opentraduction.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


@Entity(name = "user")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class User {

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
    private Group group;

    @ManyToMany(mappedBy = "users")
    private Set<Thesaurus> thesauruses;

    public String getFullName() {
        return String.join(" ", List.of(firstName, lastName));
    }
}
