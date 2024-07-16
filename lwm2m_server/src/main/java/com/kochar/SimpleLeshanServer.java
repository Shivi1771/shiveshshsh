package com.kochar;


import java.util.Collection;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.core.request.ExecuteRequest;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.californium.LeshanServerBuilder;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationListener;
import org.eclipse.leshan.server.registration.RegistrationUpdate;

public class SimpleLeshanServer {
    private static LeshanServer server;

    public static void main(String[] args) {
        // Create and configure Leshan server builder
        LeshanServerBuilder builder = new LeshanServerBuilder();

        // Build and start the server
        server = builder.build();
        server.start();

        // Add a registration listener to print when devices connect or leave the server
        server.getRegistrationService().addListener(new RegistrationListener() {
            @Override
            public void registered(Registration registration, Registration previousReg, Collection<Observation> previousObservations) {
                System.out.println("New device: " + registration.getEndpoint());
            }

            @Override
            public void updated(RegistrationUpdate update, Registration updatedReg, Registration previousReg) {
                System.out.println("Device updated: " + updatedReg.getEndpoint());
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
                } else if (command.equalsIgnoreCase("reboot")) {
                    rebootDevice();
                } else if (command.startsWith("deregister ")) {
                    String[] parts = command.split(" ");
                    if (parts.length == 2) {
                        deregisterDevice(parts[1]); // Trigger deregistration for the specified device
                    } else {
                        System.out.println("Invalid command format. Use 'deregister <endpoint>'");
                    }
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

    private static void rebootDevice() {
        System.out.println("Enter the endpoint of the device to reboot:");
        Scanner scanner = new Scanner(System.in);
        String endpoint = scanner.nextLine();
        Registration registration = server.getRegistrationService().getByEndpoint(endpoint);

        if (registration != null) {
            try {
                ExecuteRequest request = new ExecuteRequest(3, 0, 4); // Reboot resource ID is 4
                ExecuteResponse response = server.send(registration, request);

                if (response.isSuccess()) {
                    System.out.println("Reboot command sent successfully to " + endpoint);
                } else {
                    System.out.println("Failed to send reboot command: " + response.getCode() + " " + response.getErrorMessage());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No device found with endpoint: " + endpoint);
        }
    }

    private static void deregisterDevice(String endpoint) {
        System.out.println("Triggering deregistration for device: " + endpoint);
        Registration registration = server.getRegistrationService().getByEndpoint(endpoint);

        if (registration != null) {
            try {
                // Example: Send execute request to trigger custom deregistration on the device
                ExecuteRequest request = new ExecuteRequest(3, 0, 5); // Assuming resource ID 5 is for deregistration
                ExecuteResponse response = server.send(registration, request);

                if (response.isSuccess()) {
                    System.out.println("Deregistration command sent successfully to " + endpoint);
                } else {
                    System.out.println("Failed to send deregistration command: " + response.getCode() + " " + response.getErrorMessage());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No device found with endpoint: " + endpoint);
        }
    }
}
