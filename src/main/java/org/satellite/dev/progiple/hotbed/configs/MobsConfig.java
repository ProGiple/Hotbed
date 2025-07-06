package org.satellite.dev.progiple.hotbed.configs;

import lombok.experimental.UtilityClass;
import org.bukkit.configuration.ConfigurationSection;
import org.novasparkle.lunaspring.API.configuration.Configuration;
import org.satellite.dev.progiple.hotbed.Hotbed;

import java.io.File;

@UtilityClass
public class MobsConfig {
    public final Configuration config;
    static {
        config = new Configuration(new File(Hotbed.getINSTANCE().getDataFolder(), "mobs.yml"));
    }

    public void reload() {
        config.reload();
    }

    public ConfigurationSection getSection(String path) {
        return config.getSection(path);
    }
}
