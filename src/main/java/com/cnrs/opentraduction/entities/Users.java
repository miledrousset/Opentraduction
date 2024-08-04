package com.cnrs.opentraduction.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


@Getter
@Setter
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

    @Column(name = "api_key")
    private String apiKey;

    private boolean active;

    private boolean admin;

    private LocalDateTime created;

    private LocalDateTime modified;

    @Column(name = "default_target_traduction")
    private String defaultTargetTraduction;

    @ManyToOne
    @JoinColumn(name = "id_group", referencedColumnName = "id")
    private Groups group;

    public String getFullName() {
        return String.join(" ", List.of(firstName, lastName));
    }

}
