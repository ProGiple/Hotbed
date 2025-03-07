package org.satellite.dev.progiple.hotbed.configs.menuCfg;

import lombok.experimental.UtilityClass;
import org.bukkit.configuration.ConfigurationSection;

@UtilityClass
public class MainMenuConfig {
    private final IMenuConfig config;
    static {
        config = new IMenuConfig("main.yml");
    }

    public void reload() {
        config.reload();
    }

    public String getTitle() {
        return config.getTitle();
    }

    public byte getSize() {
        return config.getSize();
    }

    public ConfigurationSection getSection(String path) {
        return config.getSection(path);
    }
}
