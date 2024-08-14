package com.kochar.controller;

import org.eclipse.leshan.core.request.ExecuteRequest;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devices")
public class DeviceDeregisterController {

    @Autowired
    private LeshanServer server;

    @DeleteMapping("/deregister/{endpoint}")
    public String deregisterDevice(@PathVariable String endpoint) {
        System.out.println("Triggering deregistration for device: " + endpoint);
        RegistrationService registrationService = server.getRegistrationService();
        Registration registration = registrationService.getByEndpoint(endpoint);

        if (registration != null) {
            try {
                // Create and send the execute request
                ExecuteRequest request = new ExecuteRequest(3, 0, 5); // Assuming resource ID 5 is for deregistration
                ExecuteResponse response = server.send(registration, request);

                if (response.isSuccess()) {
                    return "Deregistration command sent successfully to " + endpoint;
                } else {
                    return "Failed to send deregistration command: " + response.getCode() + " " + response.getErrorMessage();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return "Error occurred while sending deregistration command.";
            }
        } else {
            return "No device found with endpoint: " + endpoint;
        }
    }
}
