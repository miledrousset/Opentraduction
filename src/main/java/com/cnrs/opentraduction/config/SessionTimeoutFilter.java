package com.cnrs.opentraduction.config;

import com.cnrs.opentraduction.views.ApplicationBean;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.faces.context.FacesContext;


@Slf4j
@Component
public class SessionTimeoutFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        var httpRequest = (HttpServletRequest) request;
        var httpResponse = (HttpServletResponse) response;

        if (!StringUtils.isEmpty(httpRequest.getRequestedSessionId())
                && !httpRequest.isRequestedSessionIdValid()) {

            try {
                var applicationBean = SpringUtils.getBean(httpRequest.getServletContext(), ApplicationBean.class);
                if (!ObjectUtils.isEmpty(applicationBean)) {
                    applicationBean.logout();
                }
            } catch (Exception ex) {
                log.error("Erreur pendant la fermeture de la session");
            }

            if (FacesContext.getCurrentInstance() != null) {
                FacesContext.getCurrentInstance().getExternalContext().redirect(httpRequest.getContextPath() + "/index.xhtml");
            } else {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/index.xhtml");
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }
}

