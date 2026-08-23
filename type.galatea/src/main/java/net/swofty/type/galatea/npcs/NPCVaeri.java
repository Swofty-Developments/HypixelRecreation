package net.swofty.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCVaeri extends HypixelNPC {

    public NPCVaeri() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Vaeri", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "JZvY6u8/AZgq4M1sSm+wOK7qFtgxle2/0VKL24KblSmjW5mMXtGVDoHVuZCy8t3Te/Lbq80Bz10wnCJZWAflACcf+IWHbc8uIo6kmt5jcxY/dY1VjcQVA8lpufvdb0Y+g/iTNc0X7QAk7K15UtWa1T3F0TRBdIyxJVkaQ6SPS3FuErD43/sUL8wKqLMbLle4wZPI2NmMLQdRllyYeGvS4YKj04cOuc1ZZml7UbQUkut4QHA1VJN2dMFMRLuB48a4aT5yzCK1X8ZxuLNkXg7X74bWbBXi6jYe22/exV/78XAfUZ2eCzw0LdF+IswI2YBuN/XHNfGLaiCD9qoW7288UfqBgdQlGJ0kugReLjTLop+H9/uvApl8f3sNtrx2j1GTKAOhS2QdSyFSDnQ//m4drSr3FM5HggUjg6tF6a+BR8+UlKZD3mKgZWdEWXghW5tNhKJU5eKEHRcFrNKzDkD7QuTsv5v1a2lxhELmBKwjS5efodHobtgYUs5Sqamr+PWLD6NPJ/aAPz5EOFB4v/3WQpi10WkxmiuGatQygCLa+Jz30XiGMBN1fwW6tSuY14AqpYnizqYr0lbAOCENzNJWctjcSEDDM7suSj3DAMT04aqWWb3GENqB//2ot9CwHBfaXsF48A6lmDgeKPyf/n5quw0beFpRcN1revpYMZrOoEg=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTcyMTkyMDcwMTgzOCwKICAicHJvZmlsZUlkIiA6ICJjMTJkMmY5ZWJhZGI0ZTllYTIxZmM2M2M3YWY3M2E5NSIsCiAgInByb2ZpbGVOYW1lIiA6ICJEcmVhbXlOZW9uIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzhhZjYxMGQ1YzRhYTgyOTQzY2ZiYjQ3YjQzN2FkYjg0ZTBmNGUzNzgxZGJmZWQ3MGE3ZjE4ZTYwZDc3OGM1NmYiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-688.5, 116, -35.5, -101, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (isInDialogue(player)) return;
        setDialogue(player, "idle");
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
            DialogueSet.builder().key("idle").lines(
                "How do you do! Are you from <2>Galatea<f>?",
                "Wonderful pl-",
                "Did you just say \"Gala-tee\"?",
                "No, no! It's <o>Gala-té-à</o><f>!",
                "A lot of folks get it wrong but now you know better!"
            ).build()
        };
    }
}
