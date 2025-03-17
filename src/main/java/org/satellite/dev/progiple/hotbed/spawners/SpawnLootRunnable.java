package org.satellite.dev.progiple.hotbed.spawners;

import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;
import org.satellite.dev.progiple.hotbed.Hotbed;
import org.satellite.dev.progiple.hotbed.Tools;
import org.satellite.dev.progiple.hotbed.configs.Config;
import org.satellite.dev.progiple.hotbed.configs.MobsConfig;
import org.satellite.dev.progiple.hotbed.configs.SpawnerConfig;
import org.satellite.dev.progiple.hotbed.configs.menuCfg.MenuConfig;

import java.util.Objects;
import java.util.Random;

public class SpawnLootRunnable extends BukkitRunnable {
    private final Random random = new Random();
    private final SpawnerConfig spawnerConfig;

    @Setter private ConfigurationSection mobSection;
    public SpawnLootRunnable(SpawnerConfig spawnerConfig) {
        this.spawnerConfig = spawnerConfig;
        this.mobSection = MobsConfig.getSection(String.format("mobs.%s", spawnerConfig.getString("mob")));
    }

    @Override
    public void run() {
        Location loc = this.spawnerConfig.getLocation();
        if (this.mobSection == null ||
                Bukkit.getOnlinePlayers()
                        .stream()
                        .noneMatch(player -> {
                            Location plLoc = player.getLocation();
                            return plLoc.getWorld().equals(loc.getWorld()) && plLoc.distance(loc) <= 50;
                        })) return;

        int storageSize = Tools.getStorageSize(this.spawnerConfig);
        int level = this.spawnerConfig.getInt("level");
        int amountLimit = Config.getInt("settings.maxLootItemsLimitPerLevel") * level;
        int expLimit = Config.getInt("settings.maxExpLimitPerLevel") * level;
        int maxSlot = MenuConfig.getContainer_page().getInt("maxLootInPage");
        int exp = this.spawnerConfig.getInt("exp");

        if (storageSize >= amountLimit || exp >= expLimit) return;
        ConfigurationSection storageSection = this.spawnerConfig.getStorageSection();
        for (String loot : this.mobSection.getStringList("lootlist")) {
            String[] split = loot.split(";");

            int amount = 0;
            if (split.length >= 2) {
                String[] splitAmount = split[1].split("-");
                amount = splitAmount.length >= 2 ? this.random.nextInt(Integer.parseInt(splitAmount[1])
                        - Integer.parseInt(splitAmount[0])) + Integer.parseInt(splitAmount[0]) : Integer.parseInt(splitAmount[0]);
                amount *= level;
            }

            if (amount > 0) {
                int maxPage = 0;
                for (String page : storageSection.getKeys(false)) {
                    ConfigurationSection pageSection = storageSection.getConfigurationSection(page);
                    int intPage = Integer.parseInt(page);
                    if (intPage > maxPage) maxPage = intPage;
                    assert pageSection != null;

                    int focusedSlots = 0;
                    if (amount == 0 || focusedSlots + pageSection.getKeys(false).size() >= maxSlot) break;
                    for (int i = 0; i < maxSlot; i++) {
                        if (amount == 0 || focusedSlots + pageSection.getKeys(false).size() >= maxSlot) break;

                        if (pageSection.getKeys(false).contains(String.valueOf(i))) {
                            String value = pageSection.getString(String.valueOf(i));
                            if (value == null || value.isEmpty()) continue;

                            String[] itemSplit = value.split(";");
                            if (itemSplit[0].equalsIgnoreCase(split[0])) {
                                int itemAmount = itemSplit.length >= 2 ? Integer.parseInt(itemSplit[1]) : 1;
                                if (itemAmount < 64) {
                                    int spaceLeft = 64 - itemAmount;
                                    if (amount <= spaceLeft) {
                                        itemAmount += amount;
                                        amount = 0;
                                    } else {
                                        itemAmount = 64;
                                        amount -= spaceLeft;
                                    }
                                    this.spawnerConfig.set(String.format("storage.%s.%s", page, i),
                                            String.format("%s;%s", split[0].toUpperCase(), itemAmount));
                                }
                            }
                        } else {
                            int am = Math.min(amount, 64);
                            this.spawnerConfig.set(String.format("storage.%s.%s", page, i),
                                    String.format("%s;%s", split[0].toUpperCase(), am));
                            amount -= am;
                            focusedSlots++;
                        }
                    }
                }

                if (maxPage > 0 && amount > 0) {
                    int slot = 0;
                    while (amount > 0) {
                        int am = Math.min(amount, 64);
                        this.spawnerConfig.set(String.format("storage.%s.%s", maxPage + 1, slot),
                                String.format("%s;%s", split[0].toUpperCase(), am));
                        amount -= am;
                    }
                }
            }
        }

        String[] split = Objects.requireNonNull(this.mobSection.getString("exp")).split("-");
        int generatedExp = split.length >= 2 ? this.random.nextInt(Integer.parseInt(split[1])
                - Integer.parseInt(split[0])) + Integer.parseInt(split[0]) : Integer.parseInt(split[0]);
        this.spawnerConfig.set("exp", Math.min(exp + generatedExp * level, expLimit));
        this.spawnerConfig.save();
        this.spawnerConfig.updateHologram();
        Tools.updateStorageMenu(this.spawnerConfig);
    }

    public void start() {
        long timer = Config.getInt("settings.spawnLootTimer") * 20L;
        this.runTaskTimerAsynchronously(Hotbed.getPlugin(), timer, timer);
    }
}
