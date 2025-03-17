package org.satellite.dev.progiple.hotbed.spawners.handlers;

import eu.decentsoftware.holograms.api.DHAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.novasparkle.lunaspring.Menus.IMenu;
import org.novasparkle.lunaspring.Menus.MenuManager;
import org.satellite.dev.progiple.hotbed.Tools;
import org.satellite.dev.progiple.hotbed.configs.Config;
import org.satellite.dev.progiple.hotbed.configs.SpawnerConfig;
import org.satellite.dev.progiple.hotbed.spawners.menus.HMenu;

import java.util.Objects;

public class SpawnerBreakHandler implements Listener {
    @EventHandler
    public void onBreakSpawner(BlockBreakEvent e) {
        Block block = e.getBlock();
        Player player = e.getPlayer();

        BlockState state = block.getState();
        if (block.getType() != Material.SPAWNER || !(state instanceof CreatureSpawner)) return;

        CreatureSpawner creatureSpawner = (CreatureSpawner) state;
        EntityType entityType = creatureSpawner.getSpawnedType();
        if (entityType == null) entityType = EntityType.ZOMBIE;

        Location location = block.getLocation();
        SpawnerConfig spawnerConfig = SpawnerConfig.getSpawnerCfgs().getOrDefault(location, null);
        if (spawnerConfig != null) {
            if (!Tools.isOwner(player, spawnerConfig)) {
                Config.sendMessage(player, "youNotOwner");
                return;
            }

            ConfigurationSection section = spawnerConfig.getStorageSection();
            for (String key : section.getKeys(false)) {
                if (!Objects.requireNonNull(section.getConfigurationSection(key)).getKeys(false).isEmpty()) {
                    Config.sendMessage(player, "hasLootInside");
                    e.setCancelled(true);
                    return;
                }
            }
        }

        for (IMenu value : MenuManager.getActiveInventories().values()) {
            if (value instanceof HMenu) {
                HMenu hMenu = (HMenu) value;
                if (hMenu.getSpawnerConfig().getLocation().equals(location)) {
                    hMenu.getPlayer().closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                    break;
                }
            }
        }
        if (spawnerConfig != null || Tools.getPlayerChance(player) / 100 >= Math.random()) {
            int spawnerLevel = spawnerConfig == null ? 1 : spawnerConfig.getInt("level");

            location.getWorld().dropItem(location, Tools.getVirtualSpawner(entityType, player.getName(), spawnerLevel));
            if (spawnerConfig != null) spawnerConfig.delete();

            Config.sendMessage(player, "spawnerReceived");
        }
        else Config.sendMessage(player, "spawnerBroken");
    }
}
