package com.cnrs.opentraduction.models;

import lombok.Data;
import java.io.Serializable;


@Data
public class ConnexionModel implements Serializable {

    private String login;
    private String password;

    public ConnexionModel() {
        login = "";
        password = "";
    }

}
