package org.satellite.dev.progiple.hotbed.spawners.menus.buttons.realized;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.novasparkle.lunaspring.API.menus.items.Item;
import org.satellite.dev.progiple.hotbed.configs.Config;
import org.satellite.dev.progiple.hotbed.configs.SpawnerConfig;
import org.satellite.dev.progiple.hotbed.spawners.menus.buttons.Button;

public class CollectExpButton extends Button {
    public CollectExpButton(ConfigurationSection section, SpawnerConfig spawnerConfig) {
        super(section, spawnerConfig);
    }

    @Override
    public Item onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        SpawnerConfig spawnerConfig = this.getSpawnerConfig();

        int exp = spawnerConfig.getInt("exp");
        if (exp <= 0) {
            Config.sendMessage(player, "noExp");
            return this;
        }

        player.giveExp(exp);
        spawnerConfig.set("exp", 0);
        spawnerConfig.save();
        spawnerConfig.updateHologram();

        Config.sendMessage(player, "collectExp", String.valueOf(exp));
        player.closeInventory();
        return this;
    }
}
