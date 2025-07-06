package org.satellite.dev.progiple.hotbed.configs;

import lombok.experimental.UtilityClass;
import org.bukkit.command.CommandSender;
import org.novasparkle.lunaspring.API.configuration.IConfig;
import org.novasparkle.lunaspring.API.events.CooldownPrevent;
import org.novasparkle.lunaspring.API.util.service.managers.ColorManager;
import org.satellite.dev.progiple.hotbed.Hotbed;

import java.util.List;

@UtilityClass
public class Config {
    private final IConfig config;
    private final CooldownPrevent<CommandSender> cooldown = new CooldownPrevent<>(400);
    static {
        config = new IConfig(Hotbed.getINSTANCE());
    }

    public void reload() {
        Hotbed.getINSTANCE().saveDefaultConfig();
        config.reload(Hotbed.getINSTANCE());
    }

    public void sendMessage(CommandSender sender, String id, String... replacements) {
        if (cooldown.isCancelled(null, sender)) return;
        config.sendMessage(sender, id, replacements);
    }

    public String getString(String path) {
        return ColorManager.color(config.getString(path));
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public double getDouble(String path) {
        return config.self().getDouble(path);
    }

    public List<String> getStrList(String path) {
        return config.getStringList(path);
    }
}
