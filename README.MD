# LagGoggles <img height="70" src="docs/logo.png">
Intuitive lag hunting!<br>
<br>
# I am currently working on a complete rewrite of this mod with added functionality.
The planned _main_ functionality is listed below:
* **Throttling block ticking based upon tps**, PER PLAYER. This means that every player gets an equal share of tick time on his/her machines. Needs performance testing to see if this is viable. TileEntities will be assigned owners when placed or claimed by a player.
* **Network profiling**, monitor in and outgoing bytes
* **Browser interface instead of in-game**. Allows for multi monitor setups and much more space to fit content in a proper manner.

If you have suggestions, I will take them into consideration.
<br>
<br>
<br>
LagGoggles is a profiler where you can see timings in the world, visually. It comes with a custom GUI using a configurable keybind. It's compatible with [SpongeForge](https://github.com/SpongePowered/SpongeForge) and [Mixin](https://github.com/SpongePowered/Mixin)!<br>
You can use LagGoggles as a player too! This is my attempt to create an understanding of what causes lag on a server to regular players.<br>
I noticed some common misconceptions like having machines spread out over different chunks causes less lag than putting it in one chunk.<br>
With LagGoggles, you can look around and see that alot of small things like pipes or cables cause a tiny bit of lag most of the time, often resulting in more lag than a machine right next to the source would.

LagGoggles can also profile clientside FPS as of version 4.0!

### Download: [here](https://minecraft.curseforge.com/projects/laggoggles/files)

## Screenshots

| LagGoggles enabled | normal view |
| --- | --- |
| ![alt text](docs/2-enabled.png "With laggoggles") | ![alt text](docs/2-disabled.png "Without laggoggles") |
| ![alt text](docs/1-enabled.png "With laggoggles") | ![alt text](docs/1-disabled.png "Without laggoggles") |
| ![alt text](docs/3-enabled.png "With laggoggles") | ![alt text](docs/3-disabled.png "Without laggoggles") |

![alt text](docs/menu.png "LagGoggles menu")
![alt text](docs/menu-5sec.png "LagGoggles menu")
![alt text](docs/sheep.png "Entity view")
![alt text](docs/timingsmenu.png "Analyzer")

## Video explaining the concept
Massive thanks to [Grok DuckFarmer](https://www.youtube.com/channel/UCoKMLbTK35pzF8Lv_oj3KlA) for making this video.


[Minecraft Talk 35 - LagGoggles and Better Server Play](https://youtu.be/2ALrIvw1S4I)

[![IMAGE ALT TEXT HERE](http://img.youtube.com/vi/2ALrIvw1S4I/0.jpg)](http://www.youtube.com/watch?v=2ALrIvw1S4I)

## Features
* Sponge compatible (use the SPONGEFORGE version!)
* Teleporting to sources of lag
* Client GUI, including overlay and clickable menus
* Colour scale for easy spotting of sources of lag.
* Calculations are done on the client, so each client can have a different colour scale.
* Clientside FPS support!
   * Checks render time for Entities, Tile entities and event subscribers
   * It's very young, but it should give some insight in what may cause FPS issues.
* Config for client:
    * Custom gradients
    * Colorblindess support
* Config for server
    * Custom permissions for players (non-operators)

## Configuration
* Drop the jar in the /mods folder
* Serverside configuration is not required and has no effect.
* Clients can edit the config to change the colour scale.

## How it works
At startup [Mixin](https://github.com/SpongePowered/Mixin) handles each class being loaded. On the server side, this mod hooks into the loading mechanism. This is the same mechanism [SpongeForge](https://github.com/SpongePowered/SpongeForge) uses, that's why it's compatible.<br>
<br>
Entities, blocks and event-subscribers are timed using System.nanotime() before and after the tick() and update() methods. These times are being recorded and sent to the client when the profiler is done.

## Benefits
* Visualisation of various sources of lag
* Learn your players to build lag-free bases.

## Dependencies
As of version 3.0, you'll have to pick the right version of LagGoggles.<br>
If you are a player, you can simply install the FORGE edition.<br>
<br>
If you're installing it on the server you have to use either the FORGE or SPONGEFORGE version.<br>
Quick answer: Running SpongeForge? Install the SPONGEFORGE version.<br>
<br>
Elaboration: SpongeForge changes classes at startup in such a way it's completely incompatible with LagGoggles's FORGE edit's on WorldServer.class, therefore I made an alternative WorldServer.class just to be compatible with SpongeForge.
