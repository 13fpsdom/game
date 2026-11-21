# DonutPlus (Fabric, client-side, MC 1.21.4)

![build](https://github.com/YOUR_USER/donutplus/actions/workflows/build.yml/badge.svg)

Open the menu with **Right Shift**. Features:

- **Money tracker** – balance (from /bal), session / 24h / 7d income & spending, sell income per hour, transaction history (config/donutplus_ledger.json)
- **Auctions** – /pay-based bidding with a live HUD, winner announcement and history
- **Price watch** – scans the open container (e.g. /ah) and pings when a keyword item is at/below your per-item price; per-item price shown in tooltips
- **HUD** – balance, session profit, totem count, coords, auction timer, low-armor warning
- **Pay/TPA guard** – holds /pay above a threshold and anything to names on your watch list until you press Y
- **Ad blocker** – regex list in config
- **Command hotkeys** – 4 bindable keys, one command per press

## Install
Needs Minecraft 1.21.4, Fabric Loader and Fabric API. Download the jar from the Releases page into your `mods` folder.

## Build
Needs JDK 21 and Gradle 8.12+. Run `gradle build` (or generate a wrapper once with `gradle wrapper` and commit it,
then use `./gradlew build`). The jar ends up in `build/libs/`. GitHub Actions builds every push automatically.

## Releasing
`git tag v1.0.0 && git push --tags` builds the mod and attaches the jar to a GitHub Release.

## Important
The chat regexes in `donutplus.json` are best guesses. Check the real DonutSMP payment/balance/sell
messages and edit `recvRegex`, `sentRegex`, `sellRegex`, `balanceRegex` if numbers don't track.
Check DonutSMP's rules before using any client mod.
