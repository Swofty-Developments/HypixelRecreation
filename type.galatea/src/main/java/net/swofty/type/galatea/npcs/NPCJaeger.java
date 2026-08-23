package net.swofty.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCJaeger extends HypixelNPC {

    public NPCJaeger() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Jaeger", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "iYftCZb0NhyhgQ5IQbFNTOBiUN45AwrJrxr9W0mukvrW0bH+0xuOnl7Oe0GXjyeE75aHy0dJZnc6aQCqg0Poa7c6OMkuJxP60GT6a1weatQNBMm0Z8uK/o0dGZU5pJG7y98nzAJFpxXwhpgJucKN46RLjoOcqHKV49rap2wINyR2Jf5DbmqOkp1ktBKPbmUFZrc5kcK1qIlB4jX+0ek0GOiLkjv7pukYleg8ZYoJ10bMGQfJtZcjwC9e+4WGKvi+559X9I4il/jyCilYQMTCuopEkabfUuY68p4nG3dSz6MxW8H1jDJEaz9rMquEw2QlS3ctGF7Ij16ip9Dd1YATfezARvdmvhMMmwRnEZP1eASs6/blQua+bUKwp5GpH3hN+IL0m9fkVhgBmZSF+ILeiQpPxG/nny1YFgIZcOxXH2c7HrBBRgb+hrbm6dyHkosTYSxXcVWXCfS2vxrp+tNs93Ki4flzi3P+flEEJnwG/Eo6gj2GWbAgN/QOuxL96hs4sQf4krQu0Hhvz0eq2Dm29S1xEa320OC6HB1r514ehaSTDhM1ayZLrHTffp5rYCzvS3o7fpWwP7DfhNBcPUsXggh5oT+nkmSPZSVKVLH+ZtxWEtn+gNDfLqGBtNaW05YrcemnWGvY5Qg8t98LFYU4ZLrBbdGZmgful8BjLPWJkk4=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTcxOTMyODk0OTI0MiwKICAicHJvZmlsZUlkIiA6ICI0MzFhMmRlYTQ4YTE0NTMxYjEyZDU5MzY0NDUxNmIyNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJpQ2FwdGFpbk5lbW8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTA3ZjE2NTdlYmZlZGQwYzM0NzMwYmU3ZDg2M2M1YWFiNjRmYWE0Mjk4MDhhYTEwZTk1MmYwNGZlYjZhMjdiOCIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-651.5, 89, -24.5, 149, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        event.player().notImplemented();
    }
}
