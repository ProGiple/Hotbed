package org.satellite.dev.progiple.hotbed.spawners.menus.buttons.realized;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.novasparkle.lunaspring.API.menus.items.Item;
import org.novasparkle.lunaspring.API.util.service.managers.ColorManager;
import org.satellite.dev.progiple.hotbed.configs.SpawnerConfig;
import org.satellite.dev.progiple.hotbed.spawners.menus.buttons.Button;

import java.util.ArrayList;
import java.util.List;

public class SwitchHologramButton extends Button {
    private final String[] statuses;
    public SwitchHologramButton(ConfigurationSection section, SpawnerConfig spawnerConfig) {
        super(section, spawnerConfig);

        ConfigurationSection statusSection = section.getConfigurationSection("statuses");
        if (statusSection == null) {
            this.statuses = new String[]{null, null};
        }
        else {
            this.statuses = new String[]{
                    ColorManager.color(statusSection.getString("DISABLE")),
                    ColorManager.color(statusSection.getString("ENABLE"))
            };

            this.updateLore(spawnerConfig.getHologramStatus());
        }
    }

    private void updateLore(boolean status) {
        List<String> lore = new ArrayList<>(this.getDefaultLore());

        String statusStr = statuses[status ? 1 : 0];
        lore.replaceAll(l -> l.replace("%status%", statusStr));

        this.setLore(lore);
    }

    @Override
    public Item onClick(InventoryClickEvent e) {
        SpawnerConfig spawnerConfig = this.getSpawnerConfig();

        boolean newStatus = !spawnerConfig.getHologramStatus();
        spawnerConfig.set("holo", newStatus);
        spawnerConfig.save();

        if (newStatus) {
            spawnerConfig.updateHologram();
        }
        else {
            spawnerConfig.getHologram().delete();
            spawnerConfig.setHologram(null);
        }

        this.updateLore(newStatus);
        this.insert();
        return this;
    }
}
