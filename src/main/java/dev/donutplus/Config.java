package dev.donutplus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Config {
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("donutplus.json");
    public static Config I = new Config();

    // general
    public boolean forceEnable = false;      // run on non-DonutSMP servers too
    // HUD
    public boolean hud = true, hudBalance = true, hudSession = true, hudTotems = true, hudCoords = false, hudAuction = true;
    public boolean armorWarn = true;
    public int armorWarnPercent = 10;
    public int hudX = 4, hudY = 4;
    // trading
    public boolean unitPrice = true;
    public boolean priceWatch = false;
    public String watchKeyword = "";
    public double watchMaxUnit = 0;
    // safety
    public boolean payGuard = true;
    public double payConfirmAbove = 1_000_000;
    public List<String> guardNames = new ArrayList<>();
    public boolean adBlock = true;
    public List<String> adPatterns = new ArrayList<>(List.of("(?i)discord\\.gg/", "(?i)store\\.donutsmp"));
    // hotkeys: each press sends ONE command (no automation)
    public String[] hotkeyCommands = {"ah", "bal", "shop", "sell"};
    // chat parsing - verify against real DonutSMP messages and edit in donutplus.json
    public String recvRegex = "(\\w{3,16}) (?:has )?paid you \\$([\\d.,]+[KMBkmb]?)";
    public String sentRegex = "You (?:have )?paid (\\w{3,16}) \\$([\\d.,]+[KMBkmb]?)";
    public String sellRegex = "(?i)you sold .*? for \\$([\\d.,]+[KMBkmb]?)";
    public String balanceRegex = "(?i)(?:balance|money)[: ]+\\$([\\d.,]+[KMBkmb]?)";

    public static void load() {
        try {
            if (Files.exists(PATH)) {
                Config c = GSON.fromJson(Files.readString(PATH), Config.class);
                if (c != null) I = c;
            }
        } catch (Exception e) { e.printStackTrace(); }
        save();
    }

    public static void save() {
        try { Files.writeString(PATH, GSON.toJson(I)); } catch (Exception e) { e.printStackTrace(); }
    }
}
