package org.satellite.dev.progiple.hotbed;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.satellite.dev.progiple.hotbed.configs.Config;
import org.satellite.dev.progiple.hotbed.configs.MobsConfig;
import org.satellite.dev.progiple.hotbed.configs.SpawnerConfig;
import org.satellite.dev.progiple.hotbed.configs.menuCfg.MenuConfig;

import java.util.List;
import java.util.stream.Collectors;

public class Command implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, org.bukkit.command.@NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender.hasPermission("hotbed.admin")) {
            if (strings.length >= 1) {
                if (strings[0].equals("give")) {
                    if (strings.length >= 3) {
                        Player player = Bukkit.getPlayerExact(strings[1]);
                        if (player != null && player.isOnline()) {
                            EntityType entityType;
                            String strType = strings[2].toUpperCase();

                            ConfigurationSection section = MobsConfig.getSection("mobs");
                            entityType = section.getKeys(false).contains(strType) ? EntityType.valueOf(strType) :
                                    EntityType.valueOf(
                                            section.getKeys(false)
                                                    .stream()
                                                    .findFirst()
                                                    .orElse("ZOMBIE"));

                            int level = strings.length >= 4 ? Integer.parseInt(strings[3]) : 1;
                            ItemStack spawner = Tools.getVirtualSpawner(entityType, player.getName(), level);
                            player.getInventory().addItem(spawner);

                            Config.sendMessage(commandSender, "givingSpawner",
                                    strings[1], Tools.getMobName(strType), String.valueOf(level));
                        }
                        else Config.sendMessage(commandSender, "playerIsOffline", strings[1]);
                    }
                    else return false;
                }
                else if (strings[0].equals("reload")) {
                    Config.reload();
                    MobsConfig.reload();
                    MenuConfig.getContainer_page().reload();
                    MenuConfig.getMain().reload();
                    SpawnerConfig.getSpawnerCfgs().values().forEach(SpawnerConfig::reload);
                    Tools.updateStorageMenus();
                    Config.sendMessage(commandSender, "reload");
                }
            }
            else return false;
        }
        else Config.sendMessage(commandSender, "noPermission");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, org.bukkit.command.@NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1) {
            return List.of("reload", "give");
        }
        else if (strings.length == 2 && strings[0].equals("give")) {
            return Bukkit.getOnlinePlayers()
                    .stream()
                    .map(Player::getName)
                    .filter(nick -> nick.toUpperCase().startsWith(strings[1].toUpperCase()))
                    .collect(Collectors.toList());
        }
        else if (strings.length == 3 && strings[0].equals("give")) {
            return MobsConfig.getSection("mobs").getKeys(false)
                    .stream()
                    .filter(type -> type.startsWith(strings[2].toUpperCase()))
                    .collect(Collectors.toList());
        }
        else if (strings.length == 4 && strings[0].equals("give")) {
            return List.of("<level>");
        }
        return List.of();
    }
}
