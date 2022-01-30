package customhardcore.customhardcore;

import customhardcore.customhardcore.Helpers.*;
import customhardcore.customhardcore.Helpers.Levelling.PlayerSave;
import customhardcore.customhardcore.Helpers.UI.InventoryEvents;
import customhardcore.customhardcore.Helpers.UI.UIHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EventListeners implements Listener {

    @EventHandler
    public void onPlayerDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player)
            PlayerHelper.onDeathEvent(((Player) event.getEntity()));

        if (event.getEntity().getKiller() != null) {
            Player killer = event.getEntity().getKiller();
            PlayerSave.addXp(killer, event.getDroppedExp());
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = ((Player) event.getEntity());
            ScoreboardHelper.createOrUpdatePlayerBoard(player);
            if (event.getDamage() > player.getHealth()) {
                event.setCancelled(true);
                PlayerHelper.onDeathEvent(player);
            }
        }
    }

    @EventHandler
    public void onPlayerClickSign(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getClickedBlock().getType().equals(Material.WARPED_SIGN) &&
                event.getClickedBlock().hasMetadata("finish-line")) {
            Player player = event.getPlayer();
            Msg.send(player, "Congratulations! Let's take you home.", "&2&l");
            Location location = player.getBedSpawnLocation();
            if (location != null)
                player.teleport(location);
            else
                player.teleport(Objects.requireNonNull(Bukkit.getWorld("world")).getSpawnLocation());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerSave.initialisePlayer(event.getPlayer());
        if (ConfigurationHelper.isMaxDeathsEnabled())
            ScoreboardHelper.updatePlayerBoards();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        System.out.println("Removing board for " + event.getPlayer().getName());
        PlayerSave.removePlayer(event.getPlayer());
        ScoreboardHelper.removeBoard(event.getPlayer());
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(CustomHardcore.getInstance(), ScoreboardHelper::updatePlayerBoards, 10L);
    }

    @EventHandler
    public void onHeal(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player)
            Bukkit.getServer().getOnlinePlayers().forEach(ScoreboardHelper::createOrUpdatePlayerBoard);
    }

    @EventHandler
    public void onInventoryItemTouched(InventoryClickEvent event) {
        if (event.getView().getTitle().equalsIgnoreCase("Configuration"))
            InventoryEvents.configurationEvent(event);
        if (event.getView().getTitle().equalsIgnoreCase("Point Shop"))
            InventoryEvents.shopEvent(event);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerSave.addXp(player, Math.round(event.getBlock().getType().getHardness()));
    }

}
