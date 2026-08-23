package net.swofty.type.skyblockgeneric.data.monogdb;

import com.mongodb.client.MongoClient;
import net.swofty.commons.skyblock.CoopStorage;
import net.swofty.proxyapi.ProxyPlayerSet;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class CoopDatabase {

    public static void connect(MongoClient client) {
    }

    public void save(Coop coop) {
        CoopStorage.write(coop.serialize());
    }

    public static Coop update(UUID coopId, Consumer<Coop> mutation) {
        Document mutated = CoopStorage.update(coopId, stored -> {
            Coop coop = Coop.deserialize(stored);
            mutation.accept(coop);
            return coop.serialize();
        });
        return mutated == null ? null : Coop.deserialize(mutated);
    }

    public static Coop getFromMember(UUID member) {
        return deserialize(CoopStorage.readByMember(member));
    }

    public static Coop getFromMemberProfile(UUID memberProfile) {
        return deserialize(CoopStorage.readByProfile(memberProfile));
    }

    private static Coop deserialize(Document roster) {
        return roster == null ? null : Coop.deserialize(roster);
    }

    public static Coop getClean(UUID originator) {
        return new Coop(UUID.randomUUID(), originator, new ArrayList<>(), new ArrayList<>(List.of(originator)), new ArrayList<>());
    }

    public record Coop(UUID coopUUID, UUID originator, List<UUID> members, List<UUID> memberInvites,
                       List<UUID> memberProfiles) {
        public ProxyPlayerSet getMembersAsProxyPlayerSet(UUID... toExclude) {
            List<UUID> members = new ArrayList<>(this.members);
            members.removeAll(List.of(toExclude));
            return new ProxyPlayerSet(members);
        }

        public boolean isOriginator(UUID uuid) {
            return uuid.equals(originator);
        }

        public void addInvite(UUID uuid) {
            memberInvites.add(uuid);
        }

        public void removeInvite(UUID uuid) {
            memberInvites.remove(uuid);
        }

        public Document serialize() {
            Document document = new Document("_id", coopUUID.toString());
            document.put("originator", originator.toString());

            List<String> members = new ArrayList<>();
            this.members.forEach(uuid -> members.add(uuid.toString()));
            document.put("members", members);
            List<String> memberInvites = new ArrayList<>();
            this.memberInvites.forEach(uuid -> memberInvites.add(uuid.toString()));
            document.put("memberInvites", memberInvites);
            List<String> memberProfiles = new ArrayList<>();
            this.memberProfiles.forEach(uuid -> memberProfiles.add(uuid.toString()));
            document.put("memberProfiles", memberProfiles);

            return document;
        }

        public List<SkyBlockPlayer> getOnlineInvitedPlayers() {
            return SkyBlockGenericLoader.getLoadedPlayers().stream().filter(player -> memberInvites.contains(player.getUuid())).toList();
        }

        public List<SkyBlockPlayer> getOnlineMembers() {
            return SkyBlockGenericLoader.getLoadedPlayers().stream()
                    .filter(player -> members.contains(player.getUuid()))
                    .filter(player -> memberProfiles.contains(player.getProfiles().getCurrentlySelected()))
                    .toList();
        }

        public void save() {
            CoopDatabase database = new CoopDatabase();
            database.save(this);
        }

        public Boolean isSameAs(Coop coop) {
            return coop.coopUUID.equals(coopUUID);
        }

        public static Coop deserialize(Document document) {
            UUID coopUUID = UUID.fromString(document.getString("_id"));
            UUID originator = UUID.fromString(document.getString("originator"));

            List<UUID> members = new ArrayList<>();
            List<String> membersString = (List<String>) document.get("members");
            membersString.forEach(uuid -> members.add(UUID.fromString(uuid)));
            List<UUID> memberInvites = new ArrayList<>();
            List<String> memberInvitesString = (List<String>) document.get("memberInvites");
            memberInvitesString.forEach(uuid -> memberInvites.add(UUID.fromString(uuid)));
            List<UUID> memberProfiles = new ArrayList<>();
            List<String> memberProfilesString = (List<String>) document.get("memberProfiles");
            memberProfilesString.forEach(uuid -> memberProfiles.add(UUID.fromString(uuid)));

            return new Coop(coopUUID, originator, members, memberInvites, memberProfiles);
        }
    }
}
