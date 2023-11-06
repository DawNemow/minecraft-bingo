package com.extremelyd1.listener;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.PlayerTeam;
import com.extremelyd1.game.team.Team;
import com.extremelyd1.util.ItemUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerJoinLeaveListener implements Listener {

    /**
     * The game instance
     */
    private final Game game;

    public PlayerJoinLeaveListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
        if (game.isMaintenance()) {
            for (OfflinePlayer offlinePlayer : Bukkit.getOperators()) {
                if (e.getUniqueId().equals(offlinePlayer.getUniqueId())) {
                    return;
                }
            }

            e.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    "Game is currently in maintenance mode"
            );
            return;
        }

        if (game.getConfig().isPreGenerateWorlds()) {
            e.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    "Game is currently pre-generating worlds"
            );
            return;
        }

        if (!game.getState().equals(Game.State.PRE_GAME)) {
            for (PlayerTeam team : game.getTeamManager().getActiveTeams()) {
                for (UUID uuid : team.getUUIDs()) {
                    if (uuid.equals(e.getUniqueId())) {
                        return;
                    }
                }
            }

            for (UUID uuid : game.getTeamManager().getSpectatorTeam().getUUIDs()) {
                if (uuid.equals(e.getUniqueId())) {
                    return;
                }
            }

            // fallen's fork: allow player join in mid-game
            if (game.getConfig().isAllowMidGameJoin()) {
                return;
            }

            e.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    "Game is currently in progress"
            );
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        this.game.onPregameUpdate(Bukkit.getOnlinePlayers().size());

        Player player = e.getPlayer();

        if (game.getState().equals(Game.State.PRE_GAME)) {
            player.getInventory().clear();
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(20D);
            player.setFoodLevel(20);
            player.setSaturation(5);

            Location spawnLocation = game.getWorldManager().getSpawnLocation();
            player.teleport(spawnLocation);
            player.setBedSpawnLocation(spawnLocation);
        }

        Team team = game.getTeamManager().getTeamByPlayer(player);
        if (team == null) {
            game.getTeamManager().addPlayerToTeam(player, game.getTeamManager().getSpectatorTeam(), false);

            team = game.getTeamManager().getSpectatorTeam();

            // fallen's fork: allow player join in mid-game - sync scoreboard player list entry
            game.broadcastGameBoard();
        }

        // fallen's fork:
        // - allow player join in mid-game - set spectator mode
        // - fix existing spectator player still being in survival mode when join in mid-game
        if (team.isSpectatorTeam() && !game.getState().equals(Game.State.PRE_GAME)) {
            player.setGameMode(GameMode.SPECTATOR);

            // fallen's fork: give bingo cards of all teams to the spectator player
            if (!ItemUtil.hasBingoCard(player)) {
                for (PlayerTeam t : game.getTeamManager().getActiveTeams()) {
                    player.getInventory().addItem(
                            game.getBingoCardItemFactory().create(game.getBingoCard(), t)
                    );
                }
            }
        }

        e.setJoinMessage(
                ChatColor.GREEN + "+ " + ChatColor.RESET
                        + team.getColor() + player.getName()
                        + ChatColor.WHITE + " joined"
        );
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        // Online players - 1, because the player object is not yet removed from
        // this list when the event is called
        this.game.onPregameUpdate(Bukkit.getOnlinePlayers().size() - 1);

        Player player = e.getPlayer();

        Team team = game.getTeamManager().getTeamByPlayer(player);
        if (team == null) {
            e.setQuitMessage(
                    ChatColor.RED + "- "
                            + player.getName()
                            + ChatColor.WHITE + " left"
            );

            return;
        }

        e.setQuitMessage(
                ChatColor.RED + "- "
                        + team.getColor() + player.getName()
                        + ChatColor.WHITE + " left"
        );
    }

}
