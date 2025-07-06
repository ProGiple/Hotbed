package org.satellite.dev.progiple.hotbed.configs;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.novasparkle.lunaspring.API.configuration.Configuration;
import org.novasparkle.lunaspring.API.util.service.managers.ColorManager;
import org.novasparkle.lunaspring.API.util.utilities.Utils;
import org.satellite.dev.progiple.hotbed.Hotbed;
import org.satellite.dev.progiple.hotbed.Tools;
import org.satellite.dev.progiple.hotbed.spawners.SpawnLootRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnerConfig {
    @Getter private final static Map<Location, SpawnerConfig> spawnerCfgs = new HashMap<>();

    public static void load() {
        File dir = new File(Hotbed.getINSTANCE().getDataFolder(), "spawners");
        if (!dir.exists() || !dir.isDirectory()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.exists() && !file.isDirectory()) {
                new SpawnerConfig(file);
            }
        }
    }

    private final Configuration config;
    @Getter private final Location location;
    @Getter private final SpawnLootRunnable runnable;

    @Getter private Hologram hologram;
    public SpawnerConfig(Location location, String mobType) {
        this.config = new Configuration(new File(Hotbed.getINSTANCE().getDataFolder(),
                String.format("spawners/%s.yml", Tools.getStrLoc(location))));
        this.location = location;
        this.set("exp", 0);
        this.set("level", 1);
        this.set("mob", mobType);

        this.config.createSection("storage", "1");
        this.save();

        this.runnable = new SpawnLootRunnable(this);
        this.runnable.runTaskAsynchronously(Hotbed.getINSTANCE());
        spawnerCfgs.put(this.location, this);
    }

    public SpawnerConfig(File file) {
        this.config = new Configuration(file);

        String[] strLoc = file.getName().replace(".yml", "").split(";");
        this.location = new Location(Bukkit.getWorld(strLoc[0]),
                Integer.parseInt(strLoc[1]),Integer.parseInt(strLoc[2]), Integer.parseInt(strLoc[3]));
        this.updateHologram();

        this.runnable = new SpawnLootRunnable(this);
        this.runnable.runTaskAsynchronously(Hotbed.getINSTANCE());
        spawnerCfgs.put(this.location, this);
    }

    public void reload() {
        this.config.reload();
        DHAPI.removeHologram(this.hologram.getName());
        this.hologram = null;
        this.updateHologram();
        this.runnable.updateTimers();
    }

    public void updateHologram() {
        int level = this.getInt("level");
        int maxExp = Config.getInt("settings.maxExpLimitPerLevel") * level;
        int maxItems = Config.getInt("settings.maxLootItemsLimitPerLevel") * level;
        int storagedItems = Tools.getStorageSize(this);
        int exp = this.getInt("exp");

        String mainPath = String.format("settings.%s_holo", maxExp <= exp || maxItems <= storagedItems ? "fulled" : "default");
        if (this.hologram == null) this.hologram = DHAPI.createHologram(Utils.getRKey((byte) 24),
                this.location.clone().add(0.5, Config.getDouble(mainPath + "_height"), 0.5));
        else {
            this.hologram.removePage(0);
            this.hologram.addPage();
        }
        List<String> lines = new ArrayList<>(Config.getStrList(mainPath));

        String mob_type = Tools.getMobName(this.getString("mob"));
        String player = this.getString("owner");
        lines.forEach(line -> {
            if (line.startsWith("Material.")) {
                Material material = Material.getMaterial(line.replace("Material.",""));
                if (material != null) DHAPI.addHologramLine(this.hologram, material);
            }
            else DHAPI.addHologramLine(this.hologram, ColorManager.color(line
                    .replace("%mob_type%", mob_type))
                    .replace("%level%", String.valueOf(level))
                    .replace("%owner%", player == null || player.isEmpty() ? "NONE" : player)
                    .replace("%items%", String.valueOf(storagedItems))
                    .replace("%max_items%", String.valueOf(maxItems))
                    .replace("%exp%", String.valueOf(exp))
                    .replace("%max_exp%", String.valueOf(maxExp)));
        });
    }

    public int getInt(String path) {
        return this.config.getInt(path);
    }

    public String getString(String path) {
        return this.config.getString(path);
    }

    public ConfigurationSection getStorageSection() {
        return this.config.getSection("storage");
    }

    public void set(String path, Object o) {
        this.config.set(path, o);
    }

    public void save() {
        this.config.save();
    }

    public void delete() {
        if (!this.config.getFile().delete()) System.out.println("file wasn't deleted");
        SpawnerConfig.getSpawnerCfgs().remove(this.location);

        this.runnable.cancel();
        Bukkit.getScheduler().cancelTask(this.runnable.getTaskId());

        if (this.hologram != null) DHAPI.removeHologram(this.hologram.getName());
    }
}
