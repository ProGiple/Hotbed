package org.satellite.dev.progiple.hotbed;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class Hotbed extends JavaPlugin {
    @Getter private static Hotbed plugin;

    @Override
    public void onEnable() {
        plugin = this;
    }

    @Override
    public void onDisable() {
    }
}
