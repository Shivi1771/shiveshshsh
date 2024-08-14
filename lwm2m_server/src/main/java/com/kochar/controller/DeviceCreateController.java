package com.kochar.controller;

import java.util.List;

import org.eclipse.leshan.core.node.LwM2mObjectInstance;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.request.CreateRequest;
import org.eclipse.leshan.core.response.CreateResponse;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.registration.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/device")
public class DeviceCreateController {

    @Autowired
    private LeshanServer server;

    @PostMapping("/create/{deviceId}/{objectId}")
    public String createResource(@PathVariable String deviceId,
                                 @PathVariable int objectId,
                                 @RequestBody List<LwM2mResource> resources) {
        Registration registration = server.getRegistrationService().getById(deviceId);
        if (registration == null) {
            return "Device not found";
        }

        // Create an object instance with the provided resources
        LwM2mObjectInstance objectInstance = new LwM2mObjectInstance(objectId, resources);

        // Create the CreateRequest using LwM2mObjectInstance
        CreateRequest request;
        try {
            request = new CreateRequest("/" + objectId, objectInstance);
        } catch (Exception e) {
            return "Error creating CreateRequest: " + e.getMessage();
        }

        try {
            // Send the request to the device
            CreateResponse response = server.send(registration, request);

            // Check the response and return appropriate message
            if (response.isSuccess()) {
                return "Resource created successfully";
            } else {
                return "Failed to create resource. Error: " + response.getErrorMessage();
            }
        } catch (InterruptedException e) {
            // Handle the InterruptedException
            Thread.currentThread().interrupt(); // Restore interrupted status
            return "Failed to create resource due to interruption";
        }
    }
}
