package com.extremelyd1.game.winCondition;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.PlayerTeam;
import com.extremelyd1.game.team.TeamManager;
import com.extremelyd1.util.ItemUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

// fallen's fork: add "hold" mode
public class HoldModeHelper {

    private final Game game;
	private final WinConditionChecker winConditionChecker;
	private final TeamManager teamManager;

	private final Map<PlayerTeam, TeamInventorySnapshot> inventorySnapshots = Maps.newHashMap();
    private BukkitTask tickingTask = null;

	public HoldModeHelper(Game game, WinConditionChecker winConditionChecker, TeamManager teamManager) {
		this.game = game;
		this.winConditionChecker = winConditionChecker;
		this.teamManager = teamManager;
	}

	public void onGameStart() {
        if (!winConditionChecker.isHoldMode()) {
            return;
        }

        this.cancelTicking();

        this.inventorySnapshots.clear();
		this.tickingTask = Bukkit.getScheduler().runTaskTimer(
                game.getPlugin(),
                () -> this.tick(teamManager.getActiveTeams()),
                0, 1
        );
    }

    private void tick(Iterable<PlayerTeam> teams) {
        if (!winConditionChecker.isHoldMode()) {
            return;
        }
        if (game.getState() != Game.State.IN_GAME) {
            this.cancelTicking();
            return;
        }

        for (PlayerTeam team : teams) {
            TeamInventorySnapshot oldSnapshot = inventorySnapshots.get(team);
            TeamInventorySnapshot newSnapshot = TeamInventorySnapshot.create(team, oldSnapshot);
            if (oldSnapshot != null) {
                Set<Material> droppedMaterials = oldSnapshot.diff(newSnapshot);
                for (Material material : droppedMaterials) {
                    game.onMaterialDropped(team, material);
                }
            }
            inventorySnapshots.put(team, newSnapshot);
        }
    }

    private void cancelTicking() {
        if (this.tickingTask != null) {
            this.tickingTask.cancel();
            this.tickingTask = null;
        }
    }

    private static class TeamInventorySnapshot {
        private final PlayerTeam team;
        private final Set<Material> allMaterials;
        private final Map<UUID, Set<Material>> playerMaterials;

	    private TeamInventorySnapshot(PlayerTeam team, Map<UUID, Set<Material>> playerMaterials) {
		    this.team = team;
		    this.playerMaterials = playerMaterials;
		    this.allMaterials = playerMaterials.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
	    }

        public PlayerTeam getTeam() {
            return team;
        }

        private static TeamInventorySnapshot create(PlayerTeam team, @Nullable TeamInventorySnapshot prev) {
            if (prev == null) {
                prev = empty(team);
            }

            Map<UUID, Set<Material>> snapshot = Maps.newHashMap();

            for (Player player : team.getPlayers()) {
                Set<Material> materials = Sets.newHashSet();
                snapshot.put(player.getUniqueId(), materials);

                Consumer<ItemStack> add = itemStack -> ItemUtil.iterateItemMaterialNested(itemStack, materials::add);
                player.getInventory().forEach(add);
                add.accept(player.getItemOnCursor());
            }

            // handle player log out -- keep it
            for (UUID uuid : prev.playerMaterials.keySet()) {
                if (!snapshot.containsKey(uuid)) {
                    snapshot.put(uuid, prev.playerMaterials.get(uuid));
                }
            }

            return new TeamInventorySnapshot(team, snapshot);
        }

        private static TeamInventorySnapshot empty(PlayerTeam team) {
            return new TeamInventorySnapshot(team, Collections.emptyMap());
        }

        // returns this - other
        public Set<Material> diff(TeamInventorySnapshot other) {
            Set<Material> difference = Sets.newHashSet(allMaterials);
            difference.removeAll(other.allMaterials);
            return difference;
        }
    }
}
