# Swofty AntiCheat

[<img src="https://discordapp.com/assets/e4923594e694a21542a489471ecffa50.svg" alt="" height="55" />](https://discord.gg/atlasmc)

An advanced prediction-based anticheat for Minestom servers. Features comprehensive packet tracking, movement prediction
with physics simulation, and lag compensation.

## Table of contents

* [Features](#features)
* [Getting Started](#getting-started)
* [Minestom Loader](#minestom-loader)
* [API Usage](#api-usage)
  * [Custom Modifiers](#custom-modifiers)
  * [Player Data Storage](#player-data-storage)
  * [Check Bypasses](#check-bypasses)
* [License](#license)
* [Credits](#credits)

## Features

### Prediction-Based Movement System
- Physics-accurate movement simulation with gravity, friction, and velocity modifiers
- 14 velocity modifiers (sprint, speed effects, soul speed, depth strider, soul sand, cobweb, honey, bubble columns, levitation, slow falling, sneak, using item, dolphins grace)
- 3 friction modifiers (ice, blue ice, slime blocks)
- Modular modifier architecture with priority-based application

### Advanced Packet Tracking (14 packet types)
- **Movement packets**: Position, rotation, position+rotation
- **Player state**: Entity actions (sprint/sneak/elytra), abilities (flying state)
- **Combat**: Use entity (attack/interact), animation (arm swing)
- **Block interaction**: Block dig (mining), block place
- **Inventory**: Window click, held item change
- **Vehicle**: Steer vehicle (boats/horses)
- **Network**: Ping/pong for latency tracking

### Lag Compensation
- Transaction-based snapshot system
- Ping uncertainty calculations
- Lag spike detection and check skipping
- Per-player latency tracking

### Detection Flags (15 checks)
- **Movement**: Prediction, Speed, Flight, Strafe, Phase, Jesus
- **Combat**: Reach, Velocity, KillAura, Aim
- **Misc**: Timer, AutoClicker, BadPackets, OnGroundSpoof, TimeoutPingPackets

### Cross-Platform Support
- Full Minestom support with native packet API
- Unified packet abstraction layer
- Platform-specific scheduler managers

## Getting Started

SwoftyAnticheat requires Java 25+ and Minestom.

## Minestom Loader
To use SwoftyAnticheat with Minestom, create a new instance of the MinestomLoader and initialize the anticheat:

```java
MinestomLoader minestomLoader = new MinestomLoader();

SwoftyAnticheat.loader(minestomLoader);
SwoftyAnticheat.values(new SwoftyValues());
SwoftyAnticheat.start(); // Do this after MinecraftServer#init()
```

The MinestomLoader automatically registers all packet listeners using Minestom's native packet API.

## API Usage

SwoftyAnticheat provides a comprehensive API for extending the anticheat with custom functionality.

### Custom Modifiers

You can register custom velocity and friction modifiers to extend the movement prediction system. This is useful for custom game mechanics, special abilities, or temporary speed boosts.

#### Example: Speed Boost Item

```java
import net.swofty.anticheat.api.AnticheatAPI;
import net.swofty.anticheat.prediction.modifier.VelocityModifier;
import net.swofty.anticheat.prediction.PlayerContext;
import net.swofty.anticheat.math.Vel;

public class SpeedBoostModifier extends VelocityModifier {
    @Override
    public boolean shouldApply(PlayerContext context) {
        // Check if player has active speed boost
        return AnticheatAPI.getPlayerData(context.getUuid(), "speed_boost") != null;
    }

    @Override
    public Vel apply(Vel velocity, PlayerContext context) {
        // Get boost multiplier from player data
        Double multiplier = AnticheatAPI.getPlayerData(context.getUuid(), "speed_boost");
        return velocity.mul(multiplier);
    }

    @Override
    public int getPriority() {
        return 100; // Higher priority = applied first
    }
}

// Register the modifier on startup
AnticheatAPI.registerVelocityModifier(new SpeedBoostModifier());
```

#### Example: Slippery Block

```java
import net.swofty.anticheat.api.AnticheatAPI;
import net.swofty.anticheat.prediction.modifier.FrictionModifier;
import net.swofty.anticheat.prediction.PlayerContext;

public class SlipperyBlockModifier extends FrictionModifier {
    @Override
    public boolean shouldApply(PlayerContext context) {
        // Check if player is on a custom slippery block
        return context.getBlockBelow().getType().equals("CUSTOM_SLIPPERY_BLOCK");
    }

    @Override
    public float getFriction(PlayerContext context) {
        return 0.99f; // Very slippery (like blue ice)
    }

    @Override
    public int getPriority() {
        return 50;
    }
}

// Register the modifier
AnticheatAPI.registerFrictionModifier(new SlipperyBlockModifier());
```

### Player Data Storage

Store and retrieve custom data associated with players. Data is automatically cleaned up when players disconnect.

#### Example: Speed Boost Management

```java
import net.swofty.anticheat.api.AnticheatAPI;
import java.util.UUID;

public class SpeedBoostManager {
    public void giveSpeedBoost(UUID player, double multiplier, long durationMs) {
        // Store speed boost data
        AnticheatAPI.setPlayerData(player, "speed_boost", multiplier);

        // Set temporary bypass for speed checks
        AnticheatAPI.setBypass(player, FlagType.SPEED, durationMs);

        // Schedule removal after duration
        scheduler.schedule(() -> {
            AnticheatAPI.removePlayerData(player, "speed_boost");
        }, durationMs);
    }

    public void removeSpeedBoost(UUID player) {
        AnticheatAPI.removePlayerData(player, "speed_boost");
        AnticheatAPI.removeBypass(player, FlagType.SPEED);
    }

    public boolean hasSpeedBoost(UUID player) {
        return AnticheatAPI.getPlayerData(player, "speed_boost") != null;
    }
}
```

### Check Bypasses

Temporarily or permanently disable specific checks for players. Useful for admin modes, spectator mode, or custom game mechanics.

#### Example: Admin Mode

```java
import net.swofty.anticheat.api.AnticheatAPI;
import net.swofty.anticheat.detection.FlagType;
import java.util.UUID;

public class AdminModeManager {
    public void enableAdminMode(UUID player) {
        // Bypass all movement checks permanently
        AnticheatAPI.setBypass(player, FlagType.SPEED);
        AnticheatAPI.setBypass(player, FlagType.FLIGHT);
        AnticheatAPI.setBypass(player, FlagType.PHASE);
        AnticheatAPI.setBypass(player, FlagType.PREDICTION);

        // Mark player as in admin mode
        AnticheatAPI.setPlayerData(player, "admin_mode", true);
    }

    public void disableAdminMode(UUID player) {
        // Remove all bypasses
        AnticheatAPI.clearBypasses(player);
        AnticheatAPI.removePlayerData(player, "admin_mode");
    }
}
```

#### Example: Temporary Teleport Bypass

```java
import net.swofty.anticheat.api.AnticheatAPI;
import net.swofty.anticheat.detection.FlagType;
import java.util.UUID;

public class TeleportHandler {
    public void teleportPlayer(UUID player, Location target) {
        // Bypass movement checks for 2 seconds after teleport
        long bypassDuration = 2000; // 2 seconds in milliseconds

        AnticheatAPI.setBypass(player, FlagType.PREDICTION, bypassDuration);
        AnticheatAPI.setBypass(player, FlagType.SPEED, bypassDuration);
        AnticheatAPI.setBypass(player, FlagType.FLIGHT, bypassDuration);

        // Perform teleport
        player.teleport(target);
    }
}
```

#### Example: Custom Game Mode Bypass

```java
import net.swofty.anticheat.api.AnticheatAPI;
import net.swofty.anticheat.detection.FlagType;
import java.util.UUID;

public class GameModeManager {
    public void startParkourMode(UUID player) {
        // Allow higher jump heights in parkour mode
        AnticheatAPI.setPlayerData(player, "parkour_mode", true);

        // Bypass only specific checks, keep others active
        AnticheatAPI.setBypass(player, FlagType.FLIGHT);

        // Don't bypass speed/phase - keep anti-cheat active for those
    }

    public void stopParkourMode(UUID player) {
        AnticheatAPI.removePlayerData(player, "parkour_mode");
        AnticheatAPI.removeBypass(player, FlagType.FLIGHT);
    }
}
```

### API Reference

#### AnticheatAPI Methods

**Custom Modifiers:**
- `registerVelocityModifier(VelocityModifier modifier)` - Register a custom velocity modifier
- `registerFrictionModifier(FrictionModifier modifier)` - Register a custom friction modifier
- `unregisterVelocityModifier(Class<? extends VelocityModifier> modifierClass)` - Unregister a velocity modifier
- `unregisterFrictionModifier(Class<? extends FrictionModifier> modifierClass)` - Unregister a friction modifier

**Player Data:**
- `setPlayerData(UUID uuid, String key, Object value)` - Store custom data for a player
- `getPlayerData(UUID uuid, String key)` - Retrieve custom data for a player
- `removePlayerData(UUID uuid, String key)` - Remove specific data for a player
- `clearPlayerData(UUID uuid)` - Clear all custom data for a player

**Bypasses:**
- `setBypass(UUID uuid, FlagType flagType, long durationMs)` - Set a temporary bypass
- `setBypass(UUID uuid, FlagType flagType)` - Set a permanent bypass
- `removeBypass(UUID uuid, FlagType flagType)` - Remove a bypass for a specific check
- `hasBypass(UUID uuid, FlagType flagType)` - Check if a player has an active bypass
- `clearBypasses(UUID uuid)` - Clear all bypasses for a player

## License
SwoftyAnticheat's directory is licensed under the permissive MIT license. Please see [`LICENSE.txt`](https://github.com/Swofty-Developments/HypixelRecreation/blob/master/anticheat/LICENSE.md) for more information.

## Credits
Thanks to:
* Myself and any other contributors, which can be viewed on this Git page.
