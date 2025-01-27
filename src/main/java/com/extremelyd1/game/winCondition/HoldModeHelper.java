package com.extremelyd1.game.winCondition;

import com.extremelyd1.game.Game;
import com.extremelyd1.game.team.PlayerTeam;
import com.extremelyd1.game.team.TeamManager;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;
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

    private static void nestedMaterialAdd(ItemStack itemStack, Consumer<Material> consumer) {
        if (itemStack != null && !itemStack.isEmpty()) {
            consumer.accept(itemStack.getType());
            if (itemStack.getItemMeta() instanceof BlockStateMeta im) {
	            if (im.getBlockState() instanceof ShulkerBox shulker) {
                    for (ItemStack shulkerItem : shulker.getInventory()) {
                        nestedMaterialAdd(shulkerItem, consumer);
                    }
                }
            } else if (itemStack.getItemMeta() instanceof BundleMeta bm) {
                for (ItemStack bundleItem : bm.getItems()) {
                    nestedMaterialAdd(bundleItem, consumer);
                }
            }
        }
    }

    private static Set<Material> createInventorySnapshot(PlayerTeam team) {
        Set<Material> materials = Sets.newHashSet();
        Consumer<ItemStack> add = itemStack -> nestedMaterialAdd(itemStack, materials::add);

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
