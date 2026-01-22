package org.satellite.dev.progiple.hotbed.spawners.menus.buttons.realized;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.novasparkle.lunaspring.API.menus.items.Item;
import org.novasparkle.lunaspring.API.util.utilities.LunaMath;
import org.satellite.dev.progiple.hotbed.configs.Config;
import org.satellite.dev.progiple.hotbed.configs.SpawnerConfig;
import org.satellite.dev.progiple.hotbed.spawners.menus.LootItem;
import org.satellite.dev.progiple.hotbed.spawners.menus.buttons.Button;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CollectAllButton extends Button {
    private final ConfigurationSection storage;
    public CollectAllButton(ConfigurationSection section,
                            SpawnerConfig spawnerConfig,
                            ConfigurationSection storage) {
        super(section, spawnerConfig);
        this.storage = storage;
    }

    @Override
    public Item onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

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
}
