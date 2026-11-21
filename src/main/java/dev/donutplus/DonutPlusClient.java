package dev.donutplus;

import dev.donutplus.screen.MenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DonutPlusClient implements ClientModInitializer {
    static final String CAT = "category.donutplus";
    static KeyBinding menuKey, confirmKey;
    static final KeyBinding[] hotkeys = new KeyBinding[4];
    static String heldCommand = null;
    static boolean bypass = false;
    static Object lastScreen = null;
    static final Set<String> alerted = new HashSet<>();
    static final Pattern PRICE = Pattern.compile("\\$([\\d.,]+[KMBkmb]?)");

    public static boolean active() {
        if (Config.I.forceEnable) return true;
        ServerInfo si = MinecraftClient.getInstance().getCurrentServerEntry();
        return si != null && si.address != null && si.address.toLowerCase().contains("donut");
    }

    @Override
    public void onInitializeClient() {
        Config.load();
        Ledger.load();

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.donutplus.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, CAT));
        confirmKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.donutplus.confirm_held_command", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Y, CAT));
        for (int i = 0; i < 4; i++)
            hotkeys[i] = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.donutplus.command_" + (i + 1), InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CAT));

        HudRenderCallback.EVENT.register((ctx, tick) -> Hud.render(ctx));
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);

        // chat: ad blocker + money tracking
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (overlay || !active()) return true;
            String s = Formatting.strip(message.getString());
            if (s == null) return true;
            Tracker.handle(s);
            if (Config.I.adBlock) {
                for (String r : Config.I.adPatterns) {
                    Pattern p = Util.pattern(r);
                    if (p != null && p.matcher(s).find()) return false;
                }
            }
            return true;
        });

        // pay / tpa guard
        ClientSendMessageEvents.ALLOW_COMMAND.register(cmd -> {
            if (bypass || !active() || !Config.I.payGuard) return true;
            String[] a = cmd.trim().split("\\s+");
            String base = a[0].toLowerCase();
            if (!(base.equals("pay") || base.equals("tpa") || base.equals("tpahere") || base.equals("tpaccept"))) return true;
            boolean flagged = a.length > 1 && Config.I.guardNames.stream().anyMatch(n -> n.equalsIgnoreCase(a[1]));
            boolean big = base.equals("pay") && a.length > 2 && Util.parse(a[2]) >= Config.I.payConfirmAbove;
            if (!flagged && !big) return true;
            heldCommand = cmd;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null)
                mc.player.sendMessage(Text.literal("§c[DonutPlus] Held: §f/" + cmd + " §7- press " + confirmKey.getBoundKeyLocalizedText().getString() + " to confirm"), false);
            return false;
        });

        // unit price in tooltips
        ItemTooltipCallback.EVENT.register((stack, ctxt, type, lines) -> {
            if (!Config.I.unitPrice || stack.getCount() < 2 || !active()) return;
            for (Text t : lines) {
                Matcher m = PRICE.matcher(t.getString());
                if (m.find()) {
                    double unit = Util.parse(m.group(1)) / stack.getCount();
                    lines.add(Text.literal("§7Per item: §a" + Util.fmt(unit)));
                    return;
                }
            }
        });
    }

    void tick(MinecraftClient mc) {
        if (mc.player == null) return;
        Auction.tick();

        while (menuKey.wasPressed()) mc.setScreen(new MenuScreen());

        while (confirmKey.wasPressed()) {
            if (heldCommand != null) {
                bypass = true;
                mc.player.networkHandler.sendChatCommand(heldCommand);
                bypass = false;
                heldCommand = null;
            }
        }

        for (int i = 0; i < 4; i++) {
            while (hotkeys[i].wasPressed()) {
                String cmd = Config.I.hotkeyCommands[i];
                if (cmd != null && !cmd.isBlank() && mc.player != null)
                    mc.player.networkHandler.sendChatCommand(cmd.startsWith("/") ? cmd.substring(1) : cmd);
            }
        }

        priceWatch(mc);
    }

    /** Looks at the open container (e.g. /ah) and pings when an item matches your keyword at or under your max per-item price. */
    void priceWatch(MinecraftClient mc) {
        Config c = Config.I;
        if (mc.player.age % 10 != 0) return;
        if (mc.currentScreen != lastScreen) { alerted.clear(); lastScreen = mc.currentScreen; }
        if (!c.priceWatch || c.watchMaxUnit <= 0 || c.watchKeyword.isBlank() || !active()) return;
        if (!(mc.currentScreen instanceof HandledScreen<?> hs)) return;
        String kw = c.watchKeyword.toLowerCase();
        int containerSlots = hs.getScreenHandler().slots.size() - 36;
        for (int i = 0; i < containerSlots; i++) {
            Slot slot = hs.getScreenHandler().slots.get(i);
            ItemStack st = slot.getStack();
            if (st.isEmpty() || !st.getName().getString().toLowerCase().contains(kw)) continue;
            LoreComponent lore = st.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT);
            for (Text line : lore.lines()) {
                Matcher m = PRICE.matcher(Formatting.strip(line.getString()));
                if (!m.find()) continue;
                double unit = Util.parse(m.group(1)) / Math.max(1, st.getCount());
                String key = i + ":" + unit;
                if (unit <= c.watchMaxUnit && alerted.add(key)) {
                    mc.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
                    mc.player.sendMessage(Text.literal("§a[PriceWatch] §f" + st.getName().getString() + " x" + st.getCount()
                            + " at " + Util.fmt(unit) + " each (slot " + i + ")"), false);
                }
                break;
            }
        }
    }
}
