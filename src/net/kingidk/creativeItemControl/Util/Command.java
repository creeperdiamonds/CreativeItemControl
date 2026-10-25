package net.kingidk.creativeItemControl.Util;

import java.util.List;
import java.util.Map;
import net.kingidk.creativeItemControl.CreativeItemControl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Command implements CommandExecutor, TabCompleter {
   private final CreativeItemControl plugin;
   private final MessageUtil messageUtil;

   public Command(CreativeItemControl plugin, MessageUtil messageUtil) {
      this.plugin = plugin;
      this.messageUtil = messageUtil;
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
      boolean isAdmin = sender.hasPermission("cic.admin");
      if (!isAdmin && !sender.hasPermission("cic.give")) {
         this.messageUtil.send(sender, "commands.noperm");
         return true;
      }

      if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("give")) {
         ItemStack item = this.plugin.getExcludedItem(args[1]);
         if (item == null) {
            this.messageUtil.send(sender, "commands.giveinvalid", "{id}", args[1]);
            return true;
         }

         Player target;
         if (args.length == 3) {
            if (!isAdmin) {
               this.messageUtil.send(sender, "command.giveothers");
               return true;
            }

            target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
               this.messageUtil.send(sender, "commands.playernotfound", "{player}", args[2]);
               return true;
            }
         } else {
            if (!(sender instanceof Player p)) {
               this.messageUtil.send(sender, "command.specifyplayer");
               return true;
            }

            target = p;
         }

         boolean inList = this.plugin.config.worlds.contains(target.getWorld().getName());
         if (!isAdmin && this.plugin.config.worldsBlacklist == inList) {
            this.messageUtil.send(sender, "command.invalidworld");
            return true;
         } else if (!isAdmin && this.plugin.config.giveCooldownSeconds > 0L && this.plugin.isOnGiveCooldown(target.getUniqueId(), args[1])) {
            long remaining = this.plugin.getGiveCooldownRemaining(target.getUniqueId(), args[1]);
            this.messageUtil.send(sender, "commands.cooldown", "{time}", String.valueOf(remaining));
            return true;
         } else {
            target.getInventory().addItem(new ItemStack[]{item.clone()});
            this.plugin.recordGive(target.getUniqueId(), args[1]);
            this.messageUtil.send(sender, "commands.give", "{id}", args[1], "{player}", target.getName());
            return true;
         }
      } else if (!isAdmin) {
         if (args.length == 1 && args[0].equalsIgnoreCase("give")) {
            sender.sendMessage(Component.text("You must select an item! Available Items: " + this.plugin.getExcludedItems().keySet()));
            return true;
         } else {
            this.messageUtil.send(sender, "commands.noperm");
            return true;
         }
      } else {
         if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            this.plugin.reloadConfig();
            this.plugin.config = new ConfigUtil(this.plugin.getConfig());
            this.messageUtil.send(sender, "commands.reload");
            return true;
         }

         if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            Map<String, ItemStack> items = this.plugin.getExcludedItems();
            if (items.isEmpty()) {
               this.messageUtil.send(sender, "commands.listempty");
               return true;
            } else {
               sender.sendMessage(Component.text("Excluded items:", NamedTextColor.GOLD));
               items.forEach((id, item) -> sender.sendMessage(Component.text("  " + id + " - " + item.getType(), NamedTextColor.WHITE)));
               return true;
            }
         } else if (args.length == 2 && args[0].equalsIgnoreCase("exclude")) {
            if (sender instanceof Player p) {
               ItemStack held = p.getInventory().getItemInMainHand();
               if (held.getType().isAir()) {
                  sender.sendMessage(Component.text("You must be holding an item!", NamedTextColor.RED));
                  return true;
               } else {
                  this.plugin.storeExcludedItem(args[1], held);
                  this.messageUtil.send(sender, "commands.exclude", "{type}", String.valueOf(held.getType()), "{id}", args[1]);
                  return true;
               }
            } else {
               sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
               return true;
            }
         } else if (args.length != 2 || !args[0].equalsIgnoreCase("remove")) {
            return true;
         } else if (this.plugin.getExcludedItem(args[1]) == null) {
            sender.sendMessage(Component.text("No excluded item found with id \"" + args[1] + "\".", NamedTextColor.RED));
            this.messageUtil.send(sender, "commands.giveinvalid", "{id}", args[1]);
            return true;
         } else {
            this.plugin.removeExcludedItem(args[1]);
            this.messageUtil.send(sender, "commands.remove", "{id}", args[1]);
            return true;
         }
      }
   }

   @Nullable
   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
      if (!sender.hasPermission("cic.admin") && !sender.hasPermission("cic.give")) {
         return List.of();
      }

      if (!sender.hasPermission("cic.admin")) {
         if (args.length == 1) {
            return List.of("give");
         } else {
            return args.length == 2 && args[0].equalsIgnoreCase("give") ? List.copyOf(this.plugin.getExcludedItems().keySet()) : List.of();
         }
      } else if (args.length == 1) {
         return List.of("reload", "list", "exclude", "remove", "give");
      } else if (args.length != 2 || !args[0].equalsIgnoreCase("remove") && !args[0].equalsIgnoreCase("give")) {
         return args.length == 3 && args[0].equalsIgnoreCase("give") ? Bukkit.getOnlinePlayers().stream().<String>map(Player::getName).toList() : List.of();
      } else {
         return List.copyOf(this.plugin.getExcludedItems().keySet());
      }
   }
}
