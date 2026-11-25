package gg.itzkatze.thehypixelrecreationmod.mixin;

import gg.itzkatze.thehypixelrecreationmod.features.packetlog.SkyBlockSessionLogger;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
    private void recordOutboundPacket(Packet<?> packet, CallbackInfo callbackInfo) {
        SkyBlockSessionLogger.recordOutbound(packet);
    }
}
