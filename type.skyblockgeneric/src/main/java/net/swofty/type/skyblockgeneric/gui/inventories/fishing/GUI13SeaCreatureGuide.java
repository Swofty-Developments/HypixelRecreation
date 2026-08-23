package net.swofty.type.skyblockgeneric.gui.inventories.fishing;

import net.minestom.server.item.Material;

import java.util.List;

public final class GUI13SeaCreatureGuide extends SeaCreatureGuidePage {

    @Override
    protected int pageNumber() {
        return 1;
    }

    @Override
    protected List<Entry> entries() {
        return List.of(
        head(10, "32581d564f01d712255125e1f101e534217f76e3599dab7f4ae0ffe328f729eb", """
            <7>[Lvl 1] Squid (<f><l>Common</l><7>)
            <9>⚓ Aquatic<7>, <a>☮ Animal

            <c>Drops:
            <7>- Ink Sac
            <7>- Lily Pad

            <c>Requirements:
            <7>- <a>Fishing Skill 1
            <7>- <b>Water

            <c>Stats
            <7>- Kills: <b>64"""),
        head(11, "d88ba8bb50b79e441e47b7e452764d5fff6693779d2dadd9f7f52f98d7ea0", """
            <7>[Lvl 4] Sea Walker (<f><l>Common</l><7>)
            <9>⚓ Aquatic<7>, <2>༕ Undead

            <c>Drops:
            <7>- Lily Pad
            <7>- Raw Cod
            <7>- Rotten Flesh

            <c>Requirements:
            <7>- <a>Fishing Skill 1
            <7>- <b>Water

            <c>Stats
            <7>- Kills: <b>64"""),
        head(12, "32581d564f01d712255125e1f101e534217f76e3599dab7f4ae0ffe328f729eb", """
            <7>[Lvl 6] Night Squid (<f><l>Common</l><7>)
            <9>⚓ Aquatic<7>, <a>☮ Animal

            <c>Drops:
            <7>- Ink Sac
            <7>- Lily Pad
            <7>- <a>Fishing Exp Boost
            <7>- <a>Squid Boots
            <7>- <9>Fishing Exp Boost

            <c>Requirements:
            <7>- <a>Fishing Skill 3
            <7>- <b>Water
            <7>- Nighttime
            <7>- <f>Dark Bait"""),
        head(13, "221025434045bda7025b3e514b316a4b770c6faa4ba9adb4be3809526db77f9d", """
            <7>[Lvl 10] Sea Guardian (<f><l>Common</l><7>)
            <9>⚓ Aquatic<7>, <5>♃ Arcane

            <c>Drops:
            <7>- Lily Pad
            <7>- Prismarine Crystals
            <7>- Prismarine Shard

            <c>Requirements:
            <7>- <a>Fishing Skill 5
            <7>- <b>Water

            <c>Stats
            <7>- Kills: <b>48"""),
        head(14, "6157f19da077a3df49b2925fb6e8b400222ba6e559e86815f9b296d9e9667dd7", """
            <7>[Lvl 10] Frog Man (<f><l>Common</l><7>)
            <9>⚓ Aquatic<7>, <a>☮ Animal<7>, <e>✰ Humanoid

            <c>Drops:
            <7>- Lily Pad
            <7>- Tropical Fish
            <7>- <a>Half-Eaten Mushroom

            <c>Requirements:
            <7>- <a>Fishing Skill 5
            <7>- <b>Water
            <7>- <d>Fishing Hotspot

            <c>Stats
            <7>- Kills: <b>28"""),
        head(15, "18ae7046da98dcb33f3ed42f1dc41d08ac8dfa5db3a3860de5b1b5c056804187", """
            <7>[Lvl 8] Trash Gobbler (<f><l>Common</l><7>)
            <9>⚓ Aquatic<7>, <4>Ж Arthropod

            <c>Drops:
            <7>- Clay Ball
            <7>- Lily Pad
            <7>- Raw Cod
            <7>- <a>Can of Worms

            <c>Requirements:
            <7>- <a>Fishing Skill 5
            <7>- <b>Water
            <7>- <2>Backwater Bayou

            <c>Stats
            <7>- Kills: <b>57"""),
        head(16, "a3b9003ba2d05562c75119b8a62185c67130e9282f7acbac4bc2824c21eb95d9", """
            <7>[Lvl 10] Bogged (<f><l>Common</l><7>)
            <f>🦴 Skeletal<7>, <9>⚓ Aquatic

            <c>Drops:
            <7>- Mangrove Log
            <7>- Sea Lumies

            <c>Requirements:
            <7>- <a>Fishing Skill 5
            <7>- <b>Water
            <7>- <2>Galatea"""),
        head(19, "54690f5aa6d0e800f9b8d1890fc158b921819a81dfd7342a2170e7efc46b9ed7", """
            <7>[Lvl 7] Frozen Steve (<f><l>Common</l><7>)
            <9>⚓ Aquatic<7>, <f>☃ Frozen<7>, <e>✰ Humanoid

            <c>Drops:
            <7>- <9>Ice Essence
            <7>- Ice
            <7>- Lily Pad
            <7>- Pufferfish
            <7>- Raw Cod
            <7>- Raw Salmon
            <7>- White Gift
            <7>- <a>Hunk of Ice
            <7>- <9>Icy Sinker

            <c>Requirements:
            <7>- <a>Fishing Skill 5
            <7>- <b>Water
            <7>- <c>Jerry's Workshop

            <c>Stats
            <7>- Kills: <b>9"""),
        head(20, "f5c5eb5ee072c06580986d12a029e28010c1290875534810c53140bc76dabfeb", """
            <7>[Lvl 15] Dumpster Diver (<a><l>Uncommon</l><7>)
            <9>⚓ Aquatic<7>, <e>✰ Humanoid

            <c>Drops:
            <7>- Clay Ball
            <7>- Lily Pad
            <7>- Raw Cod
            <7>- Sponge
            <7>- <a>Bronze Bowl
            <7>- <a>Overflowing Trash Can

            <c>Requirements:
            <7>- <a>Fishing Skill 5
            <7>- <b>Water
            <7>- <2>Backwater Bayou

            <c>Stats
            <7>- Kills: <b>38"""),
        head(21, "2067ccefba5d811f47e3e18438556b704393aafcafccedd5d0981999286f598a", """
            <7>[Lvl 6] Nurse Shark (<a><l>Uncommon</l><7>)
            <9>⚓ Aquatic<7>, <a>☮ Animal

            <c>Drops:
            <7>- <a>Carnival Ticket
            <7>- <a>Nurse Shark Tooth
            <7>- <9>Shark Fin

            <c>Requirements:
            <7>- <a>Fishing Skill 5
            <7>- <b>Water
            <7>- <b>Fishing Festival"""),
        block(22, Material.CARVED_PUMPKIN, """
            <7>[Lvl 13] Frosty (<f><l>Common</l><7>)
            <9>⚓ Aquatic<7>, <f>☃ Frozen<7>, ⚙ Construct

            <c>Drops:
            <7>- <9>Ice Essence
            <7>- Carrot
            <7>- Ice
            <7>- Lily Pad
            <7>- Raw Cod
            <7>- Snow Block
            <7>- Sponge
            <7>- White Gift
            <7>- <a>Hunk of Ice

            <c>Requirements:
            <7>- <a>Fishing Skill 6
            <7>- <b>Water
            <7>- <c>Jerry's Workshop

            <c>Stats
            <7>- Kills: <b>9"""),
        head(23, "811a1173af3bead305e6339f555662e990d5faadb87e07299fa68ca828a6d2fb", """
            <7>[Lvl 15] Mithril Grubber (<a><l>Uncommon</l><7>)
            <9>⚓ Aquatic<7>, <4>Ж Arthropod

            <c>Drops:
            <7>- <9>Mithril Powder
            <7>- Mithril
            <7>- Raw Cod
            <7>- <9>Enchanted Mithril

            <c>Requirements:
            <7>- <a>Fishing Skill 6
            <7>- <b>Water
            <7>- <2>Abandoned Quarry"""),
        head(24, "dbc0f7c9e926c320ba472d4a88763ef932a660c470f786ac0c04c15a78fd505f", """
            <7>[Lvl 18] Wetwing (<a><l>Uncommon</l><7>)
            <9>⚓ Aquatic<7>, <2>༕ Undead<7>, <a>☮ Animal

            <c>Drops:
            <7>- Mangrove Log
            <7>- Sea Lumies
            <7>- <a>Wet Water
            <7>- <9>Enchanted Mangrove Log

            <c>Requirements:
            <7>- <a>Fishing Skill 7
            <7>- <b>Water
            <7>- <2>Galatea

            <c>Stats
            <7>- Kills: <b>1"""),
        head(25, "fce6604157fc4ab5591e4bcf507a749918ee9c41e357d47376e0ee7342074c90", """
            <7>[Lvl 15] Sea Witch (<a><l>Uncommon</l><7>)
            <9>⚓ Aquatic<7>, <e>✰ Humanoid<7>, <5>♃ Arcane

            <c>Drops:
            <7>- Lily Pad
            <7>- Raw Salmon
            <7>- Tropical Fish
            <7>- <9>Fairy's Fedora
            <7>- <9>Fairy's Galoshes
            <7>- <9>Fairy's Polo
            <7>- <9>Fairy's Trousers

            <c>Requirements:
            <7>- <a>Fishing Skill 7
            <7>- <b>Water

            <c>Stats
            <7>- Kills: <b>52"""),
        block(28, Material.CARVED_PUMPKIN, """
            <7>[Lvl 9] Scarecrow (<f><l>Common</l><7>)
            <9>⚓ Aquatic<7>, <6>☽ Spooky

            <c>Drops:
            <7>- Hay Bale
            <7>- Lily Pad
            <7>- <a>Green Candy
            <7>- <5>Purple Candy

            <c>Requirements:
            <7>- <a>Fishing Skill 9
            <7>- <b>Water
            <7>- <6>Spooky Festival"""),
        block(29, Material.SKELETON_SKULL, """
            <7>[Lvl 15] Sea Archer (<a><l>Uncommon</l><7>)
            <9>⚓ Aquatic<7>, <f>🦴 Skeletal

            <c>Drops:
            <7>- Bone
            <7>- Lily Pad
            <7>- Raw Cod
            <7>- <a>Enchanted Bone

            <c>Requirements:
            <7>- <a>Fishing Skill 9
            <7>- <b>Water

            <c>Stats
            <7>- Kills: <b>48"""),
        head(30, "1608d86ffb297bf93b7190d24bc3b2dc094f8086740f7541a752fbe661f175fc", """
            <7>[Lvl 8] Tadgang (<9><l>Rare</l><7>)
            <9>⚓ Aquatic<7>, <a>☮ Animal

            <c>Drops:
            <7>- Mangrove Log
            <7>- Sea Lumies
            <7>- <a>Enchanted Sea Lumies
            <7>- <a>Gill Membrane
            <7>- <9>Enchanted Mangrove Log

            <c>Requirements:
            <7>- <a>Fishing Skill 9
            <7>- <b>Water
            <7>- <2>Galatea

            <c>Stats
            <7>- Kills: <b>1"""),
        head(31, "292df216ecd27624ac771bacfbfe006e1ed84a79e9270be0f88e9c8791d1ece4", """
            <7>[Lvl 10] Oasis Sheep (<a><l>Uncommon</l><7>)
            <9>⚓ Aquatic<7>, <a>☮ Animal

            <c>Drops:
            <7>- Lily Pad
            <7>- Raw Mutton
            <7>- White Wool
            <7>- <a>Enchanted Raw Mutton
            <7>- <9>Enchanted Cooked Mutton

            <c>Requirements:
            <7>- <a>Fishing Skill 10
            <7>- <b>Water
            <7>- <b>Oasis"""),
        head(32, "b50459bcb08db5ce93e021079c1cfc038c9ebe7ad9a149516efe4d5ee8afb59f", """
            <7>[Lvl 10] Oasis Rabbit (<a><l>Uncommon</l><7>)
            <9>⚓ Aquatic<7>, <a>☮ Animal

            <c>Drops:
            <7>- Lily Pad
            <7>- Rabbit Hide
            <7>- Rabbit's Foot
            <7>- Raw Rabbit
            <7>- <a>Enchanted Raw Rabbit

            <c>Requirements:
            <7>- <a>Fishing Skill 10
            <7>- <b>Water
            <7>- <b>Oasis"""),
        head(33, "30ccc3c9a06de657b98f881e23a57ecaeb252c364ddb7b92564f5ed2b8087e3b", """
            <7>[Lvl 10] Banshee (<9><l>Rare</l><7>)
            <9>⚓ Aquatic<7>, <e>✰ Humanoid

            <c>Drops:
            <7>- Clay Ball
            <7>- Lily Pad
            <7>- Raw Salmon
            <7>- Tropical Fish
            <7>- <a>Enchanted Clay Ball
            <7>- <a>Torn Cloth
            <7>- <9>Calcified Heart

            <c>Requirements:
            <7>- <a>Fishing Skill 10
            <7>- <b>Water
            <7>- <2>Backwater Bayou

            <c>Stats
            <7>- Kills: <b>22"""),
        head(34, "381e1d06e5f0654a682a3264905b5dc4b8e7b613ab6697ac45f2e0da3bc9b4fd", """
            <7>[Lvl 20] Blue Shark (<9><l>Rare</l><7>)
            <9>⚓ Aquatic<7>, <a>☮ Animal

            <c>Drops:
            <7>- <a>Carnival Ticket
            <7>- <9>Blue Shark Tooth
            <7>- <9>Shark Fin

            <c>Requirements:
            <7>- <a>Fishing Skill 10
            <7>- <b>Water
            <7>- <b>Fishing Festival"""),
        head(37, "e08fc1ae87a7035d32b0b0da58de4801463aaf8b238618024aacb0c515ae3bba", """
            <7>[Lvl 30] Snapping Turtle (<9><l>Rare</l><7>)
            <9>⚓ Aquatic<7>, <a>☮ Animal

            <c>Drops:
            <7>- Gold Ingot
            <7>- Lily Pad
            <7>- Raw Cod
            <7>- <a>Enchanted Gold Ingot
            <7>- <9>Broken Radar
            <7>- <9>Edible Seaweed

            <c>Requirements:
            <7>- <a>Fishing Skill 10
            <7>- <b>Water
            <7>- <d>Fishing Hotspot

            <c>Stats
            <7>- Kills: <b>5"""),
        head(38, "cfb7dbbe002f69463768113c1e925848197f59b62694ce105792dd5a52dc17a1", """
            <7>[Lvl 20] Rider of the Deep (<a><l>Uncommon</l><7>)
            <9>⚓ Aquatic<7>, <2>༕ Undead<7>, <a>☮ Animal

            <c>Drops:
            <7>- Dark Bait
            <7>- Lily Pad
            <7>- Sponge
            <7>- <9>Enchanted Book
            <7>   (Magnet VI)
            <7>- <a>Enchanted Feather
            <7>- <a>Enchanted Rotten Flesh

            <c>Requirements:
            <7>- <a>Fishing Skill 11
            <7>- <b>Water

            <c>Stats
            <7>- Kills: <b>36"""),
        head(39, "30519f79e5829136c3df10b6bd727db255717c87e5c102892ef67e7f46929515", """
            <7>[Lvl 14] Ent (<5><l>Epic</l><7>)
            <2>⸙ Woodland<7>, <9>⚓ Aquatic

            <c>Drops:
            <7>- Sea Lumies
            <7>- <a>Enchanted Sea Lumies
            <7>- <9>Enchanted Mangrove Log
            <7>- <5>Foraging Exp Boost
            <7>- <5>Mangcore

            <c>Requirements:
            <7>- <a>Fishing Skill 12
            <7>- <b>Water
            <7>- <2>Galatea"""),
        head(40, "2508e4a2f88502c019652b2437b76c82fedff9091389d88118ecc673f628b547", """
            <7>[Lvl 21] Grinch (<a><l>Uncommon</l><7>)
            <9>⚓ Aquatic<7>, <f>☃ Frozen<7>, <e>✰ Humanoid

            <c>Drops:
            <7>- <9>Ice Essence
            <7>- White Gift
            <7>- <a>Green Gift

            <c>Requirements:
            <7>- <a>Fishing Skill 13
            <7>- <b>Water
            <7>- <c>Jerry's Workshop

            <c>Stats
            <7>- Kills: <b>2"""),
        head(41, "e18f77331b2cab64e2b430fa8e4273e4db7f78fcdfa4b1a9a418af47375056eb", """
            <7>[Lvl 23] Catfish (<9><l>Rare</l><7>)
            <9>⚓ Aquatic<7>, <a>☮ Animal

            <c>Drops:
            <7>- Lily Pad
            <7>- Pufferfish
            <7>- Raw Cod
            <7>- Raw Salmon
            <7>- Sponge
            <7>- Tropical Fish
            <7>- <9>Enchanted Book
            <7>   (Frail VI)

            <c>Requirements:
            <7>- <a>Fishing Skill 13
            <7>- <b>Water

            <c>Stats
            <7>- Kills: <b>26"""),
        block(42, Material.COOKED_CHICKEN, """
            <7>[Lvl 30] Fried Chicken (<f><l>Common</l><7>)
            <c>♆ Magmatic<7>, <a>☮ Animal

            <c>Drops:
            <7>- Raw Chicken
            <7>- <a>Enchanted Raw Chicken
            <7>- <a>Fried Feather
            <7>- <9>Magmafish

            <c>Requirements:
            <7>- <a>Fishing Skill 14
            <7>- <c>Lava
            <7>- <d>Fishing Hotspot
            <7>- <c>Crimson Isle"""),
        head(43, "b50459bcb08db5ce93e021079c1cfc038c9ebe7ad9a149516efe4d5ee8afb59f", """
            <7>[Lvl 25] Carrot King (<9><l>Rare</l><7>)
            <9>⚓ Aquatic<7>, <a>☮ Animal

            <c>Drops:
            <7>- Lily Pad
            <7>- Sponge
            <7>- <9>Enchanted Book
            <7>   (Caster VI)
            <7>- <a>Enchanted Carrot
            <7>- <a>Rabbit Hat
            <7>- <9>Enchanted Rabbit Foot
            <7>- <5>Lucky Clover Core

            <c>Requirements:
            <7>- <a>Fishing Skill 14
            <7>- <b>Water
            <7>- <f>Carrot Bait""")
        );
    }
}

