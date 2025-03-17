package org.satellite.dev.progiple.hotbed.spawners.handlers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.novasparkle.lunaspring.Util.managers.NBTManager;
import org.satellite.dev.progiple.hotbed.configs.SpawnerConfig;

public class PlaceSpawnerHandler implements Listener {
    @EventHandler
    public void onPlaceSpawner(BlockPlaceEvent e) {
        Block block = e.getBlock();
        BlockState state = block.getState();
        if (block.getType() != Material.SPAWNER || !(state instanceof CreatureSpawner)) return;

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
