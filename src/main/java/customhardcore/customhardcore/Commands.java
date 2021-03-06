package customhardcore.customhardcore;

import customhardcore.customhardcore.Enums.ConfigurationValues;
import customhardcore.customhardcore.Enums.InvUI;
import customhardcore.customhardcore.Helpers.ConfigurationHelper;
import customhardcore.customhardcore.Helpers.Msg;
import customhardcore.customhardcore.Helpers.PlayerHelper;
import customhardcore.customhardcore.Helpers.ScoreboardHelper;
import customhardcore.customhardcore.Levelling.PlayerData;
import customhardcore.customhardcore.Levelling.PlayerSave;
import customhardcore.customhardcore.UI.UIHelper;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class Commands implements CommandExecutor {
    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nullable String label,
                             @Nullable String[] args) {
        switch (command.getName()) {
            case "get_player_deaths":
                getPlayerDeaths(args, sender);
                break;
            case "set_starting_lives":
                setStartingLives(sender, args);
                break;
            case "set_remaining_lives":
                setPlayerRemainingLives(sender, args);
                break;
            case "open_config":
                openUI(sender, InvUI.CONFIGURATION);
                break;
            case "points_shop":
                openUI(sender, InvUI.SHOP);
                break;
            case "settings":
                openUI(sender, InvUI.SETTINGS);
                break;
            case "xp":
                getXp(sender, args);
                break;
        }
        return true;
    }

    private void getXp(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return;
        Player player = (Player) sender;

        if (args.length <= 0) {
            PlayerData playerData = PlayerSave.getPlayerData(player);
            Msg.send(player, String.format("Your xp: %s. You have %s xp left until level %s",
                    playerData.getXp(), playerData.getXpLeft(),
                    (playerData.getLevel()+1)));
        } else {
            String username = args[0];
            Player target = Bukkit.getPlayer(username);
            if (target == null) {
                Msg.send(player, "&4That player is not online");
                return;
            }
            PlayerData targetPlayerData = PlayerSave.getPlayerData(target);
            Msg.send(target, String.format("%s's xp: %s. You have %s xp left until level %s",
                    username, targetPlayerData.getXp(), targetPlayerData.getXpLeft(),
                    (targetPlayerData.getLevel()+1)));
        }

    }

    private void openUI(CommandSender sender, InvUI invUI) {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;

        UIHelper.createInventoryUI(player, invUI);
    }

    private void setPlayerRemainingLives(CommandSender sender, String[] args) {
        if (args == null || args.length < 2) {
            Msg.send(sender, "This command requires a username, followed with the number of deaths you want to send",
                    "&4");
            return;
        }
        Player target = Bukkit.getServer().getPlayer(args[0]);
        if (target == null) {
            Msg.send(sender, String.format("%s is not online", args[0]), "&4");
            return;
        }

        PlayerData playerData = PlayerSave.getPlayerData(target);
        playerData.setLives(Integer.parseInt(args[1]));
        PlayerSave.replacePlayer(playerData);
        Msg.send(sender, String.format("Successfully set &b%s lives to &b%o",
                target.getName(),
                Integer.parseInt(args[1])), "&3");

        ScoreboardHelper.updatePlayerBoards();
    }

    private void setStartingLives(CommandSender sender, @Nullable String[] args) {
        if (args == null || args.length == 0) {
            Msg.send(sender, "This command requires a number as an argument", "&4");
            return;
        }
        ConfigurationHelper.getConfig().set(ConfigurationValues.STARTING_LIVES.name(),
                Integer.valueOf(args[0]));
        ConfigurationHelper.save();
        Msg.send(sender, "Maximum deaths successfully set to &b" + Integer.valueOf(args[0]), "&3");
    }

    private void getPlayerDeaths(String[] args, CommandSender sender) {
        Optional<Player> optionalPlayer = PlayerHelper.checkIfSenderIsPlayer(sender);
        if (!optionalPlayer.isPresent()) {
            Msg.send(sender, "Must be a player to send this command", "&4");
            return;
        }
        Player player = optionalPlayer.get();

        if (args != null && args.length > 0) {
            String targetUsername = args[0];
            Player targetPlayer = Bukkit.getPlayer(targetUsername);
            if (targetPlayer == null) {
                Msg.send(player, String.format("%s is not currently online", targetUsername), "&2");
                return;
            }
            Msg.send(player, String.format("%s has %o deaths", targetPlayer.getName(),
                    targetPlayer.getStatistic(Statistic.DEATHS)), "&2");
        } else
            Msg.send(player, String.format("You have %o deaths", player.getStatistic(Statistic.DEATHS)), "&2");
    }

}
