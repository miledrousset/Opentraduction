package com.cnrs.opentraduction.models.client.opentheso.users;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;


@Data
public class UserModel {

    private int idUser;
    private String name;
    private String mail;
    private boolean active;
    private boolean alertMail;
    private boolean superAdmin;
    private boolean passToModify;
    private String apiKey;
    private boolean keyNeverExpire;
    private LocalDate apiKeyExpireDate;
    private boolean isServiceAccount;
    private String keyDescription;
    private List<String> thesorusList;

}
