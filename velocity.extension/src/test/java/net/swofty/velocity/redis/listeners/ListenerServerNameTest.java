package net.swofty.velocity.redis.listeners;

import net.swofty.commons.protocol.objects.proxy.to.RequestServerNameProtocol;
import net.swofty.commons.redis.RedisEndpoint;
import net.swofty.commons.redis.RedisMessageContext;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ListenerServerNameTest {

    @Test
    void answersServersTheProxyNoLongerKnowsAbout() {
        UUID serverID = UUID.randomUUID();
        RedisMessageContext context = RedisMessageContext.between(
                UUID.randomUUID(),
                RedisEndpoint.server(serverID),
                RedisEndpoint.proxy(),
                new RequestServerNameProtocol().channel()
        );

        RequestServerNameProtocol.Response response =
                new ListenerServerName().handle(new RequestServerNameProtocol.Request(), context);

        assertNotNull(response);
        assertFalse(response.success());
        assertNull(response.serverName());
        assertNotNull(response.error());
    }
}
