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
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.novasparkle.lunaspring.API.commands.annotations.LunaHandler;
import org.novasparkle.lunaspring.API.util.service.managers.NBTManager;
import org.satellite.dev.progiple.hotbed.configs.SpawnerConfig;

@LunaHandler
public class PlaceSpawnerHandler implements Listener {
    @EventHandler
    public void onPlaceSpawner(BlockPlaceEvent e) {
        Block block = e.getBlock();
        BlockState state = block.getState();
        if (block.getType() != Material.SPAWNER || !(state instanceof CreatureSpawner)) return;

        Location location = new Location(BukkitAdapter.adapt(block.getWorld()), block.getX(), block.getY(), block.getZ());
        Player player = e.getPlayer();

        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        if (query.testBuild(location, localPlayer, Flags.BUILD) || query.testBuild(location, localPlayer, Flags.BLOCK_PLACE)) {
            ItemStack item = e.getItemInHand();
            if (NBTManager.hasTag(item, "mob")) {
                CreatureSpawner creatureSpawner = (CreatureSpawner) state;
                creatureSpawner.setDelay(999999999);

                String mob = NBTManager.getString(item, "mob");
                creatureSpawner.setSpawnedType(EntityType.valueOf(mob));
                creatureSpawner.update();

                SpawnerConfig spawnerConfig = new SpawnerConfig(block.getLocation(), mob);
                spawnerConfig.set("level", NBTManager.getInt(item, "level"));
                spawnerConfig.set("owner", e.getPlayer().getName());
                spawnerConfig.save();
                spawnerConfig.updateHologram();
            }
        }
    }
}
