package com.kochar;

import java.util.Collection;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.observation.CompositeObservation;
import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.core.observation.SingleObservation;
import org.eclipse.leshan.core.request.CreateRequest;
import org.eclipse.leshan.core.request.DeleteRequest;
import org.eclipse.leshan.core.request.DiscoverRequest;
import org.eclipse.leshan.core.request.ExecuteRequest;
import org.eclipse.leshan.core.request.ObserveRequest;
import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.request.WriteRequest;
import org.eclipse.leshan.core.response.CreateResponse;
import org.eclipse.leshan.core.response.DeleteResponse;
import org.eclipse.leshan.core.response.DiscoverResponse;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ObserveCompositeResponse;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.californium.LeshanServerBuilder;
import org.eclipse.leshan.server.observation.ObservationListener;
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

        server.getObservationService().addListener(new ObservationListener() {
            @Override
            public void newObservation(Observation observation, Registration registration) {
                System.out.println("New observation added: " + observation);
            }

            @Override
            public void cancelled(Observation observation) {
                System.out.println("Observation cancelled: " + observation);
            }

            @Override
            public void onResponse(SingleObservation observation, Registration registration, ObserveResponse response) {
                System.out.println("Single observation response: " + response.getContent());
            }

            @Override
            public void onResponse(CompositeObservation observation, Registration registration, ObserveCompositeResponse response) {
                System.out.println("Composite observation response received.");
                System.out.println("Response details: " + response.toString());
            }

            @Override
            public void onError(Observation observation, Registration registration, Exception error) {
                System.err.println("Observation error: " + error.getMessage());
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
                } else if (command.equalsIgnoreCase("observe")) {
                    observeDevice();
                } else if (command.equalsIgnoreCase("cancelobserve")) {
                    cancelObservation();
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
                } else if (command.startsWith("discover ")) {
                    String[] parts = command.split(" ", 3);
                    if (parts.length == 3) {
                        discoverResources(parts[1], parts[2]);
                    } else {
                        System.out.println("Invalid command format. Use 'discover <endpoint> <resource_path>'");
                    }
                }
            }
        });
    }

    private static void createInstance(String endpoint, String objectId, String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createInstance'");
    }

    // Method to cancel an observation
    private static void cancelObservation() {
        System.out.println("Enter the endpoint of the device to cancel observation:");
        Scanner scanner = new Scanner(System.in);
        String endpoint = scanner.nextLine();
        Registration registration = server.getRegistrationService().getByEndpoint(endpoint);

        if (registration != null) {
            System.out.println("Enter the resource path to cancel observation (e.g., /3/0/13 for Time):");
            String resourcePath = scanner.nextLine();
            try {
                // Retrieve all observations for this registration
                Collection<Observation> observations = server.getObservationService().getObservations(registration);
                Observation targetObservation = null;

                // Find the specific observation for the given resource path
                for (Observation observation : observations) {
                    if (((SingleObservation) observation).getPath().toString().equals(resourcePath)) {
                        targetObservation = observation;
                        break;
                    }
                }

                if (targetObservation != null) {
                    // Cancel the observation
                    server.getObservationService().cancelObservation(targetObservation);
                    System.out.println("Observation cancelled successfully on " + endpoint);
                } else {
                    System.out.println("No active observation found for resource: " + resourcePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Failed to cancel observation due to an error: " + e.getMessage());
            }
        } else {
            System.out.println("No device found with endpoint: " + endpoint);
        }
    }

    private static void createInstance(String endpoint, String objectId, Collection<LwM2mResource> parts) {
        System.out.println("Creating new instance " + parts + " for object " + objectId + " on device " + endpoint);
        Registration registration = server.getRegistrationService().getByEndpoint(endpoint);

        if (registration != null) {
            try {
                CreateRequest request = new CreateRequest(objectId, parts);
                CreateResponse response = server.send(registration, request);

                if (response.isSuccess()) {
                    System.out.println("Instance created successfully.");
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

    // Method to list all registered devices
    private static void listAllDevices(LeshanServer server) {
        System.out.println("Listing all registered devices:");
        Iterator<Registration> iterator = server.getRegistrationService().getAllRegistrations();
        while (iterator.hasNext()) {
            Registration registration = iterator.next();
            System.out.println("Device: " + registration.getEndpoint());
        }
    }

    // Method to observe a device resource for real-time monitoring
    private static void observeDevice() {
        System.out.println("Enter the endpoint of the device to observe:");
        Scanner scanner = new Scanner(System.in);
        String endpoint = scanner.nextLine();
        Registration registration = server.getRegistrationService().getByEndpoint(endpoint);

        if (registration != null) {
            System.out.println("Enter the resource path to observe (e.g., /3/0/13 for Time):");
            String resourcePath = scanner.nextLine();
            try {
                ObserveRequest request = new ObserveRequest(resourcePath);
                ObserveResponse response = server.send(registration, request);

                if (response.isSuccess()) {
                    System.out.println("Observation started successfully.");
                } else {
                    System.out.println("Failed to start observation: " + response.getCode() + " " + response.getErrorMessage());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No device found with endpoint: " + endpoint);
        }
    }

    // Method to reboot a device
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


    // Method to reboot the server
    private static void rebootServer() {
        System.out.println("Rebooting the server...");
        server.stop();
        server.start();
        System.out.println("Server rebooted successfully.");
    }

    // Method to deregister a device
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

    // Method to read a resource from a device
    private static void readResource(String endpoint, String resourcePath) {
        System.out.println("Reading resource " + resourcePath + " from device " + endpoint);
        Registration registration = server.getRegistrationService().getByEndpoint(endpoint);

        if (registration != null) {
            try {
                ReadRequest request = new ReadRequest(resourcePath);
                ReadResponse response = server.send(registration, request);

                if (response.isSuccess()) {
                    System.out.println("Resource value: " + response.getContent());
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

    // Method to write a value to a resource on a device
    private static void writeResource(String endpoint, String resourcePath, String value) {
        System.out.println("Writing value " + value + " to resource " + resourcePath + " on device " + endpoint);
        Registration registration = server.getRegistrationService().getByEndpoint(endpoint);

        if (registration != null) {
            try {
                WriteRequest request = new WriteRequest(null, null, resourcePath, null, value);
                WriteResponse response = server.send(registration, request);

                if (response.isSuccess()) {
                    System.out.println("Resource value written successfully.");
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

    // Method to delete an instance on a device
    private static void deleteInstance(String endpoint, String objectId) {
        System.out.println("Deleting instance " + objectId + " from device " + endpoint);
        Registration registration = server.getRegistrationService().getByEndpoint(endpoint);

        if (registration != null) {
            try {
                DeleteRequest request = new DeleteRequest(objectId);
                DeleteResponse response = server.send(registration, request);

                if (response.isSuccess()) {
                    System.out.println("Instance deleted successfully.");
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

    // Method to discover resources on a device
    private static void discoverResources(String endpoint, String resourcePath) {
        System.out.println("Discovering resources at " + resourcePath + " on device " + endpoint);
        Registration registration = server.getRegistrationService().getByEndpoint(endpoint);

        if (registration != null) {
            try {
                DiscoverRequest request = new DiscoverRequest(resourcePath);
                DiscoverResponse response = server.send(registration, request);

                if (response.isSuccess()) {
                    System.out.println("Discovered resources: " + response.getCode());
                } else {
                    System.out.println("Failed to discover resources: " + response.getCode() + " " + response.getErrorMessage());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No device found with endpoint: " + endpoint);
        }
    }
}
