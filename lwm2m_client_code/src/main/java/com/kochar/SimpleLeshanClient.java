package com.kochar;

import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.client.object.Security;
import org.eclipse.leshan.client.object.Server;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.LwM2mId;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;

public class SimpleLeshanClient {

    private static final String serverUri = "coap://192.168.105.180:5683"; // Update with your server URI
    private static LeshanClient client; // Declare client as a class variable

    public static void main(String[] args) {
        String endpoint = "KocharTech"; // Choose an endpoint name
        System.out.println("Starting Leshan client with endpoint: " + endpoint + " and server URI: " + serverUri);

        // Create Leshan client builder with the chosen endpoint
        LeshanClientBuilder builder = new LeshanClientBuilder(endpoint);

        // Initialize and set custom objects
        ObjectsInitializer initializer = getCustomObjects(serverUri, 12345); // Pass serverUri and client ID
        builder.setObjects(initializer.createAll());

        // Build the Leshan client
        client = builder.build();

        // Start the Leshan client
        client.start();

        System.out.println("Leshan client started. Endpoint: " + endpoint);
    }

    private static ObjectsInitializer getCustomObjects(String serverUri, int clientId) {
        ObjectsInitializer initializer = new ObjectsInitializer();

        // Print the serverUri being used for setting up Security and Server objects
        System.out.println("Setting up Security and Server objects with server URI: " + serverUri);

        // Security Object
        initializer.setInstancesForObject(LwM2mId.SECURITY, Security.noSec(serverUri, clientId));

        // Server Object
        initializer.setInstancesForObject(LwM2mId.SERVER, new Server(clientId, 5 * 60, BindingMode.U, false));

        // Custom Device Object with Reboot and Deregister capabilities
        initializer.setInstancesForObject(LwM2mId.DEVICE, new CustomDevice());

        // Add other objects as needed for your device

        return initializer;
    }

    // Custom Device class to handle execute commands (e.g., reboot and deregister)
    public static class CustomDevice extends BaseInstanceEnabler {
        @Override
        public ReadResponse read(ServerIdentity identity, int resourceid) {
            switch (resourceid) {
                case 0:
                    // Implement read logic for resource ID 0
                    return ReadResponse.success(resourceid, "Your Read Value");
            }
            return ReadResponse.notFound();
        }

        @Override
        public WriteResponse write(ServerIdentity identity, int resourceid, LwM2mResource value) {
            switch (resourceid) {
                case 15:
                    // Implement write logic for resource ID 15
                    return WriteResponse.success();
            }
            return WriteResponse.notFound();
        }

        @Override
        public ExecuteResponse execute(ServerIdentity identity, int resourceid, String params) {
            switch (resourceid) {
                case 4: // Reboot resource ID
                    System.out.println("Reboot command received.");
                    // Implement reboot logic here
                    // For example, stop and restart the client
                    Thread rebootThread = new Thread(() -> {
                        try {
                            Thread.sleep(1000); // Delay for simulation
                            client.stop(true); // Stop the client with deregistration
                            main(new String[0]); // Restart the client
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    rebootThread.start();
                    return ExecuteResponse.success();

                case 5: // Deregister resource ID (example)
                    System.out.println("Deregister command received.");
                    // Implement deregistration logic here
                    // For example, stop the client and unregister from server
                    Thread deregisterThread = new Thread(() -> {
                        try {
                            Thread.sleep(1000); // Delay for simulation
                            client.stop(true); // Stop the client with deregistration
                            System.exit(0); // Exit the application after deregistration
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    deregisterThread.start();
                    return ExecuteResponse.success();
            }
            return ExecuteResponse.notFound();
        }
    }
}
