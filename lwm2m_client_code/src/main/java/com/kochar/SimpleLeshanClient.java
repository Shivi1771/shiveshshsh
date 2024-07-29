package com.kochar;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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
import org.eclipse.leshan.core.response.ExecuteResponse;
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
            System.out.println("Deleted object with ID: " + objectId);
        } else {
            System.out.println("No object found with ID: " + objectId);
        }
    }

    private static void updateObject(int objectId, int resourceId, String newValue) {
        CustomDevice device = devices.get(objectId);
        if (device != null) {
            // Create an instance of LwM2mSingleResource
            LwM2mSingleResource resource = LwM2mSingleResource.newStringResource(resourceId, newValue);
            WriteResponse response = device.write(null, resourceId, resource);
            if (response.isSuccess()) {
                System.out.println("Updated resource " + resourceId + " of object with ID: " + objectId);
            } else {
                System.out.println("Failed to update resource " + resourceId + " of object with ID: " + objectId);
            }
        } else {
            System.out.println("No object found with ID: " + objectId);
        }
    }

    private static void collectResources(int objectId) {
        CustomDevice device = devices.get(objectId);
        if (device != null) {
            // Collect all resources of the device into a single resource
            Map<Integer, String> resources = device.getResources();
            // Create a collected resource data
            StringBuilder collectedData = new StringBuilder();
            for (Map.Entry<Integer, String> entry : resources.entrySet()) {
                collectedData.append("Resource ID ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }

            // Create a LwM2mSingleResource to hold collected data
            LwM2mSingleResource collectedResource = LwM2mSingleResource.newStringResource(9999, collectedData.toString());

            // Use the collected resource data (e.g., print it)
            System.out.println("Collected resources for object ID " + objectId + ":");
            System.out.println(collectedData.toString());
        } else {
            System.out.println("No object found with ID: " + objectId);
        }
    }

    private static void moveObject(int fromObjectId, int toObjectId) {
        CustomDevice device = devices.get(fromObjectId);
        if (device != null) {
            // Move the device to the new ID
            devices.remove(fromObjectId);
            devices.put(toObjectId, device);
            System.out.println("Moved object from ID " + fromObjectId + " to ID " + toObjectId);
        } else {
            System.out.println("No object found with ID: " + fromObjectId);
        }
    }

    public static class CustomDevice extends BaseInstanceEnabler {
        private final Map<Integer, String> resources = new HashMap<>();

        public CustomDevice() {
            resources.put(0, "Kochar_C1");
            resources.put(1, "Model_A1");
        }

        @Override
        public ReadResponse read(ServerIdentity identity, int resourceId) {
            if (resources.containsKey(resourceId)) {
                return ReadResponse.success(resourceId, resources.get(resourceId));
            } else {
                return ReadResponse.notFound();
            }
        }

        @Override
        public WriteResponse write(ServerIdentity identity, int resourceId, LwM2mResource value) {
            resources.put(resourceId, value.getValue().toString());
            System.out.println("Write request received for resource id " + resourceId + " with value: " + value.getValue().toString());
            return WriteResponse.success();
        }

        @Override
        public ExecuteResponse execute(ServerIdentity identity, int resourceId, String params) {
            switch (resourceId) {
                case 4:
                    System.out.println("Reboot command received.");
                    executeReboot();
                    return ExecuteResponse.success();
                case 5:
                    System.out.println("Deregister command received.");
                    executeDeregister();
                    return ExecuteResponse.success();
                default:
                    return ExecuteResponse.notFound();
            }
        }

        private void executeReboot() {
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    client.stop(true);
                    main(new String[0]);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        private void executeDeregister() {
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    client.stop(true);
                    System.exit(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        // Custom method to get resources
        public Map<Integer, String> getResources() {
            return resources;
        }
    }
}
