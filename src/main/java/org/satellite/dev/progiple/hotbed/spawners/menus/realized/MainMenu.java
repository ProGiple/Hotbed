package org.satellite.dev.progiple.hotbed.spawners.menus.realized;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.novasparkle.lunaspring.API.menus.MenuManager;
import org.novasparkle.lunaspring.API.menus.items.Item;
import org.satellite.dev.progiple.hotbed.configs.Config;
import org.satellite.dev.progiple.hotbed.configs.SpawnerConfig;
import org.satellite.dev.progiple.hotbed.configs.menuCfg.MenuConfig;
import org.satellite.dev.progiple.hotbed.spawners.menus.HMenu;
import org.satellite.dev.progiple.hotbed.spawners.menus.buttons.Button;

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
                        MenuManager.openInventory(player, new StorageMenu(player, spawnerConfig, 1));
                        return this;
                    }
                };
                case "COLLECT_EXP" -> button = new Button(itemSection, spawnerConfig) {
                    @Override
                    public Item onClick(InventoryClickEvent event) {
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
                };
                case "CLOSE" -> button = new Button(itemSection, spawnerConfig) {
                    @Override
                    public Item onClick(InventoryClickEvent event) {
                        player.closeInventory();
                        return this;
                    }
                };
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
        if (this.cancelNums(e)) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        e.setCancelled(true);
        for (Button button : this.getButtons()) {
            if (button.getItemStack().equals(item) && button.getSlot() == e.getSlot()) {
                button.onClick(e);
                return;
            }
        }
    }

    @Override
    public void onClose(InventoryCloseEvent e) {
    }

    @Override
    public void onDrag(InventoryDragEvent inventoryDragEvent) {

    }
}
