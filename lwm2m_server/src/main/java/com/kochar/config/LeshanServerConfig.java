package com.kochar.config;

import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.californium.LeshanServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LeshanServerConfig {

    @Value("${coap.server.port:5683}")  // Default port is 5683 if not specified
    private int coapPort;

    @Bean
    public LeshanServer leshanServer() {
        // Create and configure LeshanServer using LeshanServerBuilder
        LeshanServerBuilder builder = new LeshanServerBuilder();
        
        // Set the port for the CoAP server
        builder.setLocalAddress(null, coapPort);
        
        // Build the LeshanServer
        LeshanServer leshanServer = builder.build();
        
        // Start the LeshanServer
        leshanServer.start();
        
        return leshanServer;
    }
}
