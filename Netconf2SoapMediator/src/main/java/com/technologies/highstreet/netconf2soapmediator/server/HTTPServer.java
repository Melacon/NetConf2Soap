/* Copyright (C) 2018 Pierluigi Greto */

package com.technologies.highstreet.netconf2soapmediator.server;

import java.util.Collections;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class HTTPServer {
	
	
	@Bean
    ServletRegistrationBean<HTTPServlet> hTTPServlet() {
        ServletRegistrationBean<HTTPServlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new HTTPServlet());
        srb.setLoadOnStartup(1);
        srb.setUrlMappings(Collections.singletonList("/*"));
        return srb;
    }


}


