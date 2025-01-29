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

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

// fallen's fork: add "hold" mode
public class HoldModeHelper {

    private final Game game;
	private final WinConditionChecker winConditionChecker;
	private final TeamManager teamManager;

	private final Map<PlayerTeam, Set<Material>> inventorySnapshots = Maps.newHashMap();
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
            Set<Material> oldSnapshot = inventorySnapshots.get(team);
            Set<Material> newSnapshot = createInventorySnapshot(team);
            if (oldSnapshot != null) {
                for (Material material : oldSnapshot) {
                    if (!newSnapshot.contains(material)) {
                        game.onMaterialDropped(team, material);
                    }
                }
            }
            inventorySnapshots.put(team, newSnapshot);
        }
    }

    private static Set<Material> createInventorySnapshot(PlayerTeam team) {
        Set<Material> materials = Sets.newHashSet();
        Consumer<ItemStack> add = itemStack -> ItemUtil.iterateItemMaterialNested(itemStack, materials::add);

        for (Player player : team.getPlayers()) {
            player.getInventory().forEach(add);
            add.accept(player.getItemOnCursor());
        }
        return materials;
    }

    private void cancelTicking() {
        if (this.tickingTask != null) {
            this.tickingTask.cancel();
            this.tickingTask = null;
        }
    }
}
