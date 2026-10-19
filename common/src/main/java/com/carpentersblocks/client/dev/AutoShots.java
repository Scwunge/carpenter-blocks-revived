package com.carpentersblocks.client.dev;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.RecipeBookType;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.logging.LogUtils;

/**
 * Dev-only: with {@code -Dcarpentersblocks.autoshots=<script>}, runs a script of commands and screenshots in a
 * singleplayer world, then quits. It lets the render and the screens be checked without anyone driving the game.
 * Script lines:
 * <ul>
 * <li>{@code cmd <command>}: run on the integrated server, as the player, at op level</li>
 * <li>{@code wait <ticks>}</li>
 * <li>{@code screen inventory} (the inventory, recipe book open), {@code screen inventory plain} (recipe book
 * closed, leaving room for a recipe viewer), {@code screen close}, or a screen the loader adds,
 * such as {@code screen config} and {@code screen mods}</li>
 * <li>{@code hud on} / {@code hud off}: show the HUD in shots (it starts hidden), clearing any toasts</li>
 * <li>{@code mouse <x> <y>}, {@code click <x> <y>}: move or left-click the mouse, in GUI coordinates (the log gives
 * the scale from screenshot pixels)</li>
 * <li>{@code type <text>}, {@code key <name>}: type text, or press a key by its {@code key.keyboard.<name>} name
 * ({@code enter}, {@code escape}, ...)</li>
 * <li>{@code shot <name>}: a screenshot, to screenshots/<name>.png</li>
 * <li>{@code ifmod <mod id> <line>}: the line, only with that mod loaded</li>
 * <li>{@code # comment}</li>
 * </ul>
 * Input goes through the game's own mouse and keyboard handlers, so mods that hook those (recipe viewers) see it.
 */
public final class AutoShots {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PROPERTY = "carpentersblocks.autoshots";

    private final Deque<String> lines;
    private final Map<String, UnaryOperator<Screen>> screens;
    private final Predicate<String> modLoaded;
    private int wait = 200; // let the world load and the chunks mesh
    private boolean started;

    private AutoShots(Deque<String> lines, Map<String, UnaryOperator<Screen>> screens, Predicate<String> modLoaded) {
        this.lines = lines;
        this.screens = screens;
        this.modLoaded = modLoaded;
    }

    /**
     * The script the property names, or null when there is none; tick it at the end of every client tick.
     *
     * @param screens extra screens for {@code screen <name>}, each made from the screen to go back to
     * @param modLoaded whether a mod is loaded, for {@code ifmod}
     */
    @Nullable
    public static AutoShots fromProperty(Map<String, UnaryOperator<Screen>> screens, Predicate<String> modLoaded) {
        String script = System.getProperty(PROPERTY);
        if (script == null) {
            return null;
        }
        try {
            return new AutoShots(new ArrayDeque<>(Files.readAllLines(Path.of(script))), screens, modLoaded);
        } catch (IOException e) {
            LOGGER.error("autoshots: can't read {}", script, e);
            return null;
        }
    }

    public void tick(Minecraft mc) {
        if (mc.player == null || mc.getSingleplayerServer() == null) {
            return;
        }
        if (wait > 0) {
            --wait;
            return;
        }
        if (!started) {
            started = true;
            mc.options.hideGui = true;
            Window window = mc.getWindow();
            LOGGER.info("autoshots: GUI {}x{} (scale {}), screenshots {}x{}", window.getGuiScaledWidth(),
                    window.getGuiScaledHeight(), window.getGuiScale(), window.getWidth(), window.getHeight());
        }
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
            if (line.startsWith("ifmod ")) {
                String[] parts = line.split(" ", 3);
                if (parts.length < 3 || !modLoaded.test(parts[1])) {
                    continue;
                }
                line = parts[2];
            }
            String arg = line.indexOf(' ') < 0 ? "" : line.substring(line.indexOf(' ') + 1);
            try {
                run(mc, line, arg);
            } catch (ReflectiveOperationException | RuntimeException e) {
                LOGGER.error("autoshots: {} failed", line, e);
            }
        }
    }

    private void run(Minecraft mc, String line, String arg) throws ReflectiveOperationException {
        if (line.startsWith("cmd ")) {
            command(mc, arg);
        } else if (line.startsWith("wait ")) {
            wait = Integer.parseInt(arg);
        } else if (line.startsWith("screen inventory")) {
            mc.player.getRecipeBook().setOpen(RecipeBookType.CRAFTING, !arg.endsWith("plain"));
            mc.setScreen(new InventoryScreen(mc.player));
        } else if (line.equals("screen close")) {
            mc.setScreen(null);
        } else if (line.startsWith("screen ") && screens.containsKey(arg)) {
            mc.setScreen(screens.get(arg).apply(mc.screen));
        } else if (line.startsWith("hud ")) {
            mc.options.hideGui = !arg.equals("on");
            mc.getToasts().clear(); // the tutorial and recipe toasts would cover the top right
        } else if (line.startsWith("mouse ")) {
            move(mc, arg);
        } else if (line.startsWith("click ")) {
            move(mc, arg);
            press(mc, GLFW.GLFW_PRESS);
            press(mc, GLFW.GLFW_RELEASE);
        } else if (line.startsWith("type ")) {
            Method charTyped = mc.keyboardHandler.getClass().getDeclaredMethod("charTyped", long.class, int.class,
                    int.class);
            charTyped.setAccessible(true);
            for (int codePoint : arg.codePoints().toArray()) {
                charTyped.invoke(mc.keyboardHandler, mc.getWindow().getWindow(), codePoint, 0);
            }
        } else if (line.startsWith("key ")) {
            int key = InputConstants.getKey("key.keyboard." + arg).getValue();
            mc.keyboardHandler.keyPress(mc.getWindow().getWindow(), key, 0, GLFW.GLFW_PRESS, 0);
            mc.keyboardHandler.keyPress(mc.getWindow().getWindow(), key, 0, GLFW.GLFW_RELEASE, 0);
        } else if (line.startsWith("shot ")) {
            Screenshot.grab(mc.gameDirectory, arg + ".png", mc.getMainRenderTarget(),
                    message -> LOGGER.info("autoshots: {}", message.getString()));
            wait = 2;
        } else {
            LOGGER.warn("autoshots: unknown line {}", line);
        }
    }

    /** Moves the mouse to GUI coordinates "x y", as the window would report it. */
    private static void move(Minecraft mc, String xy) throws ReflectiveOperationException {
        String[] parts = xy.split(" ");
        Window window = mc.getWindow();
        double x = Double.parseDouble(parts[0]) * window.getScreenWidth() / window.getGuiScaledWidth();
        double y = Double.parseDouble(parts[1]) * window.getScreenHeight() / window.getGuiScaledHeight();
        Method onMove = mc.mouseHandler.getClass().getDeclaredMethod("onMove", long.class, double.class,
                double.class);
        onMove.setAccessible(true);
        onMove.invoke(mc.mouseHandler, window.getWindow(), x, y);
    }

    private static void press(Minecraft mc, int action) throws ReflectiveOperationException {
        Method onPress = mc.mouseHandler.getClass().getDeclaredMethod("onPress", long.class, int.class, int.class,
                int.class);
        onPress.setAccessible(true);
        onPress.invoke(mc.mouseHandler, mc.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT, action, 0);
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
