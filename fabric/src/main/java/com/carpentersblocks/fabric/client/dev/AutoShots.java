package com.carpentersblocks.fabric.client.dev;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.RecipeBookType;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

/**
 * Dev-only: with {@code -Dcarpentersblocks.autoshots=<script>}, runs a script of commands and screenshots in a
 * singleplayer world, then quits. It lets the render be checked without anyone driving the game. Script lines:
 * {@code cmd <command>} (run on the integrated server, as the player, at op level), {@code wait <ticks>},
 * {@code screen inventory} (the inventory, recipe book open), {@code screen close},
 * {@code shot <name>} (a screenshot with the HUD hidden, to screenshots/<name>.png) and {@code # comment}.
 */
public final class AutoShots {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PROPERTY = "carpentersblocks.autoshots";

    private final Deque<String> lines;
    private int wait = 200; // let the world load and the chunks mesh

    private AutoShots(Deque<String> lines) {
        this.lines = lines;
    }

    public static void registerIfRequested() {
        String script = System.getProperty(PROPERTY);
        if (script == null) {
            return;
        }
        try {
            AutoShots shots = new AutoShots(new ArrayDeque<>(Files.readAllLines(Path.of(script))));
            ClientTickEvents.END_CLIENT_TICK.register(shots::tick);
        } catch (IOException e) {
            LOGGER.error("autoshots: can't read {}", script, e);
        }
    }

    private void tick(Minecraft mc) {
        if (mc.player == null || mc.getSingleplayerServer() == null) {
            return;
        }
        if (wait > 0) {
            --wait;
            return;
        }
        mc.options.hideGui = true;
        while (wait == 0) {
            String line = lines.poll();
            if (line == null) {
                LOGGER.info("autoshots: done");
                mc.stop();
                return;
            }
            line = line.strip();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String arg = line.indexOf(' ') < 0 ? "" : line.substring(line.indexOf(' ') + 1);
            if (line.startsWith("cmd ")) {
                command(mc, arg);
            } else if (line.startsWith("wait ")) {
                wait = Integer.parseInt(arg);
            } else if (line.equals("screen inventory")) {
                mc.player.getRecipeBook().setOpen(RecipeBookType.CRAFTING, true);
                mc.setScreen(new InventoryScreen(mc.player));
            } else if (line.equals("screen close")) {
                mc.setScreen(null);
            } else if (line.startsWith("shot ")) {
                Screenshot.grab(mc.gameDirectory, arg + ".png", mc.getMainRenderTarget(),
                        message -> LOGGER.info("autoshots: {}", message.getString()));
                wait = 2;
            } else {
                LOGGER.warn("autoshots: unknown line {}", line);
            }
        }
    }

    private static void command(Minecraft mc, String command) {
        MinecraftServer server = mc.getSingleplayerServer();
        var uuid = mc.player.getUUID();
        server.execute(() -> {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player != null) {
                server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4),
                        command);
            }
        });
    }
}
