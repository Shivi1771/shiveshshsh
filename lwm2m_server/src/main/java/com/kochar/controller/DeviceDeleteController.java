package com.kochar.controller;

import org.eclipse.leshan.core.request.DeleteRequest;
import org.eclipse.leshan.core.response.DeleteResponse;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.registration.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device")
public class DeviceDeleteController {

    @Autowired
    private LeshanServer server;

    @DeleteMapping("/delete/{deviceId}/{objectId}/{instanceId}")
    public String deleteResource(@PathVariable String deviceId,
                                 @PathVariable int objectId,
                                 @PathVariable int instanceId) {
        Registration registration = server.getRegistrationService().getById(deviceId);
        if (registration == null) {
            return "Device not found";
        }

        // Create the DeleteRequest
        String path = "/" + objectId + "/" + instanceId;
        DeleteRequest request = new DeleteRequest(path);

        try {
            // Send the request to the device
            DeleteResponse response = server.send(registration, request);

            // Check the response and return appropriate message
            if (response.isSuccess()) {
                return "Resource deleted successfully";
            } else {
                return "Failed to delete resource. Error: " + response.getErrorMessage();
            }
        } catch (InterruptedException e) {
            // Handle the InterruptedException
            Thread.currentThread().interrupt(); // Restore interrupted status
            return "Failed to delete resource due to interruption";
        }
    }
}
