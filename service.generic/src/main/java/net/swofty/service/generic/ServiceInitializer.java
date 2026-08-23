package net.swofty.service.generic;

import lombok.RequiredArgsConstructor;
import net.swofty.commons.config.ConfigProvider;
import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.redis.RedisChannels;
import net.swofty.commons.redis.RedisClient;
import net.swofty.commons.redis.RedisEndpoint;
import net.swofty.commons.redis.RedisEnvelope;
import net.swofty.commons.redis.RedisMessageBus;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.redisapi.api.RedisAPI;
import net.swofty.service.generic.redis.PingEndpoint;
import net.swofty.service.generic.redis.ServiceRedisManager;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@RequiredArgsConstructor
public class ServiceInitializer {
    private final SkyBlockService service;

    public void init() {
        Logger.info("Initializing service {}...", service.getType().name());
        ItemAttribute.registerItemAttributes();

        ServiceRedisManager.connect(ConfigProvider.settings().getRedisUri(), service.getType());
        RedisClient.registerResponseChannel(RedisChannels.SERVICE_RESPONSE);
        RedisClient.registerResponseChannel(RedisChannels.SERVICE_BROADCAST_RESPONSE);

        List<RedisMessageHandler> endpoints = new ArrayList<>(service.getEndpoints());
        endpoints.add(new PingEndpoint());

        endpoints.forEach(endpoint -> {
            RedisProtocol<?, ?> protocolObject = endpoint.protocol();
            Logger.debug("Registering channel {}", protocolObject.channel());

            RedisMessageBus.registerHandler(
                RedisEndpoint.service(service.getType()),
                RedisChannels.protocol(protocolObject),
                endpoint,
                (envelope, _) -> RedisMessageContext.between(
                    UUID.fromString(envelope.id()),
                    RedisEndpoint.server(envelope.from()),
                    RedisEndpoint.service(service.getType()),
                    protocolObject.channel()
                ),
                RedisEnvelope::from,
                _ -> RedisChannels.protocol(protocolObject)
            );
        });

        RedisAPI.getInstance().startListeners();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                RedisAPI.getInstance().shutdown();
            } catch (Exception exception) {
                Logger.error(exception, "Failed to shut down Redis for service {}", service.getType().name());
            }
        }, "service-redis-shutdown"));

        Logger.info("Service {} initialized!", service.getType().name());
        service.onReady();

        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
