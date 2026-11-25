package io.github.term4.polyp.mechanics.projectile.entities;

import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.potion.PotionType;
import org.junit.jupiter.api.Test;

class SplashKitProbeTest extends HeadlessServerTest {

    @Test
    void probeConfigTimeKitAdd() {
        var listener = net.minestom.server.event.EventListener.of(net.minestom.server.event.player.AsyncPlayerConfigurationEvent.class, e -> {
            if (!e.getPlayer().getUsername().equals("KitProbe2")) return;
            var player = e.getPlayer();
            player.getInventory().addItemStack(ItemStack.of(Material.RED_WOOL, 1000));
            player.getInventory().addItemStack(ItemStack.of(Material.LADDER, 64));
            player.getInventory().addItemStack(ItemStack.of(Material.DIAMOND_SWORD, 1));
            player.getInventory().addItemStack(ItemStack.of(Material.SNOWBALL, 64));
            player.getInventory().addItemStack(ItemStack.of(Material.FIRE_CHARGE, 64));
            player.getInventory().addItemStack(ItemStack.of(Material.TNT, 64));
            player.getInventory().addItemStack(ItemStack.of(Material.EGG, 16));
            player.getInventory().addItemStack(ItemStack.of(Material.ENDER_PEARL, 16));
            player.getInventory().addItemStack(ItemStack.of(Material.BOW, 1));
            player.getInventory().addItemStack(ItemStack.of(Material.ARROW, 64));
            player.getInventory().addItemStack(ItemStack.of(Material.GOLDEN_APPLE, 16));
            player.getInventory().addItemStack(ItemStack.of(Material.ENCHANTED_GOLDEN_APPLE, 16));
            player.getInventory().addItemStack(ItemStack.of(Material.MILK_BUCKET, 1));
            player.getInventory().addItemStack(ItemStack.of(Material.POTION).with(DataComponents.POTION_CONTENTS,
                    new PotionContents(new net.minestom.server.potion.CustomPotionEffect(net.minestom.server.potion.PotionEffect.SPEED, 0, 1200, false, true, true))));
            player.getInventory().addItemStack(ItemStack.of(Material.POTION).with(DataComponents.POTION_CONTENTS,
                    new PotionContents(PotionType.HEALING)));
            player.getInventory().addItemStack(ItemStack.of(Material.SPLASH_POTION).with(DataComponents.POTION_CONTENTS,
                    new PotionContents(PotionType.SWIFTNESS)));
            player.getInventory().addItemStack(ItemStack.of(Material.SPLASH_POTION).with(DataComponents.POTION_CONTENTS,
                    new PotionContents(PotionType.HARMING)));
            System.out.println("[probe] kit given at config time");
        });
        net.minestom.server.MinecraftServer.getGlobalEventHandler().addListener(listener);
        try {
            FakePlayer p = FakePlayer.connect(instance, new Pos(0.5, 64, 0.5), "KitProbe2");
            int splash = 0;
            for (int i = 0; i < p.player.getInventory().getSize(); i++) {
                ItemStack it = p.player.getInventory().getItemStack(i);
                if (it.material() == Material.SPLASH_POTION) { splash++; System.out.println("[probe] slot " + i + " = SPLASH x" + it.amount() + " potion=" + (it.get(DataComponents.POTION_CONTENTS) != null ? it.get(DataComponents.POTION_CONTENTS).potion() : null)); }
            }
            System.out.println("[probe] splash stacks server-side: " + splash);
            p.player.remove();
        } finally {
            net.minestom.server.MinecraftServer.getGlobalEventHandler().removeListener(listener);
        }
    }
}
