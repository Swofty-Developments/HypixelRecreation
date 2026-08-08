package net.swofty.service.replay.endpoints;

import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.game.ViewReplayPushProtocol;
import net.swofty.commons.protocol.objects.replay.ChooseReplayProtocolObject;
import net.swofty.commons.redis.RedisClient;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.redis.RedisMessageHandler;

public class ReplayChooseEndpoint implements RedisMessageHandler
		<ChooseReplayProtocolObject.ChooseReplayMessage,
			ChooseReplayProtocolObject.ChooseReplayResponse> {

	@Override
	public RedisProtocol<ChooseReplayProtocolObject.ChooseReplayMessage, ChooseReplayProtocolObject.ChooseReplayResponse> protocol() {
		return new ChooseReplayProtocolObject();
	}

	@Override
	public ChooseReplayProtocolObject.ChooseReplayResponse handle(ChooseReplayProtocolObject.ChooseReplayMessage body, RedisMessageContext context) {
		var responses = RedisClient.requestAllServersFromService(
			new ViewReplayPushProtocol(),
			new ViewReplayPushProtocol.Request(body.player(), body.replayId(), body.shareCode()),
				300).join();
		boolean accepted = responses.values().stream().anyMatch(ViewReplayPushProtocol.Response::success);
		return new ChooseReplayProtocolObject.ChooseReplayResponse(!accepted);
	}

}
