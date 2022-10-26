/**
 *  Copyright 2005-2016 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package io.fabric8.quickstarts;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import javax.mail.Session;
import java.util.Properties;

/**
 * A spring-boot application that includes a Camel route builder to setup the Camel routes
 */
@SpringBootApplication
public class Application {

    @Autowired
    ApplicationConfiguration conf;

    // must have a main method spring-boot can run
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public Session mailSession() {
        Oauth2ExchangeMailAuthenticator authenticator =
                new Oauth2ExchangeMailAuthenticator(
                        conf.getTenantId(),
                        conf.getClientId(),
                        conf.getClientSecret(),
                        conf.getUser()
                );
        Properties properties = new Properties();
        properties.setProperty("mail.imaps.auth.mechanisms", "XOAUTH2");
        Session session = Session.getInstance(properties, authenticator);
        session.setDebug(conf.isDebug());

        return session;
    }

    @Bean
    public RouteBuilder routeBuilder(){
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("imaps://outlook.office365.com:993?" +
                        "session=#mailSession&" +
                        "delay="+conf.getPollInterval()+"&" +
                        "delete="+conf.isDelete()+"&"+
                        "bridgeErrorHandler=true")
                        .id("camel-mail-ms-exchange")
                        .log(LoggingLevel.INFO, "message Received: \nFrom: ${header.from}\nSubj: ${header.subject}\nBody:\n${body}");
            }
        };
    }
}
