package com.kochar;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.client.object.Security;
import org.eclipse.leshan.client.object.Server;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.LwM2mId;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.LwM2mSingleResource;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;

public class SimpleLeshanClient {

    private static final String SERVER_URI = "coap://192.168.105.180:5683";
    private static LeshanClient client;
    private static final Map<Integer, CustomDevice> devices = new HashMap<>();
    private static int nextInstanceId = 1;

    public static void main(String[] args) {
        String endpoint = "KocharTech";
        System.out.println("Starting Leshan client with endpoint: " + endpoint + " and server URI: " + SERVER_URI);

        LeshanClientBuilder builder = new LeshanClientBuilder(endpoint);
        ObjectsInitializer initializer = getCustomObjects(SERVER_URI, 12345);
        builder.setObjects(initializer.createAll());

        client = builder.build();
        client.start();

        System.out.println("Leshan client started. Endpoint: " + endpoint);
        printHelp();                                                                                                

        // Command-line interface
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter command: ");
            String command = scanner.nextLine().trim();
            String[] parts = command.split(" ");
            if (parts[0].equalsIgnoreCase("list")) {
                listObjects();
            } else if (parts[0].equalsIgnoreCase("create") && parts.length == 2) {
                createObject(parts[1]);
            } else if (parts[0].equalsIgnoreCase("delete") && parts.length == 2) {
                deleteObject(Integer.parseInt(parts[1]));
            } else if (parts[0].equalsIgnoreCase("update") && parts.length == 4) {
                updateObject(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), parts[3]);
            } else if (parts[0].equalsIgnoreCase("collect") && parts.length == 2) {
                collectResources(Integer.parseInt(parts[1]));
            } else if (parts[0].equalsIgnoreCase("move") && parts.length == 3) {
                moveObject(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            } else if (parts[0].equalsIgnoreCase("help")) {
                printHelp();
            } else if (parts[0].equalsIgnoreCase("exit")) {
                client.stop(true);
                System.exit(0);
            } else {
                System.out.println("Unknown command: " + command);
            }
        }
    }

    private static ObjectsInitializer getCustomObjects(String serverUri, int clientId) {
        ObjectsInitializer initializer = new ObjectsInitializer();

        System.out.println("Setting up Security and Server objects with server URI: " + serverUri);

        initializer.setInstancesForObject(LwM2mId.SECURITY, Security.noSec(serverUri, clientId));
        initializer.setInstancesForObject(LwM2mId.SERVER, new Server(clientId, 5 * 60, BindingMode.U, false));

        // Create and add instances of CustomDevice
        CustomDevice customDevice = new CustomDevice();
        devices.put(LwM2mId.DEVICE, customDevice);
        initializer.setInstancesForObject(LwM2mId.DEVICE, customDevice);

        return initializer;
    }

    // Custom method to list all objects, instances, and resources
    private static void listObjects() {
        System.out.println("Listing all objects, instances, and resources:");

        // List devices
        for (Map.Entry<Integer, CustomDevice> entry : devices.entrySet()) {
            Integer objectId = entry.getKey();
            CustomDevice device = entry.getValue();
            System.out.println("Object ID: " + objectId);

            // For CustomDevice, we know it has only one instance (ID 0 in this example)
            System.out.println("  Instance ID: 0");

            // Get resources from the instance
            Map<Integer, String> resources = device.getResources();
            for (Map.Entry<Integer, String> resourceEntry : resources.entrySet()) {
                System.out.println("    Resource ID: " + resourceEntry.getKey() + ", Value: " + resourceEntry.getValue());
            }
        }
    }

    private static void createObject(String objectType) {
        switch (objectType.toLowerCase()) {
            case "device":
                // Create a new instance of CustomDevice
                CustomDevice newDevice = new CustomDevice();
                devices.put(nextInstanceId++, newDevice);
                System.out.println("Created new device instance with ID: " + (nextInstanceId - 1));
                break;
            default:
                System.out.println("Unknown object type: " + objectType);
                break;
        }
    }

    private static void deleteObject(int objectId) {
        if (devices.containsKey(objectId)) {
            devices.remove(objectId);
            System.out.println("Deleted device instance with ID: " + objectId);
        } else {
            System.out.println("No device instance found with ID: " + objectId);
        }
    }

    private static void updateObject(int objectId, int resourceId, String value) {
        CustomDevice device = devices.get(objectId);
        if (device != null) {
            device.updateResource(resourceId, value);
            System.out.println("Updated device instance with ID: " + objectId);
        } else {
            System.out.println("No device instance found with ID: " + objectId);
        }
    }

    private static void collectResources(int objectId) {
        CustomDevice device = devices.get(objectId);
        if (device != null) {
            device.collectResources();
            System.out.println("Collected resources for device instance with ID: " + objectId);
        } else {
            System.out.println("No device instance found with ID: " + objectId);
        }
    }

    private static void moveObject(int fromObjectId, int toObjectId) {
        CustomDevice fromDevice = devices.get(fromObjectId);
        CustomDevice toDevice = devices.get(toObjectId);
        if (fromDevice != null && toDevice != null) {
            fromDevice.moveTo(toDevice);
            System.out.println("Moved resources from device instance with ID: " + fromObjectId + " to device instance with ID: " + toObjectId);
        } else {
            System.out.println("Invalid object IDs provided for move operation.");
        }
    }

    private static void printHelp() {
        System.out.println("Commands:");
        System.out.println("  list - List all objects, instances, and resources.");
        System.out.println("  create <objectType> - Create a new instance of the specified object type.");
        System.out.println("  delete <objectId> - Delete the instance with the specified object ID.");
        System.out.println("  update <objectId> <resourceId> <value> - Update the resource with the specified ID and value.");
        System.out.println("  collect <objectId> - Collect resources for the specified object ID.");
        System.out.println("  move <fromObjectId> <toObjectId> - Move resources from one object to another.");
        System.out.println("  help - Display this help message.");
        System.out.println("  exit - Exit the client.");
    }
}

class CustomDevice extends BaseInstanceEnabler {
    private Map<Integer, String> resources = new HashMap<>();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public CustomDevice() {
        // Initialize your resources and set their default values
        resources.put(0, "Example String");
        resources.put(1, "1234"); // Example integer as a string

        // Start a periodic task to simulate real-time data changes
        scheduler.scheduleAtFixedRate(() -> {
            // Update resources and notify the server
            updateResource(1, String.valueOf((int) (Math.random() * 1000)));
        }, 0, 5, TimeUnit.SECONDS); // Adjust the time interval as needed
    }

    public void updateResource(int resourceId, String value) {
        resources.put(resourceId, value);
        fireResourcesChange(resourceId); // Notify the server of the change
    }

    @Override
    public ReadResponse read(ServerIdentity identity, int resourceId) {
        String value = resources.get(resourceId);
        if (value != null) {
            return ReadResponse.success(LwM2mSingleResource.newStringResource(resourceId, value));
        } else {
            return ReadResponse.notFound();
        }
    }

    @Override
    public WriteResponse write(ServerIdentity identity, int resourceId, LwM2mResource value) {
        // Handle write requests from the server if needed
        return WriteResponse.success();
    }

    public Map<Integer, String> getResources() {
        return resources;
    }

    public void collectResources() {
        // Implement your logic for collecting resources from the device
        System.out.println("Collecting resources...");
        // Example: Iterate over resources and perform some operation
        for (Map.Entry<Integer, String> entry : resources.entrySet()) {
            System.out.println("Resource ID: " + entry.getKey() + ", Value: " + entry.getValue());
        }
    }

    public void moveTo(CustomDevice targetDevice) {
        // Implement your logic for moving resources to another device
        System.out.println("Moving resources...");
        // Example: Iterate over resources and move them to the target device
        for (Map.Entry<Integer, String> entry : resources.entrySet()) {
            targetDevice.updateResource(entry.getKey(), entry.getValue());
        }
        // Clear resources from the current device
        resources.clear();
    }
}
