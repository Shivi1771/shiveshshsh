package com.kochar;

import java.util.Collection;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.leshan.core.node.LwM2mObjectInstance;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.LwM2mSingleResource;
import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.core.request.CreateRequest;
import org.eclipse.leshan.core.request.DeleteRequest;
import org.eclipse.leshan.core.request.ExecuteRequest;
import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.request.WriteRequest;
import org.eclipse.leshan.core.response.CreateResponse;
import org.eclipse.leshan.core.response.DeleteResponse;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.californium.LeshanServerBuilder;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationListener;
import org.eclipse.leshan.server.registration.RegistrationUpdate;

public class SimpleLeshanServer {
    private static LeshanServer server;

    public static void main(String[] args) {
        LeshanServerBuilder builder = new LeshanServerBuilder();
        server = builder.build();
        server.start();

        server.getRegistrationService().addListener(new RegistrationListener() {
            @Override
            public void registered(Registration registration, Registration previousReg, Collection<Observation> previousObservations) {
                System.out.println("New device registered: " + registration.getEndpoint());
            }

            @Override
            public void updated(RegistrationUpdate update, Registration updatedReg, Registration previousReg) {
                System.out.println("Device updated: " + updatedReg.getEndpoint());
            }

            @Override
            public void unregistered(Registration registration, Collection<Observation> observations, boolean expired, Registration newReg) {
                System.out.println("Device unregistered: " + registration.getEndpoint());
            }
        });

        System.out.println("Leshan server started...");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String command = scanner.nextLine();
                if (command.equalsIgnoreCase("listall")) {
                    listAllDevices(server);
                } else if (command.equalsIgnoreCase("reboot")) {
                    rebootDevice();
                } else if (command.equalsIgnoreCase("rebootserver")) {
                    rebootServer();
                } else if (command.startsWith("deregister ")) {
                    String[] parts = command.split(" ");
                    if (parts.length == 2) {
                        deregisterDevice(parts[1]);
                    } else {
                        System.out.println("Invalid command format. Use 'deregister <endpoint>'");
                    }
                } else if (command.startsWith("read ")) {
                    String[] parts = command.split(" ");
                    if (parts.length == 3) {
                        readResource(parts[1], parts[2]);
                    } else {
                        System.out.println("Invalid command format. Use 'read <endpoint> <resource_path>'");
                    }
                } else if (command.startsWith("write ")) {
                    String[] parts = command.split(" ", 4);
                    if (parts.length == 4) {
                        writeResource(parts[1], parts[2], parts[3]);
                    } else {
                        System.out.println("Invalid command format. Use 'write <endpoint> <resource_path> <value>'");
                    }
                } else if (command.startsWith("create ")) {
                    String[] parts = command.split(" ", 4);
                    if (parts.length == 4) {
                        createInstance(parts[1], parts[2], parts[3]);
                    } else {
                        System.out.println("Invalid command format. Use 'create <endpoint> <objectId> <instanceId>'");
                    }
                } else if (command.startsWith("delete ")) {
                    String[] parts = command.split(" ", 3);
                    if (parts.length == 3) {
                        deleteInstance(parts[1], parts[2]);
                    } else {
                        System.out.println("Invalid command format. Use 'delete <endpoint> <objectId/instanceId>'");
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
                ExecuteRequest request = new ExecuteRequest("/3/0/4"); // Adjust as per your device's resource path
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

    private static void rebootServer() {
        System.out.println("Rebooting the server...");
        Thread rebootThread = new Thread(() -> {
            try {
                server.stop();
                Thread.sleep(1000); // Delay for simulation
                server.start();
                System.out.println("Server rebooted successfully.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        rebootThread.start();
    }

    private static void deregisterDevice(String endpoint) {
        System.out.println("Triggering deregistration for device: " + endpoint);
        Registration registration = server.getRegistrationService().getByEndpoint(endpoint);

        if (registration != null) {
            try {
                ExecuteRequest request = new ExecuteRequest("/3/0/5"); // Adjust as per your device's resource path
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

    private static void readResource(String endpoint, String resourcePath) {
        System.out.println("Reading resource " + resourcePath + " from device: " + endpoint);
        Registration registration = server.getRegistrationService().getByEndpoint(endpoint);

        if (registration != null) {
            try {
                ReadRequest request = new ReadRequest(resourcePath);
                ReadResponse response = server.send(registration, request);

                if (response.isSuccess()) {
                    System.out.println("Read response from " + endpoint + ": " + response.getContent().toString());
                } else {
                    System.out.println("Failed to read resource: " + response.getCode() + " " + response.getErrorMessage());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No device found with endpoint: " + endpoint);
        }
    }

    private static void writeResource(String endpoint, String resourcePath, String value) {
        System.out.println("Writing value '" + value + "' to resource " + resourcePath + " on device: " + endpoint);
        Registration registration = server.getRegistrationService().getByEndpoint(endpoint);

        if (registration != null) {
            try {
                // Convert the value to an LwM2mNode with the appropriate content format
                WriteRequest request = new WriteRequest(null, null, resourcePath, null, value);
                WriteResponse response = server.send(registration, request);

                if (response.isSuccess()) {
                    System.out.println("Write response from " + endpoint + ": " + response.getCode().toString());
                } else {
                    System.out.println("Failed to write resource: " + response.getCode() + " " + response.getErrorMessage());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No device found with endpoint: " + endpoint);
        }
    }

    private static void createInstance(String endpoint, String objectId, String instanceId) {
        System.out.println("Creating instance " + instanceId + " for object " + objectId + " on device: " + endpoint);
        Registration registration = server.getRegistrationService().getByEndpoint(endpoint);

        if (registration != null) {
            try {
                // Adjust the resources and values as per your specific object requirements
                LwM2mResource resource = LwM2mSingleResource.newStringResource(0, "NewValue"); // Example single resource
                LwM2mObjectInstance instance = new LwM2mObjectInstance(Integer.parseInt(instanceId), resource);
                CreateRequest request = new CreateRequest("/" + objectId, instance);
                CreateResponse response = server.send(registration, request);

                if (response.isSuccess()) {
                    System.out.println("Create instance response from " + endpoint + ": " + response.getCode().toString());
                } else {
                    System.out.println("Failed to create instance: " + response.getCode() + " " + response.getErrorMessage());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No device found with endpoint: " + endpoint);
        }
    }

    private static void deleteInstance(String endpoint, String objectPath) {
        System.out.println("Deleting instance at " + objectPath + " from device: " + endpoint);
        Registration registration = server.getRegistrationService().getByEndpoint(endpoint);

        if (registration != null) {
            try {
                DeleteRequest request = new DeleteRequest(objectPath);
                DeleteResponse response = server.send(registration, request);

                if (response.isSuccess()) {
                    System.out.println("Delete response from " + endpoint + ": " + response.getCode().toString());
                } else {
                    System.out.println("Failed to delete instance: " + response.getCode() + " " + response.getErrorMessage());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No device found with endpoint: " + endpoint);
        }
    }
}
