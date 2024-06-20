package com.cnrs.opentraduction.views;

import com.cnrs.opentraduction.entities.ConsultationInstances;
import com.cnrs.opentraduction.entities.ReferenceInstances;
import com.cnrs.opentraduction.entities.Users;
import lombok.Data;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;


@Data
@SessionScoped
@Named(value = "userSettingsBean")
public class UserSettingsBean implements Serializable {

    private Users userConnected;
    private List<ConsultationInstances> consultationInstances;
    private ReferenceInstances referenceInstances;


    public void initialInterface(Users userConnected) {

        this.userConnected = userConnected;

        referenceInstances = userConnected.getGroup().getReferenceInstances();
        consultationInstances = userConnected.getGroup().getConsultationInstances();
    }
}
