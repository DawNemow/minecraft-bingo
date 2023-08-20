package com.extremelyd1.config;

import com.extremelyd1.game.progress.ProgressController;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {

    /**
     * Whether blacklist is enabled
     */
    private final boolean enableBlacklist;

    /**
     * The default number of S tier items
     */
    private int numSTierItems;
    /**
     * The default number of A tier items
     */
    private int numATierItems;
    /**
     * The default number of B tier items
     */
    private int numBTierItems;
    /**
     * The default number of C tier items
     */
    private int numCTierItems;
    /**
     * The default number of D tier items
     */
    private int numDTierItems;

    /**
     * The default number of lines to complete to win
     */
    private final int defaultNumLinesComplete;

    /**
     * Whether to show the currently winning team on the scoreboard
     */
    private final boolean showCurrentlyWinningTeam;

    /**
     * Whether to notify teams if other teams complete a goal
     */
    private final boolean notifyOtherTeamCompletions;

    /**
     * Whether a world border is enabled
     */
    private final boolean borderEnabled;
    /**
     * The size of the world border
     */
    private final int overworldBorderSize;

    /**
     * The size of the nether border
     */
    private final int netherBorderSize;

    /**
     * Whether to prevent teams from spawning in water
     */
    private final boolean preventWaterSpawns;

    /**
     * Whether to give all players all teams maps after the game
     */
    private final boolean showAllMapsPostGame;

    /**
     * Whether all players have all recipes unlocked in their inventory
     */
    private final boolean giveAllRecipes;

    /**
     * Store progression notify config data
     */
    private final ProgressController progressController;

    /**
     * Whether a timer is enabled
     */
    private boolean timerEnabled;
    /**
     * The length of the timer is seconds
     */
    private long timerLength;

    /**
     * Whether to pre-generate the worlds within the border in advance
     */
    private final boolean preGenerateWorlds;
    /**
     * The number of ticks in between generation cycles
     */
    private final int preGenerationTicksPerCycle;
    /**
     * The number of chunks to generate per cycle
     */
    private final int preGenerationChunksPerCycle;

    /**
     * The radius of the pregame border
     */
    private final int preGameBorderRadius;

    /**
     * [fallen's fork]
     * If autosave for all worlds should be disabled
     * - true: no lag from saving chunks, but chunks are hold in memory, causing memory to increase infinitely
     */
    private final boolean autoSaveDisabled;

    /**
     * [fallen's fork]
     * Chunks in the given radius of the spawn location of each team will be force-loaded before the game start
     * radius == 0 means only the spawn location chunk will be loaded
     * radius == 1 means 3x3 chunks will be loaded
     */
    private final int spawnLocationsChunkLoadingRadius;

    /**
     * [fallen's fork]
     * A variant of the "line" mode
     * Any team completing given lines ends the game, but the winner is the team who collect the most items
     */
    private final boolean defaultWinConditionIsQuidditch;

    public Config(JavaPlugin plugin) throws IllegalArgumentException {
        plugin.saveDefaultConfig();
        
        FileConfiguration config = plugin.getConfig();

        enableBlacklist = config.getBoolean("enable-blacklist");
        String defaultItemDistributionString = config.getString("default-item-distribution");

        if (defaultItemDistributionString == null
                || !defaultItemDistributionString.contains(",")) {
            throw new IllegalArgumentException("Default item distribution config value is not parsable");
        }

        String[] itemDistributions = defaultItemDistributionString.split(",");
        if (itemDistributions.length != 5) {
            throw new IllegalArgumentException("Default item distribution config value does not have 5 items");
        }

        numSTierItems = parseItemDistribution(itemDistributions[0]);
        numATierItems = parseItemDistribution(itemDistributions[1]);
        numBTierItems = parseItemDistribution(itemDistributions[2]);
        numCTierItems = parseItemDistribution(itemDistributions[3]);
        numDTierItems = parseItemDistribution(itemDistributions[4]);

        defaultNumLinesComplete = config.getInt("default-num-lines-complete-for-win");

        showCurrentlyWinningTeam = config.getBoolean("show-currently-winning-team");

        notifyOtherTeamCompletions = config.getBoolean("notify-other-team-completions");

        preventWaterSpawns = config.getBoolean("prevent-water-spawns");

        showAllMapsPostGame = config.getBoolean("show-all-maps-after-game");

        giveAllRecipes = config.getBoolean("give-all-recipes");

        progressController = new ProgressController(config);

        borderEnabled = config.getBoolean("border.enable");
        overworldBorderSize = config.getInt("border.overworld-size");
        netherBorderSize = config.getInt("border.nether-size");

        if (overworldBorderSize < netherBorderSize) {
            throw new IllegalArgumentException("Nether border should be at most as large as the overworld border size");
        }

        timerEnabled = config.getBoolean("timer.enable");
        timerLength = config.getInt("timer.length");

        // Only allow pregeneration of worlds if there is the border is enabled
        preGenerateWorlds = borderEnabled && config.getBoolean("pregeneration-mode.enable");

        preGenerationTicksPerCycle = config.getInt("pregeneration-mode.ticks-per-cycle");

        preGenerationChunksPerCycle = config.getInt("pregeneration-mode.chunks-per-cycle");

        preGameBorderRadius = config.getInt("pregame.border-radius");

        // fallen's fork
        autoSaveDisabled = config.getBoolean("auto-save-disabled");
        spawnLocationsChunkLoadingRadius = config.getInt("spawn-locations-chunk-loading-radius");
        defaultWinConditionIsQuidditch = config.getBoolean("default-win-condition-is-quidditch");
    }

    /**
     * Parse the given string value to an integer or throw a exception if not possible
     * @param stringValue The string value to parse
     * @return The parsed integer value
     */
    private int parseItemDistribution(String stringValue) {
        try {
            return Integer.parseInt(stringValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Default item distribution config value has a non-integer value");
        }
    }

    public ProgressController getProgressController() {
        return progressController;
    }

    public boolean isBlacklistEnabled() {
        return enableBlacklist;
    }

    public int getNumSTier() {
        return numSTierItems;
    }

    public int getNumATier() {
        return numATierItems;
    }

    public int getNumBTier() {
        return numBTierItems;
    }

    public int getNumCTier() {
        return numCTierItems;
    }

    public int getNumDTier() {
        return numDTierItems;
    }

    public void setItemDistribution(
            int numSTierItems,
            int numATierItems,
            int numBTierItems,
            int numCTierItems,
            int numDTierItems
    ) {
       this.numSTierItems = numSTierItems;
       this.numATierItems = numATierItems;
       this.numBTierItems = numBTierItems;
       this.numCTierItems = numCTierItems;
       this.numDTierItems = numDTierItems;
    }

    public int getDefaultNumLinesComplete() {
        return defaultNumLinesComplete;
    }

    public boolean showCurrentlyWinningTeam() {
        return showCurrentlyWinningTeam;
    }

    public boolean notifyOtherTeamCompletions() {
        return notifyOtherTeamCompletions;
    }

    public boolean isPreventWaterSpawns() {
        return preventWaterSpawns;
    }

    public boolean isShowAllMapsPostGame() {
        return showAllMapsPostGame;
    }

    public boolean isGiveAllRecipes() {
        return giveAllRecipes;
    }

    public boolean isBorderEnabled() {
        return borderEnabled;
    }

    public int getOverworldBorderSize() {
        return overworldBorderSize;
    }

    public int getNetherBorderSize() {
        return netherBorderSize;
    }

    public boolean isTimerEnabled() {
        return timerEnabled;
    }

    public void setTimerEnabled(boolean timerEnabled) {
        this.timerEnabled = timerEnabled;
    }

    public long getTimerLength() {
        return timerLength;
    }

    public void setTimerLength(long timerLength) {
        this.timerLength = timerLength;
    }

    public boolean isPreGenerateWorlds() {
        return preGenerateWorlds;
    }

    public int getPreGenerationTicksPerCycle() {
        return preGenerationTicksPerCycle;
    }

    public int getPreGenerationChunksPerCycle() {
        return preGenerationChunksPerCycle;
    }

    public int getPreGameBorderRadius() {
        return preGameBorderRadius;
    }

    // fallen's fork
    public boolean isAutoSaveDisabled() {
        return autoSaveDisabled;
    }

      // fallen's fork: add "quidditch" mode
    public int getSpawnLocationsChunkLoadingRadius() {
        return spawnLocationsChunkLoadingRadius;
    }

    // fallen's fork
    public boolean isDefaultWinConditionIsQuidditch() {
        return defaultWinConditionIsQuidditch;
    }
}
