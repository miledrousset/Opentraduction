package com.cnrs.opentraduction.models.dao;

import lombok.Data;
import java.io.Serializable;


@Data
public class ConnexionDto implements Serializable {

    private String login;
    private String password;

    public ConnexionDto() {
        login = "";
        password = "";
    }

}
