package com.cnrs.opentraduction.controlleurs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.HttpServletRequest;


@Slf4j
@Controller
@RequestMapping("/logout")
public class UserController {

    @PostMapping
    public String logout(HttpServletRequest request) {

        log.info("Session timeout -> logout and navigate to index.xhtml");
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/index.xhtml";
    }
}

