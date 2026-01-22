package org.satellite.dev.progiple.hotbed.spawners.menus.buttons.realized;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.novasparkle.lunaspring.API.menus.items.Item;
import org.satellite.dev.progiple.hotbed.configs.SpawnerConfig;
import org.satellite.dev.progiple.hotbed.spawners.menus.buttons.Button;

public class CloseButton extends Button {
    public CloseButton(ConfigurationSection section, SpawnerConfig spawnerConfig) {
        super(section, spawnerConfig);
    }

    @Override
    public Item onClick(InventoryClickEvent e) {
        e.getWhoClicked().closeInventory();
        return this;
    }
}
