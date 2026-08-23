package net.swofty.type.generic.command.commands;

import net.minestom.server.utils.mojang.MojangUtils;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointRank;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CommandParameters(labels = "forceadmin adminme",
        description = "Literally just gives me admin",
        usage = "/adminme",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class AdminMeCommand extends HypixelCommand {

    private static final List<String> ADMIN_LIST = List.of(
            "Swofty",
            "Foodzz",
            "Hamza_dev",
            "ItzKatze"
    );

    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;

            player.sendMessage("<8>Running checks...");
            Thread.startVirtualThread(() -> {
                UUID realUUID = player.getUuid();
                UUID crackedUUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getUsername()).getBytes(StandardCharsets.UTF_8));

                List<UUID> adminUUIDs = new ArrayList<>();
                ADMIN_LIST.forEach(admin -> adminUUIDs.add(UUID.nameUUIDFromBytes(("OfflinePlayer:" + admin).getBytes(StandardCharsets.UTF_8))));
                ADMIN_LIST.parallelStream().forEach(admin -> {
                    try {
                        adminUUIDs.add(MojangUtils.getUUID(admin));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                if (!adminUUIDs.contains(realUUID) && !adminUUIDs.contains(crackedUUID)) {
                    player.sendMessage("<c>You are not allowed to use this command.");
                    return;
                }

                player.getDataHandler().get(HypixelDataHandler.Data.RANK, DatapointRank.class).setValue(Rank.STAFF);

                player.sendMessage("<a>Successfully set rank to <rank>.");
            });
        });
    }
}
