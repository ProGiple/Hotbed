package org.satellite.dev.progiple.hotbed.spawners.menus.realized;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.novasparkle.lunaspring.API.menus.MenuManager;
import org.novasparkle.lunaspring.API.menus.MoveIgnored;
import org.novasparkle.lunaspring.API.menus.items.Item;
import org.satellite.dev.progiple.hotbed.configs.SpawnerConfig;
import org.satellite.dev.progiple.hotbed.configs.menuCfg.MenuConfig;
import org.satellite.dev.progiple.hotbed.spawners.menus.HMenu;
import org.satellite.dev.progiple.hotbed.spawners.menus.buttons.Button;
import org.satellite.dev.progiple.hotbed.spawners.menus.buttons.realized.CloseButton;
import org.satellite.dev.progiple.hotbed.spawners.menus.buttons.realized.CollectExpButton;
import org.satellite.dev.progiple.hotbed.spawners.menus.buttons.realized.SwitchHologramButton;

@MoveIgnored
public class MainMenu extends HMenu {
    public MainMenu(Player player, SpawnerConfig spawnerConfig) {
        super(player, MenuConfig.getMain(), spawnerConfig);

        ConfigurationSection itemsSection = this.getMenuConfig().getSection("items.clickable");
        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);

            Button button = null;
            switch (key) {
                case "STORAGE" -> button = new Button(itemSection, spawnerConfig) {
                    @Override
                    public Item onClick(InventoryClickEvent e) {
                        MenuManager.openInventory(new StorageMenu(player, spawnerConfig, 1));
                        return this;
                    }
                };
                case "COLLECT_EXP" -> button = new CollectExpButton(itemSection, spawnerConfig);
                case "CLOSE" -> button = new CloseButton(itemSection, spawnerConfig);
                case "SWITCH_HOLOGRAM" -> button = new SwitchHologramButton(itemSection, spawnerConfig);
            }
            if (button != null) this.getButtons().add(button);
        }
    }

    @Override
    public void onOpen(InventoryOpenEvent e) {
        this.getButtons().forEach(button -> button.insert(this));
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        this.processClick(e);
    }

    @Override
    public void onClose(InventoryCloseEvent e) {
    }

    @Override
    public void onDrag(InventoryDragEvent e) {
        e.setCancelled(true);
    }
}
