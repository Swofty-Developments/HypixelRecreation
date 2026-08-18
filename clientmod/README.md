# Client Mod

This is a 26.2 mod meant for the developers of this project to easily capture data from Minecraft servers.

## Features

## Full capture

`/fullcapture start <name>` records everything the client sees until you run `/fullcapture stop`. It is meant
for building a baseline of a whole gamemode: start it, play a few games, stop it, and hand the folder to
whatever is doing the recreation.

- `/fullcapture start <name>` starts a session in `.minecraft/full-captures/<name>_<timestamp>`.
- `/fullcapture start <name> raw` also keeps entity movement packets and full container refreshes.
- `/fullcapture stop` finishes the session and converts every captured world to Polar.
- `/fullcapture stop nopolar` skips the Polar conversion, which is the slow part.
- `/fullcapture status` prints worlds, chunks, events and packet counts so far.
- `/fullcapture split [label]` forces a new world segment instead of waiting for a world change.
- `/fullcapture note <text>` writes a marker into the timeline, e.g. `note the game just started`.

Every world you walk through becomes its own segment under `worlds/`, saved as both Anvil region files and a
Polar world; switching worlds never overwrites an earlier one, and a transition that lands back in the world
you just left (a death, a rejoin) is merged instead of duplicated. Alongside the worlds it records GUIs and
every item and component in them, your inventory, chat, the scoreboard, tab, boss bars, titles, sounds,
particles, block changes, entities, and your own clicks and commands, one JSON object per line under
`streams/`. `session.json` indexes the lot and `README.md` inside the session explains the format.

- `/getskins <radius>` Gets the information of player's or NPC's skins in the defined radius.
- `/getarmorstandcolors <radius>` Gets the color information of armor stands in the defined radius.
- `/getarmorstandinfos <radius>` Gets the position information of armor stands in the defined radius. (wip)
- `/getscoreboardinfo` Prints out the scoreboard information in chat.
- `/copymaptexture [all]` Copy's the texture of a map to the clipboard.
- `/chunkexporter start` starts a chunk-only export.
- `/chunkexporter start block_displays` includes stationary block and item displays.
- `/chunkexporter start ravengard <name>` starts or resumes a named stitched Ravengard capture. Stop it with
  `/chunkexporter stop <name>`; checkpoints are stored in `.minecraft/chunkexporter_sessions`.
- `/chunkexporter status` shows the active capture counts.
- `/nbsrecord start`, `/nbsrecord stop <name>`, `/nbsrecord status` records inbound server sounds to
  `nbs-recordings/<name>.nbs` and exports custom instrument sounds.
- `Keybind K` Copies the texture ID of the hovered player head into the clipboard.
- `Keybind L` Copies the lore of the hovered item.
- `Keybind I` Selects the entity under the crosshair and opens a client-only inspector for inbound packets referencing
  it, its passengers/vehicle, or entities within 1.5 blocks.
