package net.kingidk.creativeItemControl.managers;

import io.papermc.paper.datacomponent.DataComponentType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kingidk.creativeItemControl.CreativeItemControl;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public class ConfigManager {
   public final Map<ConfigManager.ConfigKeys, Object> cachedConfig = new HashMap<>();
   public List<World> worlds = new ArrayList<>();
   public List<DataComponentType> resolvedComponents = new ArrayList<>();
   private final Map<Material, ItemStack> defaultItemCache = new EnumMap<>(Material.class);
   private final CreativeItemControl plugin;

   public ConfigManager(CreativeItemControl plugin) {
      this.plugin = plugin;
   }

   public void load() {
      this.cachedConfig.clear();
      this.worlds.clear();

      for (ConfigManager.ConfigKeys key : ConfigManager.ConfigKeys.values()) {
         this.cachedConfig.put(key, this.plugin.getConfig().get(key.path, key.defaultValue));
      }

      List<String> worldStrings = this.plugin.getConfig().getStringList("config.worlds");
      if (!worldStrings.isEmpty()) {
         for (String worldName : worldStrings) {
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
               this.worlds.add(world);
            }
         }
      }

      String attributesAction = (String)this.cachedConfig.get(ConfigManager.ConfigKeys.ATTRIBUTES_ACTION);
      if (!attributesAction.equalsIgnoreCase("remove") && !attributesAction.equalsIgnoreCase("delete")) {
         this.cachedConfig.replace(ConfigManager.ConfigKeys.ATTRIBUTES_ACTION, ConfigManager.ConfigKeys.ATTRIBUTES_ACTION.defaultValue);
      }

      String enchantmentsAction = (String)this.cachedConfig.get(ConfigManager.ConfigKeys.ENCHANT_ACTION);
      if (!enchantmentsAction.equalsIgnoreCase("lower") && !enchantmentsAction.equalsIgnoreCase("remove") && !enchantmentsAction.equalsIgnoreCase("delete")) {
         this.cachedConfig.replace(ConfigManager.ConfigKeys.ENCHANT_ACTION, ConfigManager.ConfigKeys.ENCHANT_ACTION.defaultValue);
      }

      this.resolvedComponents.clear();

      for (String name : (List)this.cachedConfig.get(ConfigManager.ConfigKeys.COMPONENTS_BLOCKED)) {
         NamespacedKey key = NamespacedKey.fromString(name);
         if (key != null) {
            DataComponentType type = (DataComponentType)Registry.DATA_COMPONENT_TYPE.get(key);
            if (type != null) {
               this.resolvedComponents.add(type);
            }
         }
      }
   }

   public boolean getBoolean(ConfigManager.ConfigKeys key) {
      Object value = this.cachedConfig.get(key);
      return value instanceof Boolean ? (Boolean)value : (Boolean)key.defaultValue;
   }

   public int getInt(ConfigManager.ConfigKeys key) {
      Object value = this.cachedConfig.get(key);
      return value instanceof Integer ? (Integer)value : (Integer)key.defaultValue;
   }

   public String getString(ConfigManager.ConfigKeys key) {
      Object value = this.cachedConfig.get(key);
      return value instanceof String ? (String)value : (String)key.defaultValue;
   }

   public List<String> getStringList(ConfigManager.ConfigKeys key) {
      Object value = this.cachedConfig.get(key);
      return value instanceof List ? (List)value : (List)key.defaultValue;
   }

   public boolean isWorldDenied(World world) {
      return (Boolean)this.cachedConfig.get(ConfigManager.ConfigKeys.MAIN_BLACKLIST) == this.worlds.contains(world);
   }

   public List<DataComponentType> getResolvedComponents() {
      return this.resolvedComponents;
   }

   public ItemStack getDefaultItem(Material type) {
      return this.defaultItemCache.computeIfAbsent(type, t -> new ItemStack(t, 1));
   }

   public void migrate() {
      boolean updated = false;

      for (ConfigManager.ConfigKeys key : ConfigManager.ConfigKeys.values()) {
         if (!this.plugin.getConfig().isSet(key.path)) {
            this.plugin.getConfig().set(key.path, key.defaultValue);
            updated = true;
         }
      }

      if (updated) {
         this.plugin.saveConfig();
      }
   }

   public enum ConfigKeys {
      MAIN_ENABLED("config.enabled", true),
      MAIN_PLAYER_ALERTS("config.player-alerts", true),
      MAIN_ALERT_COOLDOWN("config.alert-cooldown", 1000),
      MAIN_BLACKLIST("config.blacklist", false),
      MAIN_GIVE_COOLDOWN("config.give-cooldown", 0),
      MAIN_DISPENSERS_ENABLED("config.check-dispensers", true),
      ENCHANT_ENABLED("enchantments.enabled", true),
      ENCHANT_ACTION("enchantments.action", "LOWER"),
      ENCHANT_ALLOW_INCOMPATIBLE("enchantments.allow-incompatible", false),
      ATTRIBUTES_ENABLED("attributes.enabled", true),
      ATTRIBUTES_ACTION("attributes.action", "REMOVE"),
      POTIONS_ENABLED("potions.enabled", true),
      COMPONENTS_ENABLED("components.enabled", true),
      COMPONENTS_BLOCKED("components.blocked", new ArrayList()),
      ALERTS_COMPONENTS("messages.alerts.components", "<b><red>Items with custom components are not allowed here!"),
      ALERTS_ATTRIBUTES("messages.alerts.attributes", "<b><red>Items with attribute modifiers are not allowed here!"),
      ALERTS_ENCHANTMENTS("messages.alerts.enchantments", "<b><red>Items with impossible enchantments are not allowed here!"),
      ALERTS_POTIONS("messages.alerts.potions", "<b><red>Custom potions are not allowed here!"),
      CMD_EXCLUDE("messages.commands.exclude", "&aStored {type} as \"{id}\"."),
      CMD_REMOVE("messages.commands.remove", "&aRemoved excluded item \"{id}\"."),
      CMD_LIST_EMPTY("messages.commands.listempty", "&eNo excluded items stored."),
      CMD_GIVE("messages.commands.give", "&aGave \"{id}\" to &f&a{player}."),
      CMD_GIVE_INVALID("messages.commands.giveinvalid", "&cNo excluded item found with id \"{id}\"."),
      CMD_GIVE_OTHERS_FAIL("messages.commands.giveothersfail", "&cYou do not have permission to give to other players!"),
      CMD_PLAYER_NOT_FOUND("messages.commands.playernotfound", "&cPlayer \"{player}\" not found."),
      CMD_SPECIFY_PLAYER("messages.commands.specifyplayer", "&cSpecify a player: /cic give <id> <player>"),
      CMD_INVALID_WORLD("messages.commands.invalidworld", "&cYou cannot use this in this world!"),
      CMD_ON_COOLDOWN("messages.commands.oncooldown", "&cYou must wait {time}s before receiving this item again."),
      CMD_NO_PERM("messages.commands.noperm", "&cYou do not have permission!");

      public final String path;
      public final Object defaultValue;

      ConfigKeys(String path, Object defaultValue) {
         this.path = path;
         this.defaultValue = defaultValue;
      }
   }
}
