# TerrainFinder

[![Build Status](http://jenkins.daporkchop.net/job/Minecraft/job/TerrainFinder/job/master/badge/icon)](http://jenkins.daporkchop.net/job/Minecraft/job/TerrainFinder)
[![Discord](https://img.shields.io/discord/428813657816956929.svg)](https://discord.gg/FrBHHCk)

# IMPORTANT! READ THIS FIRST!

After quite a bit of debugging, I've come to the unfortunate realization that this implementation, as well as ChromeCrusher's
original code, only works for specific biomes. Any biome that doesn't use the default `generateBiomeTerrain` method, as
well as any biome that uses any type of sand as a filler (beach and desert) will simply produce different results ingame.

I'm going to think hard about the best way to get around this, but I have a feeling I'll end up having to re-implement the
entire terrain generator in OpenCL or something.

# Original README follows

An optimized Java implementation of ChromeCrusher's bedrock pattern finder with a GUI.

## Building

1. [Download](https://maven.apache.org/download.cgi) and [Install](https://maven.apache.org/install.html) Maven ([Extra info for Windows](https://maven.apache.org/guides/getting-started/windows-prerequisites.html). Remember to add Maven `bin/` to your Path after extracting)
2. Clone the repository to your machine: `git clone https://github.com/DaMatrix/TerrainFinder`
3. `cd` to the clone directory
4. Run `mvn package`
5. The compiled Jar will be in the `target/` directory (choose the one that isn't prefixed with `original-`).

## Using

1. Double-click the `.jar` file or run `java -jar jarfile.jar` in your terminal.
2. The program displays a 16x16 grid representing a [chunk](https://minecraft.gamepedia.com/Chunk) in Minecraft. Click each square to set one of three states:

   - **Empty** (No bedrock at given location)
   - **Check** (There is bedrock at given location)
   - **Square** (Wildcard; use if unsure if there is bedrock or not)

3. Click Start. Wait for the program to find a chunk.

## Troubleshooting

- Make sure you have the correct **Rotation** set in Options.
- If your computer lags while using this, lower **Threads** in Options.
