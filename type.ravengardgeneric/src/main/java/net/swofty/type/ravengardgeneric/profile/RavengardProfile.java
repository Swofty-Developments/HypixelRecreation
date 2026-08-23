package net.swofty.type.ravengardgeneric.profile;

import lombok.Getter;
import lombok.Setter;
import net.swofty.type.ravengardgeneric.classes.RavengardClass;
import org.bson.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class RavengardProfile {
    private final UUID id;
    private final UUID owner;
    private RavengardClass profileClass;
    private int level = 1;
    private int experience;
    private int crowns;
    private int abilityPoints;
    private boolean tutorial = true;
    private long playtimeSeconds;
    private long created;
    private final Map<Integer, String> inventory = new HashMap<>();
    private final Map<Integer, String> lockBox = new HashMap<>();
    private int lockBoxTier = 1;
    private final java.util.Set<String> intros = new java.util.HashSet<>();
    private final java.util.Set<String> discoveredRegions = new java.util.HashSet<>();

    public RavengardProfile(UUID id, UUID owner) {
        this.id = id;
        this.owner = owner;
        this.created = System.currentTimeMillis();
    }

    public static RavengardProfile fromDocument(Document document) {
        RavengardProfile profile = new RavengardProfile(
                UUID.fromString(document.getString("_id")),
                UUID.fromString(document.getString("_owner")));
        profile.profileClass = RavengardClass.fromKey(document.getString("class"));
        profile.level = document.getInteger("level", 1);
        profile.experience = document.getInteger("experience", 0);
        profile.crowns = document.getInteger("crowns", 0);
        profile.abilityPoints = document.getInteger("ability_points", 0);
        profile.tutorial = document.getBoolean("tutorial", true);
        profile.playtimeSeconds = document.get("playtime_seconds") instanceof Number number
                ? number.longValue() : 0L;
        profile.created = document.get("created") instanceof Number number
                ? number.longValue() : System.currentTimeMillis();
        if (document.get("intros") instanceof java.util.List<?> introList) {
            introList.forEach(name -> profile.intros.add(String.valueOf(name)));
        }
        if (document.get("discovered_regions") instanceof java.util.List<?> regionList) {
            regionList.forEach(name -> profile.discoveredRegions.add(String.valueOf(name)));
        }
        if (document.get("inventory") instanceof Document inventoryDocument) {
            inventoryDocument.forEach((slot, snbt) ->
                    profile.inventory.put(Integer.parseInt(slot), (String) snbt));
        }
        return profile;
    }

    /**
     * Playtime the way the captured tooltips write it: the two largest relevant units, so
     * "2 minutes, 16 seconds" below an hour and "3 hours, 12 minutes" above.
     */
    public String playtimeText() {
        long hours = playtimeSeconds / 3600;
        long minutes = (playtimeSeconds % 3600) / 60;
        long seconds = playtimeSeconds % 60;
        if (hours > 0) {
            return hours + (hours == 1 ? " hour, " : " hours, ")
                    + minutes + (minutes == 1 ? " minute" : " minutes");
        }
        return minutes + (minutes == 1 ? " minute, " : " minutes, ")
                + seconds + (seconds == 1 ? " second" : " seconds");
    }

    public int experienceForNextLevel() {
        return level * 10;
    }
}
