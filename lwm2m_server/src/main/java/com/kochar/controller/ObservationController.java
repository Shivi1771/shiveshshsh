package com.kochar.controller;

import org.eclipse.leshan.core.request.ObserveRequest;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.registration.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devices")
public class ObservationController {

    @Autowired
    private LeshanServer server;

    @PostMapping("/observe")
    public String observeDevice(@RequestParam String endpoint, @RequestParam String resourcePath) {
        Registration registration = server.getRegistrationService().getByEndpoint(endpoint);

        if (registration != null) {
            try {
                ObserveRequest request = new ObserveRequest(resourcePath);
                ObserveResponse response = server.send(registration, request);

                if (response.isSuccess()) {
                    return "Observation started successfully.";
                } else {
                    return "Failed to start observation: " + response.getCode() + " " + response.getErrorMessage();
                }
            } catch (InterruptedException e) {
                return "Error: " + e.getMessage();
            }
        } else {
            return "No device found with endpoint: " + endpoint;
        }
    }
}
