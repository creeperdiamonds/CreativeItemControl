package net.kingidk.creativeItemControl.Util;

import io.papermc.paper.datacomponent.DataComponentType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.kingidk.creativeItemControl.Enums.AttributeAction;
import net.kingidk.creativeItemControl.Enums.EnchantAction;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.file.FileConfiguration;

public final class ConfigUtil {
   public final boolean masterEnabled;
   public final boolean attributesEnabled;
   public final boolean enchantmentsEnabled;
   public final boolean potionsEnabled;
   public final boolean componentsEnabled;
   public final boolean enchantmentsAllowIncompatible;
   public final AttributeAction attributesAction;
   public final EnchantAction enchantmentsAction;
   public final Set<String> worlds;
   public final boolean worldsBlacklist;
   public final boolean playerAlerts;
   public final long alertCooldown;
   public final List<String> components;
   public final long giveCooldownSeconds;
   public List<DataComponentType> resolvedComponents;

   public ConfigUtil(FileConfiguration config) {
      this.masterEnabled = config.getBoolean("config.enabled");
      this.attributesEnabled = config.getBoolean("attributes.enabled");
      this.enchantmentsEnabled = config.getBoolean("enchantments.enabled");
      this.potionsEnabled = config.getBoolean("potions.enabled");
      this.componentsEnabled = config.getBoolean("components.enabled");
      this.enchantmentsAllowIncompatible = config.getBoolean("enchantments.allow-incompatible");
      this.attributesAction = AttributeAction.valueOf(config.getString("attributes.action", "REMOVE"));
      this.enchantmentsAction = EnchantAction.valueOf(config.getString("enchantments.action", "LOWER"));
      this.worlds = new HashSet<>(config.getStringList("config.worlds"));
      this.worldsBlacklist = config.getBoolean("config.blacklist");
      this.playerAlerts = config.getBoolean("config.playeralerts");
      this.alertCooldown = config.getLong("config.alert-cooldown", 1000L);
      this.components = config.getStringList("components.blocked");
      this.giveCooldownSeconds = config.getLong("config.give-cooldown", 0L);
      this.resolvedComponents = new ArrayList<>();

      for (String name : this.components) {
         NamespacedKey key = NamespacedKey.fromString(name);
         if (key != null) {
            DataComponentType type = (DataComponentType)Registry.DATA_COMPONENT_TYPE.get(key);
            if (type != null) {
               this.resolvedComponents.add(type);
            }
         }
      }
   }
}
