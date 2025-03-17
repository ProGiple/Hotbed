package org.satellite.dev.progiple.hotbed.spawners.menus.realized;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.novasparkle.lunaspring.Menus.MenuManager;
import org.novasparkle.lunaspring.Util.Utils;
import org.satellite.dev.progiple.hotbed.configs.Config;
import org.satellite.dev.progiple.hotbed.configs.SpawnerConfig;
import org.satellite.dev.progiple.hotbed.configs.menuCfg.MenuConfig;
import org.satellite.dev.progiple.hotbed.spawners.menus.HMenu;
import org.satellite.dev.progiple.hotbed.spawners.menus.LootItem;
import org.satellite.dev.progiple.hotbed.spawners.menus.buttons.Button;

import java.util.*;

@Getter
public class StorageMenu extends HMenu {
    private final int page;
    private final List<LootItem> loot = new ArrayList<>();
    public StorageMenu(Player player, SpawnerConfig spawnerConfig, int page) {
        super(player, MenuConfig.getContainer_page(), spawnerConfig);
        ConfigurationSection storage = spawnerConfig.getStorageSection();
        this.page = page;

        ConfigurationSection itemsSection = this.getMenuConfig().getSection("items.clickable");
        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            Button button = new Button(itemSection, spawnerConfig) {
                @Override
                public void click(Player player) {
                    switch (key) {
                        case "NEXT_PAGE" -> {
                            ConfigurationSection storageSection = storage.getConfigurationSection(String.valueOf(page + 1));
                            if (storageSection != null) {
                                MenuManager.openInventory(player, new StorageMenu(player, spawnerConfig, page + 1));
                            }
                            else Config.sendMessage(player, "noPages");
                        }
                        case "BACK_PAGE" -> {
                            if (page > 1) MenuManager.openInventory(player,
                                        new StorageMenu(player, spawnerConfig, page - 1));
                            else Config.sendMessage(player, "noPages");
                        }
                        case "COLLECT_ALL" -> {
                            Map<LootItem, Integer> map = new HashMap<>();
                            storage.getKeys(false).forEach(key -> {
                                ConfigurationSection pageSection = storage.getConfigurationSection(key);
                                if (pageSection != null && !pageSection.getKeys(false).isEmpty()) {
                                    int page = Utils.toInt(key);
                                    for (String pageSectionKey : pageSection.getKeys(false)) {
                                        String[] split =
                                                Objects.requireNonNull(pageSection.getString(pageSectionKey)).split(";");
                                        int amount = split.length >= 2 ? Utils.toInt(split[1]) : 1;
                                        Material material = Material.getMaterial(split[0]);

                                        if (material == null) continue;
                                        LootItem lootItem = new LootItem(material, amount);
                                        lootItem.setSlot((byte) Utils.toInt(pageSectionKey));

                                        map.put(lootItem, page);
                                    }
                                }
                            });

                            if (map.isEmpty()) {
                                Config.sendMessage(player, "noItems");
                                return;
                            }

                            map.forEach((item, page) -> {
                                item.collect(player);
                                this.getSpawnerConfig().set(String.format("storage.%s.%s", page, item.getSlot()), null);
                            });
                            getSpawnerConfig().save();
                            getSpawnerConfig().updateHologram();
                            Config.sendMessage(player, "collectAllItems");
                            player.closeInventory();
                        }
                        case "CLOSE" -> player.closeInventory();
                        case "BACK" -> MenuManager.openInventory(player, new MainMenu(player, spawnerConfig));
                    }
                }
            };
            this.getButtons().add(button);
        }
        this.updateLoot();
    }

    @Override
    public void onOpen(InventoryOpenEvent e) {
        this.getButtons().forEach(button -> button.insert(this));
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        e.setCancelled(true);
        for (Button button : this.getButtons()) {
            if (button.checkId(item)) {
                button.click(this.getPlayer());
                return;
            }
        }

        for (LootItem lootItem : new ArrayList<>(this.loot)) {
            if (lootItem.checkId(item)) {
                lootItem.collect(this.getPlayer());
                this.removeLootItem(lootItem);
                this.getSpawnerConfig().save();
                this.getSpawnerConfig().updateHologram();
                return;
            }
        }
    }

    private void removeLootItem(LootItem lootItem) {
        this.getSpawnerConfig().set(String.format("storage.%s.%s", this.page, lootItem.getSlot()), null);
        this.loot.remove(lootItem);
        lootItem.remove(this);
    }

    public void updateLoot() {
        ConfigurationSection storageSection = this.getSpawnerConfig()
                .getStorageSection().getConfigurationSection(String.valueOf(this.page));
        if (storageSection == null || storageSection.getKeys(false).isEmpty()) return;

        this.loot.forEach(lootItem -> lootItem.remove(this));
        this.loot.clear();
        int maxSlot = MenuConfig.getContainer_page().getInt("maxLootInPage");
        for (String key : storageSection.getKeys(false)) {
            byte slot = Byte.parseByte(key);
            if (slot > maxSlot) break;

            String value = storageSection.getString(key);
            if (value == null || value.isEmpty()) continue;

            String[] split = value.split(";");
            int amount = split.length >= 2 ? Integer.parseInt(split[1]) : 1;
            amount = Math.min(Math.max(amount, 1), 64);

            Material material = Material.getMaterial(split[0]);
            if (material == null) continue;

            LootItem lootItem = new LootItem(material, amount);
            lootItem.insert(this, slot);
            this.loot.add(lootItem);
        }
    }

    @Override
    public void onClose(InventoryCloseEvent e) {
    }
}
