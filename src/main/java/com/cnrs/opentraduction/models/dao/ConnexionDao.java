package com.cnrs.opentraduction.models.dao;

import lombok.Data;
import java.io.Serializable;


@Data
public class ConnexionDao implements Serializable {

    private String login;
    private String password;

    public ConnexionDao() {
        login = "";
        password = "";
    }

}
