package com.cnrs.opentraduction.controlleurs;

import com.cnrs.opentraduction.models.dao.ConnexionDto;
import com.cnrs.opentraduction.models.MenuItem;
import com.cnrs.opentraduction.views.ApplicationBean;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.HttpServletRequest;


@Slf4j
@Data
@Controller
@RequestMapping("/logout")
public class UserController {

    private final ApplicationBean applicationBean;

    @PostMapping
    public String logout(HttpServletRequest request) {

        log.info("Session timeout -> logout and navigate to index.xhtml");
        applicationBean.setConnexionModel(new ConnexionDto());
        applicationBean.setConnected(false);
        applicationBean.setUserConnected(null);
        applicationBean.setMenuItemSelected(MenuItem.HOME);
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/index.xhtml";
    }
}

