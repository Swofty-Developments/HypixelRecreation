rootProject.name = "HypixelSkyBlock"
include(":packer")
include(":commons")
include(":dungeons")

include(":velocity.extension")
include(":proxy.api")

include(":service.generic")
include(":type.generic")
include(":type.skyblockgeneric")
include(":type.lobby")
include(":type.game")
include(":spark")
include(":loader")
include(":pvp")

include(":type.prototypelobby")
include(":type.thefarmingislands")
include(":type.spidersden")
include(":type.theend")
include(":type.crimsonisle")
include(":type.goldmine")
include(":type.deepcaverns")
include(":type.dwarvenmines")
include(":type.thepark")
include(":type.galatea")
include(":type.backwaterbayou")
include(":type.jerrysworkshop")
include(":type.island")
include(":type.garden")
include(":type.hub")
include(":type.dungeonhub")
include(":type.bedwarslobby")
include(":type.bedwarsgame")
include(":type.bedwarsconfigurator")
include(":type.murdermysterylobby")
include(":type.murdermysterygame")
include(":type.murdermysteryconfigurator")
include(":type.skywarslobby")
include(":type.skywarsgame")
include(":type.skywarsconfigurator")
include(":type.ravengardgeneric")
include(":type.ravengardlobby")
include(":type.ravengarddungeon")
include(":type.mainlobby")

include(":type.replayviewer")
include(":service.auctionhouse")
include(":service.bazaar")
include(":service.itemtracker")
include(":service.api")
include(":service.datamutex")
include(":service.party")
include(":service.orchestrator")
include(":service.darkauction")
include(":service.jacobscontest")
include(":service.friend")
include(":service.replay")
include(":service.punishment")
include(":service.elections")
include(":service.guild")
include(":service.store")
include(":anticheat")

val clientModRequested = gradle.startParameter.taskNames.any { taskName ->
    taskName == "clientmod" ||
        taskName == ":clientmod" ||
        taskName.startsWith("clientmod:") ||
        taskName.startsWith(":clientmod:")
}

if (clientModRequested || gradle.startParameter.projectProperties.containsKey("includeClientmod")) {
    include(":clientmod")
}

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
    }
}
