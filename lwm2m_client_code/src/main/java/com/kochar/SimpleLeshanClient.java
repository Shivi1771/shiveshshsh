package com.kochar;

import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.client.object.Security;
import org.eclipse.leshan.client.object.Server;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.core.LwM2mId;

public class SimpleLeshanClient {

    public static void main(String[] args) {
        String endpoint = "KocharTech"; // Choose an endpoint name
        String serverUri = "coap://192.168.105.180:5683"; // Update with your local server URI
        int clientId = 12345;

        // Create Leshan client builder with the chosen endpoint
        LeshanClientBuilder builder = new LeshanClientBuilder(endpoint);

        // Initialize and set custom objects
        ObjectsInitializer initializer = getCustomObjects(serverUri, clientId);
        builder.setObjects(initializer.createAll());

        // Build the Leshan client
        LeshanClient client = builder.build();

        // Start the Leshan client
        client.start();

        System.out.println("Leshan client started. Endpoint: " + endpoint);

        // No explicit reboot command handling here due to version 1.3.2 limitations
    }

    private static ObjectsInitializer getCustomObjects(String serverUri, int clientId) {
        ObjectsInitializer initializer = new ObjectsInitializer();

        // Security Object
        initializer.setInstancesForObject(LwM2mId.SECURITY, Security.noSec(serverUri, clientId));

        // Server Object
        initializer.setInstancesForObject(LwM2mId.SERVER, new Server(clientId, 5 * 60, org.eclipse.leshan.core.request.BindingMode.U, false));

        // Add other objects as needed for your device

        return initializer;
    }
}
