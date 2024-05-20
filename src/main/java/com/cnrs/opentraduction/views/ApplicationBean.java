package com.cnrs.opentraduction.views;

import lombok.Data;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;


@Data
@SessionScoped
@Named(value = "applicationBean")
public class ApplicationBean implements Serializable {

    private boolean connected;

    public String getUserNameConnected() {
        return "Bienvenu" + (connected ? " Firas GABSI" : "");
    }
}
