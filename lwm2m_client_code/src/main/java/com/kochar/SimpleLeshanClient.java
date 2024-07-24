package com.kochar;

import java.util.HashMap;
import java.util.Map;

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

    private static final String SERVER_URI = "coap://192.168.105.180:5683";
    private static LeshanClient client;

    public static void main(String[] args) {
        String endpoint = "KocharTech";
        System.out.println("Starting Leshan client with endpoint: " + endpoint + " and server URI: " + SERVER_URI);

        LeshanClientBuilder builder = new LeshanClientBuilder(endpoint);
        ObjectsInitializer initializer = getCustomObjects(SERVER_URI, 12345);
        builder.setObjects(initializer.createAll());

        client = builder.build();
        client.start();

        System.out.println("Leshan client started. Endpoint: " + endpoint);
    }

    private static ObjectsInitializer getCustomObjects(String serverUri, int clientId) {
        ObjectsInitializer initializer = new ObjectsInitializer();

        System.out.println("Setting up Security and Server objects with server URI: " + serverUri);

        initializer.setInstancesForObject(LwM2mId.SECURITY, Security.noSec(serverUri, clientId));
        initializer.setInstancesForObject(LwM2mId.SERVER, new Server(clientId, 5 * 60, BindingMode.U, false));
        initializer.setInstancesForObject(LwM2mId.DEVICE, new CustomDevice());

        return initializer;
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
    }
}
