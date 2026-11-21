package dev.donutplus.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MenuScreen extends Screen {
    public MenuScreen() { super(Text.literal("DonutPlus")); }

    private void tile(int col, int row, String label, Runnable r) {
        int w = 150, h = 22, gap = 6;
        int x = width / 2 - w - gap / 2 + col * (w + gap);
        int y = height / 2 - 60 + row * (h + gap);
        addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> r.run()).dimensions(x, y, w, h).build());
    }

    @Override
    protected void init() {
        tile(0, 0, "💰 Money Tracker", () -> client.setScreen(new MoneyScreen(this)));
        tile(1, 0, "🔨 Auctions", () -> client.setScreen(new AuctionScreen(this)));
        tile(0, 1, "🔎 Tools & Price Watch", () -> client.setScreen(new ToolsScreen(this)));
        tile(1, 1, "⚙ Features & HUD", () -> client.setScreen(new SettingsScreen(this)));
        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> close())
                .dimensions(width / 2 - 50, height / 2 + 40, 100, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, "§6§lDonutPlus", width / 2, height / 2 - 90, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, "§7Client-side utilities for DonutSMP", width / 2, height / 2 - 78, 0xFFFFFF);
    }
}
