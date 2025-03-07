package org.satellite.dev.progiple.hotbed.configs.menuCfg;

import lombok.experimental.UtilityClass;
import org.bukkit.configuration.ConfigurationSection;

@UtilityClass
public class ContainerPageConfig {
    private final IMenuConfig config;
    static {
        config = new IMenuConfig("container_page.yml");
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
