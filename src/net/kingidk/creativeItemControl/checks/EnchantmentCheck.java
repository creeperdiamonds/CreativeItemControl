package net.kingidk.creativeItemControl.checks;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.kingidk.creativeItemControl.CreativeItemControl;
import net.kingidk.creativeItemControl.managers.ConfigManager;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class EnchantmentCheck implements ItemCheck {
   private final CreativeItemControl plugin;

   public EnchantmentCheck(CreativeItemControl plugin) {
      this.plugin = plugin;
   }

   @Override
   public void check(ItemCheckContext ctx) {
      if (!ctx.isCancelled()) {
         if (this.plugin.getConfigManager().getBoolean(ConfigManager.ConfigKeys.ENCHANT_ENABLED)) {
            boolean playerNotNull = ctx.player != null;
            if (!playerNotNull || !ctx.player.hasPermission("cic.bypass.enchantments")) {
               Map<Enchantment, Integer> enchants = ctx.meta.getEnchants();
               if (!enchants.isEmpty()) {
                  Set<Enchantment> seen = new HashSet<>();
                  boolean found = false;

                  for (Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                     Enchantment enchantment = entry.getKey();
                     int level = entry.getValue();
                     if (this.impossibleLevel(ctx, enchantment, level)) {
                        found = true;
                     }

                     if (this.incompatibleEnchantment(ctx, enchantment, seen)) {
                        found = true;
                     }

                     if (this.incompatibleItem(ctx, enchantment)) {
                        found = true;
                     }

                     seen.add(enchantment);
                  }

                  if (!ctx.isCancelled() && !found && ctx.meta instanceof BundleMeta bm) {
                     for (ItemStack item : bm.getItems()) {
                        ItemMeta meta = item.getItemMeta();
                        Map<Enchantment, Integer> bundleEnchants = meta.getEnchants();
                        if (!bundleEnchants.isEmpty()) {
                           Set<Enchantment> bundleSeen = new HashSet<>();
                           boolean bundleFound = false;

                           for (Entry<Enchantment, Integer> entry : bundleEnchants.entrySet()) {
                              Enchantment enchantment = entry.getKey();
                              int level = entry.getValue();
                              if (this.impossibleLevel(ctx, enchantment, level)) {
                                 bundleFound = true;
                              }

                              if (this.incompatibleEnchantment(ctx, enchantment, bundleSeen)) {
                                 bundleFound = true;
                              }

                              if (this.incompatibleItem(ctx, enchantment)) {
                                 bundleFound = true;
                              }

                              bundleSeen.add(enchantment);
                           }

                           if (bundleFound) {
                              ctx.cancel();
                              found = true;
                              break;
                           }
                        }
                     }
                  }

                  if (playerNotNull && found && this.plugin.getConfigManager().getBoolean(ConfigManager.ConfigKeys.MAIN_PLAYER_ALERTS)) {
                     this.plugin.getMessageManager().sendAlert(ctx.player, ConfigManager.ConfigKeys.ALERTS_ENCHANTMENTS);
                  }
               }
            }
         }
      }
   }

   private boolean impossibleLevel(ItemCheckContext ctx, Enchantment enchantment, int level) {
      if (level > enchantment.getMaxLevel()) {
         switch (this.plugin.getConfigManager().getString(ConfigManager.ConfigKeys.ENCHANT_ACTION)) {
            case "LOWER":
               ctx.meta.addEnchant(enchantment, enchantment.getMaxLevel(), true);
               break;
            case "REMOVE":
               ctx.meta.removeEnchant(enchantment);
               break;
            case "DELETE":
               ctx.cancel();
            case null:
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean incompatibleEnchantment(ItemCheckContext ctx, Enchantment enchantment, Set<Enchantment> seen) {
      if (this.plugin.getConfigManager().getBoolean(ConfigManager.ConfigKeys.ENCHANT_ALLOW_INCOMPATIBLE)) {
         return false;
      }

      for (Enchantment e : seen) {
         if (enchantment.conflictsWith(e)) {
            switch (this.plugin.getConfigManager().getString(ConfigManager.ConfigKeys.ENCHANT_ACTION)) {
               case "LOWER":
               case "REMOVE":
                  ctx.meta.removeEnchant(enchantment);
                  break;
               case "DELETE":
                  ctx.cancel();
               case null:
            }

            return true;
         }
      }

      return false;
   }

   private boolean incompatibleItem(ItemCheckContext ctx, Enchantment enchantment) {
      if (!enchantment.canEnchantItem(ctx.item)) {
         switch (this.plugin.getConfigManager().getString(ConfigManager.ConfigKeys.ENCHANT_ACTION)) {
            case "LOWER":
            case "REMOVE":
               ctx.meta.removeEnchant(enchantment);
               break;
            case "DELETE":
               ctx.cancel();
            case null:
         }

         return true;
      } else {
         return false;
      }
   }
}
