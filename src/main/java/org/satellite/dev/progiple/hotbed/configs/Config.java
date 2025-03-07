package org.satellite.dev.progiple.hotbed.configs;

import lombok.experimental.UtilityClass;
import org.bukkit.configuration.ConfigurationSection;
import org.novasparkle.lunaspring.Configuration.IConfig;
import org.satellite.dev.progiple.hotbed.Hotbed;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class Config {
    private final IConfig config;
    private final Map<String, String> messages = new HashMap<>();
    static {
        config = new IConfig(Hotbed.getPlugin());
        reload();
    }

    public void reload() {
        Hotbed.getPlugin().saveDefaultConfig();
        config.reload(Hotbed.getPlugin());

        messages.clear();
        for (String id : config.getSection("messages").getKeys(false)) {
            messages.put(id, getString(String.format("messages.%s", id)));
        }
    }

    public String getMessage(String id) {
        return messages.get(id);
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }
}
