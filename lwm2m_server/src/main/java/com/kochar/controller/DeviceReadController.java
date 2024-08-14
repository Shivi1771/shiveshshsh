package com.kochar.controller;

import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.registration.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/device")
public class DeviceReadController {

    @Autowired
    private LeshanServer server;

    @GetMapping("/read/{deviceId}/{resourceId}")
    public String readResource(@PathVariable String deviceId, @PathVariable int resourceId) {
        Registration registration = server.getRegistrationService().getById(deviceId);
        if (registration == null) {
            return "Device not found";
        }

        // Create a ReadRequest for the specified resource ID
        ReadRequest request = new ReadRequest("/3/0/" + resourceId);

        try {
            // Send the request to the device
            ReadResponse response = server.send(registration, request);

            // Check the response and return appropriate message
            if (response.isSuccess()) {
                return "Resource value: " + response.getContent();
            } else {
                return "Failed to read resource. Error: " + response.getErrorMessage();
            }
        } catch (InterruptedException e) {
            // Handle the InterruptedException
            Thread.currentThread().interrupt(); // Restore interrupted status
            return "Failed to read resource due to interruption";
        }
    }
}
