package dev.donutplus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reads chat lines and feeds the ledger + auction. */
public class Tracker {
    public static void handle(String line) {
        Config c = Config.I;
        Matcher m = match(c.recvRegex, line);
        if (m != null) {
            String who = m.group(1); double amt = Util.parse(m.group(2));
            Ledger.add("IN", who, amt);
            Auction.onPayment(who, amt);
            return;
        }
        m = match(c.sentRegex, line);
        if (m != null) { Ledger.add("OUT", m.group(1), Util.parse(m.group(2))); return; }
        m = match(c.sellRegex, line);
        if (m != null) { Ledger.add("SELL", "shop", Util.parse(m.group(1))); return; }
        m = match(c.balanceRegex, line);
        if (m != null) Ledger.balance = Util.parse(m.group(1));
    }

    private static Matcher match(String regex, String line) {
        Pattern p = Util.pattern(regex);
        if (p == null) return null;
        Matcher m = p.matcher(line);
        return m.find() ? m : null;
    }
}
