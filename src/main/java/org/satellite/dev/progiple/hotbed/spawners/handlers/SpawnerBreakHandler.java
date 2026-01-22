package org.satellite.dev.progiple.hotbed.spawners.handlers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.novasparkle.lunaspring.API.events.LunaHandler;
import org.novasparkle.lunaspring.API.menus.MenuManager;
import org.satellite.dev.progiple.hotbed.Tools;
import org.satellite.dev.progiple.hotbed.configs.Config;
import org.satellite.dev.progiple.hotbed.configs.SpawnerConfig;
import org.satellite.dev.progiple.hotbed.spawners.menus.HMenu;

@LunaHandler
public class SpawnerBreakHandler implements Listener {
    @EventHandler
    public void onBreakSpawner(BlockBreakEvent e) {
        Block block = e.getBlock();
        Player player = e.getPlayer();

        BlockState state = block.getState();
        if (block.getType() != Material.SPAWNER) return;

        CreatureSpawner creatureSpawner = (CreatureSpawner) state;

        Location weLocation = new Location(BukkitAdapter.adapt(block.getWorld()), block.getX(), block.getY(), block.getZ());
        org.bukkit.Location location = block.getLocation();

        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        if (query.testBuild(weLocation, localPlayer, Flags.BUILD) || query.testBuild(weLocation, localPlayer, Flags.BLOCK_BREAK)) {
            EntityType entityType = creatureSpawner.getSpawnedType();
            if (entityType == null) entityType = EntityType.ZOMBIE;

            SpawnerConfig spawnerConfig = SpawnerConfig.getSpawnerCfgs().getOrDefault(location, null);
            if (spawnerConfig != null) {
                if (Tools.isNotOwner(player, spawnerConfig)) {
                    Config.sendMessage(player, "youNotOwner");
                    e.setCancelled(true);
                    return;
                }

                if (Tools.getStorageSize(spawnerConfig) > 0) {
                    Config.sendMessage(player, "hasLootInside");
                    e.setCancelled(true);
                    return;
                }

                e.setExpToDrop(spawnerConfig.getInt("exp"));

                MenuManager.getActiveMenus(HMenu.class, false)
                        .filter(m -> m.getSpawnerConfig().equals(spawnerConfig))
                        .forEach(m -> m.getPlayer().closeInventory(InventoryCloseEvent.Reason.PLUGIN));
            }

            if (spawnerConfig != null || Tools.getPlayerChance(player) / 100 >= Math.random()) {
                int spawnerLevel = spawnerConfig == null ? 1 : spawnerConfig.getInt("level");

                location.getWorld().dropItem(location, Tools.getVirtualSpawner(entityType, player.getName(), spawnerLevel));
                if (spawnerConfig != null) spawnerConfig.delete();

                Config.sendMessage(player, "spawnerReceived");
            }
            else
                Config.sendMessage(player, "spawnerBroken");
        }
        else {
            Config.sendMessage(player, "breakInRegion");
            e.setCancelled(true);
        }
    }
}
