package org.satellite.dev.progiple.hotbed.spawners.handlers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.novasparkle.lunaspring.API.events.LunaHandler;
import org.novasparkle.lunaspring.API.menus.MenuManager;
import org.novasparkle.lunaspring.API.util.service.managers.NBTManager;
import org.satellite.dev.progiple.hotbed.Tools;
import org.satellite.dev.progiple.hotbed.configs.Config;
import org.satellite.dev.progiple.hotbed.configs.MobsConfig;
import org.satellite.dev.progiple.hotbed.configs.SpawnerConfig;
import org.satellite.dev.progiple.hotbed.spawners.menus.realized.MainMenu;

@LunaHandler
public class ClickOnSpawnerHandler implements Listener {
    @EventHandler
    public void onClickOnBlock(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        Player player = e.getPlayer();
        if (block == null || block.getType() != Material.SPAWNER || e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Location location = block.getLocation();
        SpawnerConfig spawnerConfig = SpawnerConfig.getSpawnerCfgs().getOrDefault(location, null);

        e.setCancelled(true);

        Material hand = e.getMaterial();
        if (spawnerConfig == null) {
            Config.sendMessage(player, "clickOnNatureSpawner", String.valueOf(Tools.getPlayerChance(player)));
            return;
        }

        if (Tools.isNotOwner(player, spawnerConfig)) {
            Config.sendMessage(player, "youNotOwner");
            return;
        }

        ItemStack item = e.getItem();
        assert item != null;
        if (hand.name().contains("_SPAWN_EGG")) {
            String mob = hand.name().replace("_SPAWN_EGG", "");
            spawnerConfig.set("mob", mob);
            spawnerConfig.save();
            spawnerConfig.updateHologram();
            spawnerConfig.getRunnable().setMobSection(MobsConfig.getSection(String.format("mobs.%s", mob)));
            item.setAmount(item.getAmount() - 1);

            CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();
            creatureSpawner.setSpawnedType(EntityType.valueOf(mob));
            creatureSpawner.update();
            return;
        }
        else if (hand == Material.SPAWNER) {
            if (!NBTManager.hasTag(item, "mob") ||
                    !NBTManager.getString(item, "mob").equalsIgnoreCase(spawnerConfig.getString("mob"))) return;

            int maxLevel = Config.getInt("settings.maxSpawnerLevel");
            int oldLevel = spawnerConfig.getInt("level");
            if (oldLevel >= maxLevel) return;

            int level = NBTManager.hasTag(item, "level") ? NBTManager.getInt(item, "level") : 1;
            int newLevel = Math.min(oldLevel + level, maxLevel);

            spawnerConfig.set("level", newLevel);
            spawnerConfig.save();
            spawnerConfig.updateHologram();
            item.setAmount(item.getAmount() - 1);
            Config.sendMessage(player, "levelUp",
                    String.valueOf(oldLevel), String.valueOf(newLevel), String.valueOf(level));
            return;
        }

        MenuManager.openInventory(new MainMenu(player, spawnerConfig));
    }
}
