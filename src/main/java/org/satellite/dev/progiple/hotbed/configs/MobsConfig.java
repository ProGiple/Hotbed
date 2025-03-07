package org.satellite.dev.progiple.hotbed.configs;

import lombok.experimental.UtilityClass;
import org.bukkit.configuration.ConfigurationSection;
import org.novasparkle.lunaspring.Configuration.Configuration;
import org.satellite.dev.progiple.hotbed.Hotbed;

import java.io.File;

@UtilityClass
public class MobsConfig {
    public final Configuration config;
    static {
        Hotbed.getPlugin().saveResource("mobs.yml", false);
        config = new Configuration(new File(Hotbed.getPlugin().getDataFolder(), "mobs.yml"));
    }

    public void reload() {
        config.reload();
    }

    public ConfigurationSection getSection(String path) {
        return config.getSection(path);
    }
}
