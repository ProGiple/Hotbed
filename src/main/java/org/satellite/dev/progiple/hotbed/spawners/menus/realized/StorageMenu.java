package org.satellite.dev.progiple.hotbed.spawners.menus.realized;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.novasparkle.lunaspring.API.menus.MenuManager;
import org.novasparkle.lunaspring.API.menus.items.Item;
import org.novasparkle.lunaspring.API.util.utilities.LunaMath;
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
            Button button = null;

            switch (key) {
                case "NEXT_PAGE" -> button = new Button(itemSection, this.getSpawnerConfig()) {
                    @Override
                    public Item onClick(InventoryClickEvent e) {
                        ConfigurationSection storageSection = storage.getConfigurationSection(String.valueOf(page + 1));
                        if (storageSection != null) {
                            MenuManager.openInventory(player, new StorageMenu(player, spawnerConfig, page + 1));
                        }
                        else Config.sendMessage(player, "noPages");
                        return this;
                    }
                };
                case "BACK_PAGE" -> button = new Button(itemSection, this.getSpawnerConfig()) {
                    @Override
                    public Item onClick(InventoryClickEvent e) {
                        if (page > 1) MenuManager.openInventory(player,
                                new StorageMenu(player, spawnerConfig, page - 1));
                        else Config.sendMessage(player, "noPages");
                        return this;
                    }
                };
                case "COLLECT_ALL" -> button = new Button(itemSection, this.getSpawnerConfig()) {
                    @Override
                    public Item onClick(InventoryClickEvent e) {
                        Map<LootItem, Integer> map = new HashMap<>();
                        storage.getKeys(false).forEach(key -> {
                            ConfigurationSection pageSection = storage.getConfigurationSection(key);
                            if (pageSection != null && !pageSection.getKeys(false).isEmpty()) {
                                int page = LunaMath.toInt(key);
                                for (String pageSectionKey : pageSection.getKeys(false)) {
                                    String[] split =
                                            Objects.requireNonNull(pageSection.getString(pageSectionKey)).split(";");
                                    int amount = split.length >= 2 ? LunaMath.toInt(split[1]) : 1;
                                    Material material = Material.getMaterial(split[0]);

                                    if (material == null) continue;
                                    LootItem lootItem = new LootItem(material, amount);
                                    lootItem.setSlot((byte) LunaMath.toInt(pageSectionKey));

                                    map.put(lootItem, page);
                                }
                            }
                        });

                        if (map.isEmpty()) {
                            Config.sendMessage(player, "noItems");
                            return this;
                        }

                        map.forEach((item, page) -> {
                            item.collect(player);
                            this.getSpawnerConfig().set(String.format("storage.%s.%s", page, item.getSlot()), null);
                        });
                        getSpawnerConfig().save();
                        getSpawnerConfig().updateHologram();
                        Config.sendMessage(player, "collectAllItems");
                        player.closeInventory();
                        return this;
                    }
                };
                case "CLOSE" -> button = new Button(itemSection, this.getSpawnerConfig()) {
                    @Override
                    public Item onClick(InventoryClickEvent e) {
                        player.closeInventory();
                        return this;
                    }
                };
                case "BACK" -> button = new Button(itemSection, this.getSpawnerConfig()) {
                    @Override
                    public Item onClick(InventoryClickEvent e) {
                        MenuManager.openInventory(player, new MainMenu(player, spawnerConfig));
                        return this;
                    }
                };
            }
            if (button != null) this.getButtons().add(button);
        }
        this.updateLoot();
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

        for (LootItem lootItem : new ArrayList<>(this.loot)) {
            if (lootItem.getSlot() == e.getSlot() && lootItem.getItemStack().equals(item)) {
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

            Material material = Material.getMaterial(split[0].toUpperCase());
            if (material == null) continue;

            LootItem lootItem = new LootItem(material, amount);
            lootItem.insert(this, slot);
            this.loot.add(lootItem);
        }
    }

    @Override
    public void onClose(InventoryCloseEvent e) {
    }

    @Override
    public void onDrag(InventoryDragEvent e) {
        e.setCancelled(true);
    }
}
