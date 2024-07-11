package com.kochar;

import java.util.Collection;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.core.request.ExecuteRequest;
import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.californium.LeshanServerBuilder;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationListener;
import org.eclipse.leshan.server.registration.RegistrationUpdate;

public class SimpleLeshanServer {
    public static void main(String[] args) {
        // Create and configure Leshan server builder
        LeshanServerBuilder builder = new LeshanServerBuilder();

        // Build and start the server
        LeshanServer server = builder.build();
        server.start();

        // Add a registration listener to print when devices connect or leave the server
        server.getRegistrationService().addListener(new RegistrationListener() {
            @Override
            public void registered(Registration registration, Registration previousReg, Collection<Observation> previousObservations) {
                System.out.println("New device: " + registration.getEndpoint());
                try {
                    ReadResponse response = server.send(registration, new ReadRequest(3, 0, 13));
                    if (response.isSuccess()) {
                        System.out.println("Device time: " + ((LwM2mResource) response.getContent()).getValue());
                    } else {
                        System.out.println("Failed to read: " + response.getCode() + " " + response.getErrorMessage());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void updated(RegistrationUpdate update, Registration updatedReg, Registration previousReg) {
                System.out.println("Device is still here: " + updatedReg.getEndpoint());
            }

            @Override
            public void unregistered(Registration registration, Collection<Observation> observations, boolean expired, Registration newReg) {
                System.out.println("Device left: " + registration.getEndpoint());
            }
        });

        System.out.println("Leshan server started...");

        // Create a thread to handle user input
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String command = scanner.nextLine();
                if (command.equalsIgnoreCase("listall")) {
                    listAllDevices(server);
                } else if (command.startsWith("reboot ")) {
                    String endpoint = command.substring(7).trim();
                    rebootDevice(server, endpoint);
                }
            }
        });
    }

    private static void listAllDevices(LeshanServer server) {
        System.out.println("Listing all registered devices:");
        Iterator<Registration> iterator = server.getRegistrationService().getAllRegistrations();
        while (iterator.hasNext()) {
            Registration registration = iterator.next();
            System.out.println("Device: " + registration.getEndpoint());
        }
    }

    private static void rebootDevice(LeshanServer server, String endpoint) {
        Registration registration = server.getRegistrationService().getByEndpoint(endpoint);
        if (registration != null) {
            try {
                ExecuteResponse response = server.send(registration, new ExecuteRequest(3, 0, 4));
                if (response.isSuccess()) {
                    System.out.println("Device " + endpoint + " rebooted successfully.");
                } else {
                    System.out.println("Failed to reboot device " + endpoint + ": " + response.getCode() + " " + response.getErrorMessage());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Device " + endpoint + " not found.");
        }
    }
}
