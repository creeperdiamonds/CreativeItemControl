package net.kingidk.creativeItemControl.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kingidk.creativeItemControl.CreativeItemControl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageManager {
   private final CreativeItemControl plugin;
   private Map<UUID, Long> alertCooldowns = new HashMap<>();

   public MessageManager(CreativeItemControl plugin) {
      this.plugin = plugin;
   }

   public void sendAlert(Player player, ConfigManager.ConfigKeys key) {
      if (player != null) {
         long now = System.currentTimeMillis();
         UUID uuid = player.getUniqueId();
         if (now - this.alertCooldowns.getOrDefault(uuid, 0L) >= this.plugin.getConfigManager().getInt(ConfigManager.ConfigKeys.MAIN_ALERT_COOLDOWN)) {
            this.alertCooldowns.put(uuid, now);
            this.send(player, key, "{player}", player.getName());
         }
      }
   }

   public void send(CommandSender sender, ConfigManager.ConfigKeys key, String... replacements) {
      if (sender != null) {
         String message = this.plugin.getConfigManager().getString(key);
         if (message != null) {
            for (int i = 0; i + 1 < replacements.length; i += 2) {
               message = message.replace(replacements[i], replacements[i + 1]);
            }

            Component component = MiniMessage.miniMessage().deserialize(convertLegacyToMiniMessage(message));
            sender.sendMessage(component);
         }
      }
   }

   public void send(CommandSender sender, String message, String... replacements) {
      if (sender != null) {
         if (message != null) {
            for (int i = 0; i + 1 < replacements.length; i += 2) {
               message = message.replace(replacements[i], replacements[i + 1]);
            }

            Component component = MiniMessage.miniMessage().deserialize(convertLegacyToMiniMessage(message));
            sender.sendMessage(component);
         }
      }
   }

   private static String convertLegacyToMiniMessage(String input) {
      return input.replace("&0", "<black>")
         .replace("&1", "<dark_blue>")
         .replace("&2", "<dark_green>")
         .replace("&3", "<dark_aqua>")
         .replace("&4", "<dark_red>")
         .replace("&5", "<dark_purple>")
         .replace("&6", "<gold>")
         .replace("&7", "<gray>")
         .replace("&8", "<dark_gray>")
         .replace("&9", "<blue>")
         .replace("&a", "<green>")
         .replace("&b", "<aqua>")
         .replace("&c", "<red>")
         .replace("&d", "<light_purple>")
         .replace("&e", "<yellow>")
         .replace("&f", "<white>")
         .replace("&l", "<bold>")
         .replace("&o", "<italic>")
         .replace("&n", "<underlined>")
         .replace("&m", "<strikethrough>")
         .replace("&k", "<obfuscated>")
         .replace("&r", "<reset>");
   }
}
