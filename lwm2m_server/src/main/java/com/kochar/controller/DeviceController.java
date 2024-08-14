package com.kochar.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.registration.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private LeshanServer server;

    @GetMapping("/list")
    public List<String> listAllDevices() {
        List<String> devices = new ArrayList<>();
        Iterator<Registration> iterator = server.getRegistrationService().getAllRegistrations();
        while (iterator.hasNext()) {
            Registration registration = iterator.next();
            devices.add("ID: " + registration.getId() + ", Endpoint: " + registration.getEndpoint());
        }
        return devices;
    }
}
