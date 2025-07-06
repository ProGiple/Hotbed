package org.satellite.dev.progiple.hotbed.spawners;

import com.bgsoftware.wildloaders.api.WildLoaders;
import com.bgsoftware.wildloaders.api.WildLoadersAPI;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.novasparkle.lunaspring.API.util.utilities.LunaMath;
import org.novasparkle.lunaspring.API.util.utilities.LunaTask;
import org.satellite.dev.progiple.hotbed.Tools;
import org.satellite.dev.progiple.hotbed.configs.Config;
import org.satellite.dev.progiple.hotbed.configs.MobsConfig;
import org.satellite.dev.progiple.hotbed.configs.SpawnerConfig;
import org.satellite.dev.progiple.hotbed.configs.menuCfg.MenuConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class SpawnLootRunnable extends LunaTask {
    private final SpawnerConfig spawnerConfig;
    @Setter private ConfigurationSection mobSection;
    public SpawnLootRunnable(SpawnerConfig spawnerConfig) {
        super(0);

        this.spawnerConfig = spawnerConfig;
        this.mobSection = MobsConfig.getSection(String.format("mobs.%s", spawnerConfig.getString("mob")));
        this.updateTimers();
    }

    private int spawnLootTimer;
    private int k;
    private int minLootTimer;

    private int left_seconds = 0;

    public void updateTimers() {
        this.spawnLootTimer = Config.getInt("settings.spawnLootTimer");
        this.k = Config.getInt("settings.reducedCoeff");
        this.minLootTimer = Config.getInt("settings.minLootTimer");
    }

    @Override @SneakyThrows
    @SuppressWarnings("all")
    public void start() {
        while (this.isActive()) {
            Thread.sleep(1000);
            this.generateLoot();
        }
    }

    private void generateLoot() {
        int level = this.spawnerConfig.getInt("level");
        int timer = Math.max(this.spawnLootTimer - (this.k * level), this.minLootTimer);
        if (this.left_seconds < timer) {
            this.left_seconds++;
            return;
        } else this.left_seconds = 0;

        Location loc = this.spawnerConfig.getLocation();
        if (loc.getBlock().getType() != Material.SPAWNER) {
            this.spawnerConfig.delete();
            return;
        }

        if (this.mobSection == null || !this.chunkIsLoaded(loc)) return;

        // 100
        int storageSize = Tools.getStorageSize(this.spawnerConfig);
        int amountLimit = Config.getInt("settings.maxLootItemsLimitPerLevel") * level;
        int expLimit = Config.getInt("settings.maxExpLimitPerLevel") * level;
        int maxSlot = MenuConfig.getContainer_page().getInt("maxLootInPage");
        int exp = this.spawnerConfig.getInt("exp");

        if (storageSize >= amountLimit || exp >= expLimit) return;
        ConfigurationSection storageSection = this.spawnerConfig.getStorageSection();

        List<Integer> keys = new ArrayList<>(storageSection.getKeys(false)
                .stream()
                .map(LunaMath::toInt)
                .sorted()
                .toList());
        for (String loot : this.mobSection.getStringList("lootlist")) {
            String[] settings = loot.split(";");
            String material = settings[0].toUpperCase();

            int amount = 0;
            if (settings.length >= 2) {
                if (settings.length >= 3) {
                    double chance = Double.parseDouble(settings[2].replace("%", "")) / 100;
                    if (Math.random() > chance) continue;
                }

                String[] splitAmount = settings[1].split("-");

                int min = LunaMath.toInt(splitAmount[0]) * level;
                int max = splitAmount.length >= 2 ? LunaMath.toInt(splitAmount[1]) * level : 0;
                amount = max > 0 ? LunaMath.getRandom().nextInt(max - min) + max : min;
            }

            if (amount <= 0) continue;
            if (storageSize + amount > amountLimit) {
                amount = amountLimit - storageSize;
                if (amount <= 0 || amount >= amountLimit) {
                    this.updateSpawner(exp, level, expLimit);
                    return;
                }
            }

            storageSize += amount;
            keys.add(keys.get(keys.size() - 1) + 1);
            for (int page : keys) {
                ConfigurationSection section = storageSection.getConfigurationSection(String.valueOf(page));
                if (section == null) section = storageSection.createSection(String.valueOf(page));

                for (int i = 0; i < maxSlot; i++) {
                    if (amount <= 0) break;

                    String value = section.getString(String.valueOf(i));
                    if (value == null || value.isEmpty()) {
                        int lootAmount = Math.min(amount, 64);

                        value = String.format("%s;%s", material, lootAmount);
                        this.spawnerConfig.set(String.format("storage.%s.%s", page, i), value);

                        amount -= lootAmount;
                        continue;
                    }

                    String[] pageKeyLoot = value.split(";");
                    if (pageKeyLoot.length < 2) continue;

                    if (!pageKeyLoot[0].equalsIgnoreCase(material)) {
                        continue;
                    }

                    int lootAmount = LunaMath.toInt(pageKeyLoot[1]);
                    if (lootAmount >= 64) continue;

                    int maxDifferent = Math.min(amount, 64 - lootAmount);
                    amount -= maxDifferent;

                    lootAmount += maxDifferent;
                    value = String.format("%s;%s", material, lootAmount);
                    this.spawnerConfig.set(String.format("storage.%s.%s", page, i), value);
                }

                if (amount <= 0) break;
            }
        }

        this.updateSpawner(exp, level, expLimit);
    }

    private void updateSpawner(int exp, int level, int expLimit) {
        String[] split = Objects.requireNonNull(this.mobSection.getString("exp")).split("-");
        int generatedExp = split.length >= 2 ? LunaMath.getRandom().nextInt(Integer.parseInt(split[1])
                - Integer.parseInt(split[0])) + Integer.parseInt(split[0]) : Integer.parseInt(split[0]);
        this.spawnerConfig.set("exp", Math.min(exp + generatedExp * level, expLimit));
        this.spawnerConfig.save();
        this.spawnerConfig.updateHologram();
        Tools.updateStorageMenu(this.spawnerConfig);
    }

    private boolean chunkIsLoaded(Location location) {
        int viewDistance = (Bukkit.getSimulationDistance() + 1) * 16;

        World targetWorld = location.getWorld();
        if (Bukkit.getOnlinePlayers()
                .stream()
                .filter(p -> p.getWorld().equals(targetWorld))
                .anyMatch(p -> p.getLocation().distance(location) <= viewDistance)) return true;

        WildLoaders wildLoaders = WildLoadersAPI.getWildLoaders();
        return wildLoaders != null && wildLoaders.getLoaders()
                .getChunkLoaders()
                .stream()
                .filter(c -> c.getLocation().getWorld().equals(targetWorld))
                .anyMatch(c -> c.getLoadedChunksCollection().contains(location.getChunk()));
    }
}
