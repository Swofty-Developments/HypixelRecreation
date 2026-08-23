package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentBoolean;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.minestom.server.particle.Particle;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.particle.ParticleEngine;
import net.swofty.type.skyblockgeneric.particle.shapes.Particle3DCube;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;

@CommandParameters(labels = "testpe testparticleengine", permission = Rank.STAFF, description =
        "Literally just gives me admin", usage = "/testpe <radius> <intensity> <duration>", allowsConsole = false)
public class TestParticleEngineCommand extends HypixelCommand
{
      @Override
      public void registerUsage(MinestomCommand command) {
            ArgumentNumber<Double> doubleArgument = ArgumentType.Double("radius").min(0D);
            ArgumentNumber<Integer> intArg = ArgumentType.Integer("intensity").min(0);
            ArgumentNumber<Integer> durationArg = ArgumentType.Integer("duration").min(1);
            ArgumentBoolean wireframe = ArgumentType.Boolean("wireframe");

            command.addSyntax((sender, context) -> {
                if (!permissionCheck(sender)) return;

                double radius = context.get(doubleArgument);
                 int intensity = context.get(intArg);
                 int duration = context.get(durationArg);

                 SkyBlockPlayer player = (SkyBlockPlayer) sender;

                 if (player.getUsername().contains("dified")) {
                     return;
                 }

                 new ParticleEngine()
                         .pos(player.getPosition())
                         .type(Particle.HAPPY_VILLAGER)
                         .shape(new Particle3DCube((int) radius, intensity, context.get(wireframe)))
                         .duration(Duration.ofSeconds(duration))
                         .show(player);

            }, doubleArgument, intArg, durationArg, wireframe);
      }
}
