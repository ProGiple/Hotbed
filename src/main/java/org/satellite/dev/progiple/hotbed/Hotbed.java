package org.satellite.dev.progiple.hotbed;

import lombok.Getter;
import org.novasparkle.lunaspring.LunaPlugin;
import org.satellite.dev.progiple.hotbed.configs.SpawnerConfig;

public final class Hotbed extends LunaPlugin {
    @Getter private static Hotbed INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
        super.onEnable();

        this.loadFiles("menus/main.yml", "menus/container_page.yml", "mobs.yml");
        saveDefaultConfig();
        SpawnerConfig.load();

        this.processListeners();
        this.registerTabExecutor(new Command(), "hotbed");
    }

    @Override
    public void onDisable() {
        SpawnerConfig.getSpawnerCfgs().values().forEach(c -> c.getRunnable().cancel());
        super.onDisable();
    }
}
