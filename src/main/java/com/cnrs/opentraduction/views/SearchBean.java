package com.cnrs.opentraduction.views;

import lombok.Data;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;


@Data
@SessionScoped
@Named(value = "searchBean")
public class SearchBean implements Serializable {

    public void searchTerm() throws IOException {

        FacesContext.getCurrentInstance().getExternalContext().redirect("search.xhtml");
    }
}
