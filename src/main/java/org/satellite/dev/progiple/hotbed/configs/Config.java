package org.satellite.dev.progiple.hotbed.configs;

import lombok.experimental.UtilityClass;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.novasparkle.lunaspring.Configuration.IConfig;
import org.novasparkle.lunaspring.Util.Utils;
import org.satellite.dev.progiple.hotbed.Hotbed;

import java.util.HashMap;
import java.util.List;
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

    private final Map<CommandSender, Long> timer = new HashMap<>();
    @SuppressWarnings("deprecation")
    public void sendMessage(CommandSender sender, String id, String... replacements) {
        if (timer.containsKey(sender)) {
            long time = timer.get(sender);
            if (System.currentTimeMillis() - time < 500) return;
        }

        String message = messages.get(id);
        if (message == null || message.isEmpty() || message.startsWith("NONE")) return;

        byte index = 0;
        for (String replacement : replacements) {
            message = message.replace("{" + index + "}", replacement);
            index++;
        }

        if (message.startsWith("HOTBAR")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.sendActionBar(message.replace("HOTBAR", ""));
            }
            return;
        }
        sender.sendMessage(message);
        timer.put(sender, System.currentTimeMillis());
    }

    public String getString(String path) {
        return Utils.color(config.getString(path));
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
