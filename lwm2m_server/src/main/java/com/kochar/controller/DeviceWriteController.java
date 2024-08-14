package com.kochar.controller;

import org.eclipse.leshan.core.request.WriteRequest;
import org.eclipse.leshan.core.response.WriteResponse;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.registration.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/device")
public class DeviceWriteController {

    @Autowired
    private LeshanServer server;

    @PostMapping("/write/{deviceId}/{objectId}/{instanceId}/{resourceId}")
    public String writeResource(@PathVariable String deviceId,
                                @PathVariable int objectId,
                                @PathVariable int instanceId,
                                @PathVariable int resourceId,
                                @RequestParam String value) {
        Registration registration = server.getRegistrationService().getById(deviceId);
        if (registration == null) {
            return "Device not found";
        }

        // Create the WriteRequest
        String path = "/" + objectId + "/" + instanceId + "/" + resourceId;
        WriteRequest request = new WriteRequest(null, null, path, null, value);

        try {
            // Send the request to the device
            WriteResponse response = server.send(registration, request);

            // Check the response and return appropriate message
            if (response.isSuccess()) {
                return "Resource written successfully";
            } else {
                return "Failed to write resource. Error: " + response.getErrorMessage();
            }
        } catch (InterruptedException e) {
            // Handle the InterruptedException
            Thread.currentThread().interrupt(); // Restore interrupted status
            return "Failed to write resource due to interruption";
        }
    }
}
