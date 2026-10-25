package net.kingidk.creativeItemControl.Util;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ExcludedItemStore {
   private final File file;
   private final YamlConfiguration yaml;

   public ExcludedItemStore(JavaPlugin plugin) {
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

      return result;
   }

   public void save(String id, ItemStack item) {
      this.yaml.set(id, item);
      this.flush();
   }

   public void remove(String id) {
      this.yaml.set(id, null);
      this.flush();
   }

   private void flush() {
      try {
         this.yaml.save(this.file);
      } catch (IOException e) {
         throw new RuntimeException("Failed to save cic_items.yml", e);
      }
   }
}
