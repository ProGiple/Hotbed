package org.satellite.dev.progiple.hotbed;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.novasparkle.lunaspring.API.menus.IMenu;
import org.novasparkle.lunaspring.API.menus.MenuManager;
import org.novasparkle.lunaspring.API.menus.items.Item;
import org.novasparkle.lunaspring.API.util.service.managers.ColorManager;
import org.novasparkle.lunaspring.API.util.service.managers.NBTManager;
import org.satellite.dev.progiple.hotbed.configs.Config;
import org.satellite.dev.progiple.hotbed.configs.MobsConfig;
import org.satellite.dev.progiple.hotbed.configs.SpawnerConfig;
import org.satellite.dev.progiple.hotbed.spawners.menus.realized.StorageMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class Tools {
    public double getPlayerChance(Player player) {
        double value = 0.0;
        for (String perm : player.getEffectivePermissions().stream()
                .map(PermissionAttachmentInfo::getPermission)
                .filter(p -> p.contains("hotbed.chance.")).toList()) {
            String[] splited = perm.split("\\.");
            if (splited.length < 3) continue;

            double permValue = Double.parseDouble(splited[2] + (splited.length >= 4 ? "." + splited[3] : ""));
            if (value < permValue) value = permValue;
        }
        return value == 0.0 ? Config.getDouble("settings.default_chance") : value;
    }

    public String getStrLoc(Location location) {
        return String.format("%s;%s;%s;%s", location.getWorld().getName(),
                location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public String getMobName(String mob_type) {
        ConfigurationSection section = MobsConfig.getSection("mobs");
        if (section.getKeys(false).contains(mob_type)) {
            return section.getString(String.format("%s.displayName", mob_type));
        }
        return mob_type;
    }

    public int getStorageSize(SpawnerConfig spawnerConfig) {
        ConfigurationSection section = spawnerConfig.getStorageSection();
        return section == null ? 0 : section.getKeys(false).stream()
                .map(key -> spawnerConfig.getStorageSection().getConfigurationSection(key))
                .filter(Objects::nonNull)
                .flatMap(pageSection -> pageSection.getKeys(false).stream()
                        .map(pageSection::getString)
                        .filter(value -> value != null && !value.isEmpty())
                        .map(value -> value.split(";"))
                        .map(split -> (split.length >= 2) ? Integer.parseInt(split[1]) : 1))
                .reduce(0, Integer::sum);
    }

    public List<String> reLoreItem(Item item, SpawnerConfig spawnerConfig) {
        int level = spawnerConfig.getInt("level");
        ConfigurationSection mobSection = MobsConfig.getSection(String.format("mobs.%s", spawnerConfig.getString("mob")));
        String owner = spawnerConfig.getString("owner");
        int storagedExp = spawnerConfig.getInt("exp");
        int storageSize = Tools.getStorageSize(spawnerConfig);

        String mobName = Tools.getMobName(mobSection.getName());
        int expLimit = Config.getInt("settings.maxExpLimitPerLevel") * level;
        int lootLimit = Config.getInt("settings.maxLootItemsLimitPerLevel") * level;

        List<String> lore = new ArrayList<>(item.getLore());
        lore.replaceAll(line -> line
                .replace("%owner%", owner)
                .replace("%level%", String.valueOf(level))
                .replace("%exp%", String.valueOf(storagedExp))
                .replace("%items%", String.valueOf(storageSize))
                .replace("%limit_exp%", String.valueOf(expLimit))
                .replace("%limit_items%", String.valueOf(lootLimit))
                .replace("%mob_type%", mobName));
        return lore;
    }

    public void updateStorageMenus() {
        SpawnerConfig.getSpawnerCfgs().values().forEach(Tools::updateStorageMenu);
    }

    public void updateStorageMenu(SpawnerConfig spawnerConfig) {
        for (IMenu value : MenuManager.getActiveInventories().values()) {
            if (value instanceof StorageMenu menu) {
                if (!menu.getSpawnerConfig().equals(spawnerConfig)) continue;

                menu.updateLoot();
                break;
            }
        }
    }

    @SuppressWarnings("deprecation")
    public ItemStack getVirtualSpawner(EntityType entityType, String nick, int spawnerLevel) {
        ItemStack item = new ItemStack(Material.SPAWNER, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Config.getString("settings.spawner_name"));

        List<String> lore = new ArrayList<>(Config.getStrList("settings.spawner_lore"));
        lore.replaceAll(line -> ColorManager.color(line
                .replace("%mob_type%", Tools.getMobName(entityType.name()))
                .replace("%spawner_level%", String.valueOf(spawnerLevel))
                .replace("%player%", nick)));
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_ITEM_SPECIFICS, ItemFlag.HIDE_ARMOR_TRIM);
        item.setItemMeta(meta);

        NBTManager.setString(item, "mob", entityType.name());
        NBTManager.setInt(item, "level", spawnerLevel);
        return item;
    }

    public boolean isNotOwner(Player player, SpawnerConfig spawnerConfig) {
        return !spawnerConfig.getString("owner").equals(player.getName());
    }
}
