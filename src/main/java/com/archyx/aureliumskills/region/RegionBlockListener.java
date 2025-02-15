package com.archyx.aureliumskills.region;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.skills.excavation.ExcavationSource;
import com.archyx.aureliumskills.skills.farming.FarmingSource;
import com.archyx.aureliumskills.skills.foraging.ForagingSource;
import com.archyx.aureliumskills.skills.mining.MiningSource;
import com.archyx.aureliumskills.source.SourceManager;
import com.archyx.aureliumskills.util.block.BlockFaceUtil;
import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicInteger;

public class RegionBlockListener implements Listener {

    private Material[] customMaterials;

    private final AureliumSkills plugin;
    private final RegionManager regionManager;

    public RegionBlockListener(AureliumSkills plugin) {
        this.plugin = plugin;
        this.regionManager = plugin.getRegionManager();
        SourceManager sourceManager = plugin.getSourceManager();
        customMaterials = new Material[sourceManager.getCustomBlockSet().size()];
        int pos = 0;
        for (XMaterial material : sourceManager.getCustomBlockSet()) {
            customMaterials[pos] = material.parseMaterial();
            pos++;
        }
    }

    public void reloadCustomBlocks() {
        SourceManager sourceManager = plugin.getSourceManager();
        customMaterials = new Material[sourceManager.getCustomBlockSet().size()];
        int pos = 0;
        for (XMaterial material : sourceManager.getCustomBlockSet()) {
            customMaterials[pos] = material.parseMaterial();
            pos++;
        }
    }

    @EventHandler
    public void checkPlace(BlockPlaceEvent event) {
        // Checks if world is blocked
        if (plugin.getWorldManager().isInBlockedCheckWorld(event.getBlock().getLocation())) {
            return;
        }
        // Checks if region is blocked
        if (plugin.isWorldGuardEnabled()) {
            if (plugin.getWorldGuardSupport().isInBlockedCheckRegion(event.getBlock().getLocation())) {
                return;
            }
        }
        // Check block replace
        if (!OptionL.getBoolean(Option.CHECK_BLOCK_REPLACE)) return;
        Material material = event.getBlock().getType();
        Block block = event.getBlock();
        // Add all foraging, mining, and excavation blocks
        if (MiningSource.getSource(block) != null || ForagingSource.getSource(block) != null || ExcavationSource.getSource(block) != null) {
            regionManager.addPlacedBlock(block);
        }
        // Add farming source if has check block replace
        FarmingSource farmingSource = FarmingSource.getSource(block);
        if (farmingSource != null && farmingSource.shouldCheckBlockReplace()) {
            regionManager.addPlacedBlock(block);
        }
        for (Material checkedMaterial : customMaterials) {
            if (material.equals(checkedMaterial)) {
                regionManager.addPlacedBlock(event.getBlock());
                break;
            }
        }
    }

    @EventHandler
    public void onSandFall(EntityChangeBlockEvent event) {
        Block block = event.getBlock();
        if (!regionManager.isPlacedBlock(block)) return;
        Material type = block.getType();
        if (type == Material.SAND || type == Material.RED_SAND || type == Material.GRAVEL) {
            Block below = block.getRelative(BlockFace.DOWN);
            if (below.getType() == Material.AIR || below.getType().toString().equals("CAVE_AIR") || below.getType().toString().equals("VOID_AIR")
                    || below.getType() == Material.WATER || below.getType().toString().equals("BUBBLE_COLUMN") || below.getType() == Material.LAVA) {
                regionManager.removePlacedBlock(block);
                Entity entity = event.getEntity();
                AtomicInteger counter = new AtomicInteger();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Block currentBlock = entity.getLocation().getBlock();
                        if (entity.isDead() || !entity.isValid()) {
                            if (currentBlock.getType() == type) {
                                regionManager.addPlacedBlock(entity.getLocation().getBlock());
                            }
                            cancel();
                        } else if (currentBlock.getType().toString().contains("WEB")) {
                            cancel();
                        } else if (counter.incrementAndGet() >= 200) {
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 1L, 1L);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void checkBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        regionManager.removePlacedBlock(block);
        checkSugarCane(block, 0);
        checkBlocksRequiringSupportBelow(block);
        checkAmethystCluster(block);
    }

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            regionManager.addPlacedBlock(block.getRelative(event.getDirection()));
        }
        regionManager.removePlacedBlock(event.getBlock().getRelative(event.getDirection()));
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        Block lastBlock = event.getBlock();
        for (Block block : event.getBlocks()) {
            if (regionManager.isPlacedBlock(block)) {
                regionManager.addPlacedBlock(block.getRelative(event.getDirection()));
                if (block.getLocation().distanceSquared(event.getBlock().getLocation()) > lastBlock.getLocation().distanceSquared(event.getBlock().getLocation())) {
                    lastBlock = block;
                }
            }
        }
        regionManager.removePlacedBlock(lastBlock);
    }

    private void checkSugarCane(Block block, int num) {
        if (num < 20) {
            Block above = block.getRelative(BlockFace.UP);
            if (XBlock.isSugarCane(above.getType())) {
                if (regionManager.isPlacedBlock(above)) {
                    regionManager.removePlacedBlock(above);
                    checkSugarCane(above, num + 1);
                }
            }
        }
    }

    private void checkBlocksRequiringSupportBelow(Block block) {
        // Check if the block above requires support
        Block above = block.getRelative(BlockFace.UP);
        ForagingSource source = ForagingSource.getSource(above);
        if (source != null && source.requiresBlockBelow() && regionManager.isPlacedBlock(above)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Remove if block was destroyed
                    if (!source.isMatch(block)) {
                        regionManager.removePlacedBlock(above);
                    }
                }
            }.runTaskLater(plugin, 1);
        }
    }

    private void checkAmethystCluster(Block block) {
        // Check each side
        for (BlockFace face : BlockFaceUtil.getBlockSides()) {
            Block checkedBlock = block.getRelative(face);
            if (MiningSource.AMETHYST_CLUSTER.isMatch(block) && regionManager.isPlacedBlock(checkedBlock)) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Remove if block was destroyed
                        if (!MiningSource.AMETHYST_CLUSTER.isMatch(block)) {
                            regionManager.removePlacedBlock(block);
                        }
                    }
                }.runTaskLater(plugin, 1);
            }
        }
    }
}
