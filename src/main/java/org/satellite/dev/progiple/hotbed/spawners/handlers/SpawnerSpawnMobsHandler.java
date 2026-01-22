package org.satellite.dev.progiple.hotbed.spawners.handlers;

import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.novasparkle.lunaspring.API.events.LunaHandler;
import org.satellite.dev.progiple.hotbed.configs.SpawnerConfig;

@LunaHandler
public class SpawnerSpawnMobsHandler implements Listener {
    @EventHandler
    public void onSpawnerSpawnMobs(SpawnerSpawnEvent e) {
        CreatureSpawner spawner = e.getSpawner();
        if (spawner == null) return;

        Location location = spawner.getLocation();
        if (!SpawnerConfig.getSpawnerCfgs().containsKey(location)) return;

        e.setCancelled(true);
    }
}
