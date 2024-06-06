package com.cnrs.opentraduction.config;

import com.cnrs.opentraduction.views.ApplicationBean;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class SessionTimeoutFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        var httpRequest = (HttpServletRequest) request;
        var httpResponse = (HttpServletResponse) response;

        var isLoginRequest = httpRequest.getRequestURI().contains("index.xhtml");

        if (!StringUtils.isEmpty(httpRequest.getRequestedSessionId())
                && !httpRequest.isRequestedSessionIdValid()
                && !isLoginRequest) {

            var applicationBean = SpringUtils.getBean(httpRequest.getServletContext(), ApplicationBean.class);
            if (!ObjectUtils.isEmpty(applicationBean)) {
                applicationBean.logout();
            }
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/index.xhtml");
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }
}
