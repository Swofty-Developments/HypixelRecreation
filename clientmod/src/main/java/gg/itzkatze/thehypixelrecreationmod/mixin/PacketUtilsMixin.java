package gg.itzkatze.thehypixelrecreationmod.mixin;

import gg.itzkatze.thehypixelrecreationmod.features.hudcapture.HudCaptureRecorder;
import gg.itzkatze.thehypixelrecreationmod.features.packetlog.EntityPacketLogger;
import gg.itzkatze.thehypixelrecreationmod.features.packetlog.RavengardSessionLogger;
import gg.itzkatze.thehypixelrecreationmod.features.packetlog.SkyBlockSessionLogger;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketProcessor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketUtils.class)
public abstract class PacketUtilsMixin {
    @Inject(
            method = "ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V",
            at = @At("HEAD")
    )
    private static <T extends PacketListener> void recordInboundPacket(
            Packet<T> packet,
            T listener,
            PacketProcessor engine,
            CallbackInfo callbackInfo
    ) {
        if (listener instanceof ClientGamePacketListener) {
            EntityPacketLogger.record(packet);
            RavengardSessionLogger.record(packet);
            SkyBlockSessionLogger.recordInbound(packet);
            HudCaptureRecorder.record(packet);
        }
    }
}
