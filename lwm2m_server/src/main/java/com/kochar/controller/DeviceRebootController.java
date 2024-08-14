package com.kochar.controller;

import org.eclipse.leshan.core.request.ExecuteRequest;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.registration.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/device")
public class DeviceRebootController {

    @Autowired
    private LeshanServer server;

    @PostMapping("/reboot/{deviceId}")
    public String rebootDevice(@PathVariable String deviceId) {
        Registration registration = server.getRegistrationService().getById(deviceId);
        if (registration == null) {
            return "Device not found";
        }

        // Create an ExecuteRequest for the reboot operation (typically /3/0/4 for reboot)
        ExecuteRequest request = new ExecuteRequest("/3/0/4");

        try {
            // Send the request to the device
            ExecuteResponse response = server.send(registration, request);

            return response.isSuccess() ? "Reboot command sent successfully" : "Failed to send reboot command";
        } catch (InterruptedException e) {
            // Handle the InterruptedException
            Thread.currentThread().interrupt(); // Restore interrupted status
            return "Failed to send reboot command due to interruption";
        }
    }
}
