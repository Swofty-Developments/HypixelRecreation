package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@CommandParameters(labels = "iteminfo nbt",
        description = "Returns the players held item info",
        usage = "/nbt",
        permission = Rank.STAFF,
        allowsConsole = false)
public class NBTCommand extends HypixelCommand {
    private static Map<UUID, String> itemNBTCache = new HashMap<>();

    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            ItemStack item = ((SkyBlockPlayer) sender).getItemInMainHand();
            AtomicReference<String> values = new AtomicReference<>("");

            for (ItemAttribute possibleAttribute : ItemAttribute.getPossibleAttributes()) {
                String key = possibleAttribute.getKey();
                String value = item.getTag(Tag.String(key));
                if (value != null) {
                    values.set(key + ": " + value + "\n");
                    sender.sendMessage(Text.of("{}: {}", key, value));
                }
            }

            if (itemNBTCache.containsKey(((SkyBlockPlayer) sender).getUuid())) {
                if (itemNBTCache.get(((SkyBlockPlayer) sender).getUuid()).equals(values.get())) {
                    sender.sendMessage("Item NBT has not changed since last check");
                } else {
                    sender.sendMessage("Item NBT has changed since last check");
                    sender.sendMessage("This is what has changed:");

                    String[] oldValues = itemNBTCache.get(((SkyBlockPlayer) sender).getUuid()).split("\n");
                    String[] newValues = values.get().split("\n");

                    for (int i = 0; i < oldValues.length; i++) {
                        if (!oldValues[i].equals(newValues[i])) {
                            sender.sendMessage(Text.of("Old: {}", oldValues[i]));
                            sender.sendMessage(Text.of("New: {}", newValues[i]));
                        }
                    }

                    itemNBTCache.put(((SkyBlockPlayer) sender).getUuid(), values.get());
                }
            } else {
                itemNBTCache.put(((SkyBlockPlayer) sender).getUuid(), values.get());
            }
        });
    }
}
