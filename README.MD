# LagGoggles <img height="70" src="docs/logo.png">
Intuitive lag hunting!

LagGoggles is a profiler where you can see timings in the world, visually. It comes with a custom GUI using a configurable keybind. It's compatible with [SpongeForge](https://github.com/SpongePowered/SpongeForge) and [Mixin](https://github.com/SpongePowered/Mixin)!

### Download: [here](https://minecraft.curseforge.com/projects/laggoggles/files)

## Screenshots

| LagGoggles enabled | normal view |
| --- | --- |
| ![alt text](docs/2-enabled.png "With laggoggles") | ![alt text](docs/2-disabled.png "Without laggoggles") |
| ![alt text](docs/1-enabled.png "With laggoggles") | ![alt text](docs/1-disabled.png "Without laggoggles") |
| ![alt text](docs/3-enabled.png "With laggoggles") | ![alt text](docs/3-disabled.png "Without laggoggles") |

![alt text](docs/menu.png "LagGoggles menu")
![alt text](docs/menu-5sec.png "LagGoggles menu")
![alt text](docs/timingsmenu.png "Analyzer")

## Features
* Cross dimensional teleporting
* Sponge compatible (Not required)
* Client GUI, including overlay and clickable menus
* Colour scale for easy spotting of sources of lag.
* Calculations are done on the client, so each client can have a different colour scale.

## Configuration
* Drop the jar in the /mods folder
* Serverside configuration is not required and has no effect.
* Clients can edit the config to change the colour scale.

## How it works
At startup [Mixin](https://github.com/SpongePowered/Mixin) handles each class being loaded. On the server, this mod hooks into the loading mechanism. This is the same mechanism [SpongeForge](https://github.com/SpongePowered/SpongeForge) uses, that's why it's compatible (but not required!).<br>
<br>
Entities are timed using System.nanotime() before and after the tick() and update() methods. These times are being recorded and sent to all online operators when the profiler is done.

## Benefits
* Visualisation of Tile entities and Entities.

## Dependencies
As of version 2.0, all dependencies are included in a single jar.
