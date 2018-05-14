package com.technologies.highstreet.netconf2soapmediator.server;

import java.util.Collections;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class TR069Server {
	
	
	@Bean
    ServletRegistrationBean<TR069Servlet> TR069Servlet() {
        ServletRegistrationBean<TR069Servlet> srb = new ServletRegistrationBean<>();
        srb.setServlet(new TR069Servlet());
        srb.setLoadOnStartup(1);
        srb.setUrlMappings(Collections.singletonList("/*"));
        return srb;
    }


}


