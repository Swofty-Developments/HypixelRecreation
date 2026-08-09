package net.swofty.packer;

import net.swofty.packer.packs.ravengard.RavengardPackDefinition;
import net.swofty.packer.packs.skyblock.SkyblockPackDefinition;
import team.unnamed.creative.BuiltResourcePack;
import team.unnamed.creative.server.ResourcePackServer;
import team.unnamed.creative.server.handler.ResourcePackRequestHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HypixelPackServer {
    private static final String DEFAULT_HOST = "0.0.0.0";
    private static final int DEFAULT_PORT = 7270;

    static void main(String[] args) throws IOException {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i]) {
                case "-h", "--host" -> host = args[++i];
                case "-p", "--port" -> port = Integer.parseInt(args[++i]);
            }
        }

        Map<String, BuiltResourcePack> packs = List.of(
                        RavengardPackDefinition.INSTANCE,
                        SkyblockPackDefinition.INSTANCE
                ).stream()
                .filter(HypixelPackServer::packDirectoryExists)
                .map(HypixelPackServer::buildPack)
                .collect(Collectors.toMap(
                        pack -> pack.hash() + ".zip",
                        Function.identity(),
                        (first, second) -> first
                ));

        ResourcePackServer server = ResourcePackServer.server()
                .address(host, port)
                .handler(createRequestHandler(packs))
                .executor(Executors.newFixedThreadPool(4))
                .build();
        server.start();

        System.out.println("Resource pack server started on " + host + ":" + port);
        for (String fileName : packs.keySet()) {
            System.out.println("Pack URL: http://" + host + ":" + port + "/" + fileName);
        }
        System.out.println("Press Ctrl+C to stop.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down pack server...");
            server.stop(5);
        }));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException ignored) {}
    }

    private static boolean packDirectoryExists(PackDefinition definition) {
        boolean exists = Files.isDirectory(Path.of(definition.getPackDirectory()));
        if (!exists) {
            System.out.println("Skipping resource pack '" + definition.getPackName()
                    + "': directory does not exist: " + definition.getPackDirectory());
        }
        return exists;
    }

    private static BuiltResourcePack buildPack(PackDefinition definition) {
        System.out.println("Building resource pack '" + definition.getPackName() + "'...");
        System.out.println("Pack directory: " + definition.getPackDirectory());

        BuiltResourcePack built = new HypixelPackBuilder(definition).build();
        System.out.println("Resource pack built. Hash: " + built.hash());
        return built;
    }

    private static ResourcePackRequestHandler createRequestHandler(Map<String, BuiltResourcePack> packs) {
        return (request, exchange) -> {
            String path = exchange.getRequestURI().getPath();
            String fileName = path.substring(path.lastIndexOf('/') + 1);

            BuiltResourcePack pack = packs.get(fileName);

            if (pack == null) {
                byte[] response = "Resource pack not found\n".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(404, response.length);
                try (OutputStream output = exchange.getResponseBody()) {
                    output.write(response);
                }
                return;
            }

            byte[] response = pack.data().toByteArray();
            exchange.getResponseHeaders().set("Content-Type", "application/zip");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(response);
            }
        };
    }
}
