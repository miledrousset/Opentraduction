package com.cnrs.opentraduction.models;

import com.cnrs.opentraduction.entities.Groups;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;


@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class User implements Serializable {

    private Integer id;
    private String login;
    private String password;
    private String mail;
    private String firstName;
    private String lastName;
    private boolean active;
    private boolean admin;
    private Groups group;
}
