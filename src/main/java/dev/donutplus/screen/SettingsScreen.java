package dev.donutplus.screen;

import dev.donutplus.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class SettingsScreen extends Screen {
    private final Screen parent;
    private int idx = 0;

    public SettingsScreen(Screen parent) { super(Text.literal("Features")); this.parent = parent; }

    private void toggle(String label, boolean cur, Consumer<Boolean> set) {
        int w = 150, x = width / 2 - w - 3 + (idx % 2) * (w + 6), y = 28 + (idx / 2) * 24;
        addDrawableChild(CyclingButtonWidget.onOffBuilder(cur).build(x, y, w, 20, Text.literal(label), (b, v) -> { set.accept(v); Config.save(); }));
        idx++;
    }

    @Override
    protected void init() {
        idx = 0;
        Config c = Config.I;
        toggle("HUD", c.hud, v -> c.hud = v);
        toggle("Balance", c.hudBalance, v -> c.hudBalance = v);
        toggle("Session", c.hudSession, v -> c.hudSession = v);
        toggle("Totems", c.hudTotems, v -> c.hudTotems = v);
        toggle("Coords", c.hudCoords, v -> c.hudCoords = v);
        toggle("Auction HUD", c.hudAuction, v -> c.hudAuction = v);
        toggle("Armor warning", c.armorWarn, v -> c.armorWarn = v);
        toggle("Per-item price", c.unitPrice, v -> c.unitPrice = v);
        toggle("Price watch", c.priceWatch, v -> c.priceWatch = v);
        toggle("Pay/TPA guard", c.payGuard, v -> c.payGuard = v);
        toggle("Ad blocker", c.adBlock, v -> c.adBlock = v);
        toggle("Run on any server", c.forceEnable, v -> c.forceEnable = v);
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> client.setScreen(parent))
                .dimensions(width / 2 - 50, height - 28, 100, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, "§6§lFeatures & HUD", width / 2, 10, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, "§8More options (regexes, HUD position): config/donutplus.json", width / 2, height - 42, 0xFFFFFF);
    }
}
