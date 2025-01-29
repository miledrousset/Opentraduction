package com.cnrs.opentraduction.models.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDao implements Serializable {

    private String login;
    private String password;
    private String mail;
    private boolean superAdmin;
    private boolean alertMail;
    private boolean active;

    private String idThesaurus;
    private Integer idRole;
    private String idProject;

}
