package net.kingidk.creativeItemControl.Handlers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.kingidk.creativeItemControl.CreativeItemControl;
import net.kingidk.creativeItemControl.Util.ItemCheckContext;
import net.kingidk.creativeItemControl.Util.MessageUtil;
import org.bukkit.enchantments.Enchantment;

public class EnchantmentHandler implements ItemCheck {
   private final CreativeItemControl plugin;
   private final MessageUtil messageUtil;

   public EnchantmentHandler(CreativeItemControl plugin, MessageUtil messageUtil) {
      this.plugin = plugin;
      this.messageUtil = messageUtil;
   }

   @Override
   public void check(ItemCheckContext ctx) {
      if (!ctx.isCancelled()) {
         if (this.plugin.config.enchantmentsEnabled) {
            if (!ctx.player.hasPermission("cic.bypass.enchantments")) {
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

                     if (this.incompatibleItem(ctx, enchantment)) {
                        found = true;
                     }

                     if (this.incompatibleEnchantment(ctx, enchantment, seen)) {
                        found = true;
                     }

                     seen.add(enchantment);
                  }

                  if (found && this.plugin.config.playerAlerts) {
                     this.messageUtil.sendAlert(ctx.player, "alerts.enchantments");
                  }
               }
            }
         }
      }
   }

   public boolean incompatibleItem(ItemCheckContext ctx, Enchantment enchantment) {
      if (!enchantment.canEnchantItem(ctx.item)) {
         switch (this.plugin.config.enchantmentsAction) {
            case LOWER:
            case REMOVE:
               ctx.meta.removeEnchant(enchantment);
               break;
            case DELETE:
               ctx.cancel();
            case null:
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean impossibleLevel(ItemCheckContext ctx, Enchantment enchantment, int level) {
      if (level > enchantment.getMaxLevel()) {
         switch (this.plugin.config.enchantmentsAction) {
            case LOWER:
               ctx.meta.addEnchant(enchantment, enchantment.getMaxLevel(), true);
               break;
            case REMOVE:
               ctx.meta.removeEnchant(enchantment);
               break;
            case DELETE:
               ctx.cancel();
            case null:
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean incompatibleEnchantment(ItemCheckContext ctx, Enchantment enchantment, Set<Enchantment> seen) {
      if (this.plugin.config.enchantmentsAllowIncompatible) {
         return false;
      }

      for (Enchantment e : seen) {
         if (enchantment.conflictsWith(e)) {
            switch (this.plugin.config.enchantmentsAction) {
               case LOWER:
               case REMOVE:
                  ctx.meta.removeEnchant(enchantment);
                  break;
               case DELETE:
                  ctx.cancel();
               case null:
            }

            return true;
         }
      }

      return false;
   }
}
