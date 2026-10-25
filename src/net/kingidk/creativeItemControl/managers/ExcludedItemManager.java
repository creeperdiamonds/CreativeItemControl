package net.kingidk.creativeItemControl.managers;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.kingidk.creativeItemControl.CreativeItemControl;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

public class ExcludedItemManager {
   private final File file;
   private final YamlConfiguration yaml;
   private final CreativeItemControl plugin;
   private Map<String, ItemStack> excludedItems;
   private Map<String, Long> giveCooldowns = new HashMap<>();

   public ExcludedItemManager(CreativeItemControl plugin) {
      this.plugin = plugin;
      plugin.getDataFolder().mkdirs();
      this.file = new File(plugin.getDataFolder(), "cic_items.yml");
      this.yaml = YamlConfiguration.loadConfiguration(this.file);
   }

   public Map<String, ItemStack> loadAll() {
      Map<String, ItemStack> result = new LinkedHashMap<>();

      for (String key : this.yaml.getKeys(false)) {
         ItemStack item = this.yaml.getItemStack(key);
         if (item != null) {
            result.put(key, item);
         }
      }

      this.excludedItems = result;

      for (String id : this.excludedItems.keySet()) {
         this.registerGivePermission(id);
      }

      return result;
   }

   public String givePermissionNode(String id) {
      return "cic.give." + id.toLowerCase(Locale.ROOT);
   }

   public void registerGivePermission(String id) {
      PluginManager pm = Bukkit.getPluginManager();
      String node = this.givePermissionNode(id);
      if (pm.getPermission(node) == null) {
         pm.addPermission(new Permission(node, "Allows giving the excluded item '" + id + "'", PermissionDefault.OP, Map.of("cic.give", true)));
      }
   }

   public void unregisterGivePermission(String id) {
      Bukkit.getPluginManager().removePermission(this.givePermissionNode(id));
   }

   public void flush() {
      try {
         this.yaml.save(this.file);
      } catch (IOException e) {
         throw new RuntimeException("Failed to save cic_items.yml", e);
      }
   }

   public void storeExcludedItem(String id, ItemStack item) {
      this.excludedItems.put(id, item);
      this.yaml.set(id, item);
      this.flush();
      this.registerGivePermission(id);
   }

   public void removeExcludedItem(String id) {
      this.excludedItems.remove(id);
      this.yaml.set(id, null);
      this.flush();
      this.unregisterGivePermission(id);
   }

   public ItemStack getExcludedItem(String id) {
      return this.excludedItems.get(id);
   }

   public boolean excludedItemExists(String id) {
      return this.excludedItems.containsKey(id);
   }

   public Map<String, ItemStack> getExcludedItems() {
      return Collections.unmodifiableMap(this.excludedItems);
   }

   public boolean isExcluded(ItemStack item) {
      return this.excludedItems.values().stream().anyMatch(e -> e.isSimilar(item));
   }

   public boolean isOnGiveCooldown(UUID targetId, String itemId) {
      String key = targetId + ":" + itemId;
      Long last = this.giveCooldowns.get(key);
      return last == null ? false : System.currentTimeMillis() - last < this.plugin.getConfigManager().getInt(ConfigManager.ConfigKeys.MAIN_GIVE_COOLDOWN);
   }

   public long getGiveCooldownRemaining(UUID targetId, String itemId) {
      String key = targetId + ":" + itemId;
      Long last = this.giveCooldowns.get(key);
      return this.plugin.getConfigManager().getInt(ConfigManager.ConfigKeys.MAIN_GIVE_COOLDOWN) - (System.currentTimeMillis() - last) / 1000L;
   }

   public void recordGive(UUID targetId, String itemId) {
      this.giveCooldowns.put(targetId + ":" + itemId, System.currentTimeMillis());
   }

   public void give(Player player, String itemId) {
      ItemStack item = this.getExcludedItem(itemId);
      if (item != null) {
         player.give(new ItemStack[]{item});
         this.recordGive(player.getUniqueId(), itemId);
      }
   }

   public boolean canFit(Inventory inventory, ItemStack item) {
      int remaining = item.getAmount();
      int maxStack = item.getMaxStackSize();

      for (ItemStack slot : inventory.getStorageContents()) {
         if (slot == null || slot.getType().isAir()) {
            remaining -= maxStack;
         } else if (slot.isSimilar(item)) {
            int space = maxStack - slot.getAmount();
            if (space > 0) {
               remaining -= space;
            }
         }

         if (remaining <= 0) {
            return true;
         }
      }

      return remaining <= 0;
   }
}
