package net.kingidk.creativeItemControl.Handlers;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentType.Valued;
import java.util.Objects;
import net.kingidk.creativeItemControl.CreativeItemControl;
import net.kingidk.creativeItemControl.Util.ItemCheckContext;
import net.kingidk.creativeItemControl.Util.MessageUtil;
import org.bukkit.inventory.ItemStack;

public class ComponentHandler implements ItemCheck {
   private final CreativeItemControl plugin;
   private final MessageUtil messageUtil;

   public ComponentHandler(CreativeItemControl plugin, MessageUtil messageUtil) {
      this.plugin = plugin;
      this.messageUtil = messageUtil;
   }

   @Override
   public void check(ItemCheckContext ctx) {
      if (!ctx.isCancelled()) {
         if (this.plugin.config.componentsEnabled) {
            if (!ctx.player.hasPermission("cic.bypass.components")) {
               for (DataComponentType type : this.plugin.config.resolvedComponents) {
                  ItemStack defaultItem = this.plugin.getDefaultItem(ctx.item.getType());
                  if (ctx.item.hasData(type)
                     && !(type instanceof Valued<?> valued ? Objects.equals(defaultItem.getData(valued), ctx.item.getData(valued)) : defaultItem.hasData(type))
                     )
                   {
                     ctx.cancel();
                  }
               }

               if (ctx.isCancelled() && this.plugin.config.playerAlerts) {
                  this.messageUtil.sendAlert(ctx.player, "alerts.components");
               }
            }
         }
      }
   }
}
