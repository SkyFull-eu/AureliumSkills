package io.github.archy_x.aureliumskills.skills.abilities;

import io.github.archy_x.aureliumskills.AureliumSkills;
import io.github.archy_x.aureliumskills.Options;
import io.github.archy_x.aureliumskills.skills.PlayerSkill;
import io.github.archy_x.aureliumskills.skills.SkillLoader;
import io.github.archy_x.aureliumskills.skills.Source;
import io.github.archy_x.aureliumskills.util.XMaterial;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class ExcavationAbilities implements Listener {

	private static final Random r = new Random();
	
	public static double getModifiedXp(Player player, Source source) {
		PlayerSkill skill = SkillLoader.playerSkills.get(player.getUniqueId());
		double output = Options.getXpAmount(source);
		double modifier = 1;
		modifier += Ability.EXCAVATOR.getValue(skill.getAbilityLevel(Ability.EXCAVATOR)) / 100;
		output *= modifier;
		return output;
	}
	
	@EventHandler
	public void spadeMaster(EntityDamageByEntityEvent event) {
		if (!event.isCancelled()) {
			//Checks if entity is damaged by player
			if (event.getDamager() instanceof Player) {
				Player player = (Player) event.getDamager();
				if (SkillLoader.playerSkills.containsKey(player.getUniqueId())) {
					//Checks if item used is a shovel
					Material mat = player.getInventory().getItemInMainHand().getType();
					if (mat.equals(XMaterial.DIAMOND_SHOVEL.parseMaterial()) || mat.equals(XMaterial.IRON_SHOVEL.parseMaterial()) || mat.equals(XMaterial.GOLDEN_SHOVEL.parseMaterial()) ||
							mat.equals(XMaterial.STONE_SHOVEL.parseMaterial()) || mat.equals(XMaterial.WOODEN_SHOVEL.parseMaterial())) {
						PlayerSkill s = SkillLoader.playerSkills.get(player.getUniqueId());
						//Mutliplies damage
						event.setDamage(event.getDamage() * (1 + (Ability.SPADE_MASTER.getValue(s.getAbilityLevel(Ability.SPADE_MASTER)) / 100)));
					}
				}
			}
		}
	}
	
	@EventHandler
	public void biggerScoop(BlockBreakEvent event) {
		if (!event.isCancelled()) {
			if (event.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
				Material mat = event.getBlock().getType();
				if (mat.equals(Material.DIRT) || mat.equals(XMaterial.GRASS_BLOCK.parseMaterial()) || mat.equals(Material.SAND) || mat.equals(Material.SOUL_SAND)
						|| mat.equals(Material.GRAVEL) || mat.equals(XMaterial.MYCELIUM.parseMaterial()) || mat.equals(Material.CLAY)) {
					if (SkillLoader.playerSkills.containsKey(event.getPlayer().getUniqueId())) {
						PlayerSkill skill = SkillLoader.playerSkills.get(event.getPlayer().getUniqueId());
						if (r.nextDouble() < (Ability.BIGGER_SCOOP.getValue(skill.getAbilityLevel(Ability.BIGGER_SCOOP)) / 100)) {
							//Applies triple drops
							for (ItemStack item : event.getBlock().getDrops()) {
								event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().add(0.5, 0.5, 0.5), item);
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void metalDetector(BlockBreakEvent event) {
		if (!event.isCancelled()) {
			if (event.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
				Material mat = event.getBlock().getType();
				if (mat.equals(Material.DIRT) || mat.equals(XMaterial.GRASS_BLOCK.parseMaterial()) || mat.equals(Material.SAND) || mat.equals(Material.SOUL_SAND)
						|| mat.equals(Material.GRAVEL) || mat.equals(XMaterial.MYCELIUM.parseMaterial()) || mat.equals(Material.CLAY)) {
					if (SkillLoader.playerSkills.containsKey(event.getPlayer().getUniqueId())) {
						PlayerSkill skill = SkillLoader.playerSkills.get(event.getPlayer().getUniqueId());
						if (r.nextDouble() < (Ability.METAL_DETECTOR.getValue(skill.getAbilityLevel(Ability.METAL_DETECTOR)) / 100)) {
							ItemStack drop = AureliumSkills.lootTableManager.getLootTable("excavation-rare").getLoot().get(r.nextInt(AureliumSkills.lootTableManager.getLootTable("excavation-rare").getLoot().size())).getDrop();
							event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().add(0.5, 0.5, 0.5), drop);
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void luckySpades(BlockBreakEvent event) {
		if (!event.isCancelled()) {
			if (event.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
				Material mat = event.getBlock().getType();
				if (mat.equals(Material.DIRT) || mat.equals(XMaterial.GRASS_BLOCK.parseMaterial()) || mat.equals(Material.SAND) || mat.equals(Material.SOUL_SAND)
						|| mat.equals(Material.GRAVEL) || mat.equals(XMaterial.MYCELIUM.parseMaterial()) || mat.equals(Material.CLAY)) {
					if (SkillLoader.playerSkills.containsKey(event.getPlayer().getUniqueId())) {
						PlayerSkill skill = SkillLoader.playerSkills.get(event.getPlayer().getUniqueId());
						if (r.nextDouble() < (Ability.LUCKY_SPADES.getValue(skill.getAbilityLevel(Ability.LUCKY_SPADES)) / 100)) {
							ItemStack drop = AureliumSkills.lootTableManager.getLootTable("excavation-epic").getLoot().get(r.nextInt(AureliumSkills.lootTableManager.getLootTable("excavation-epic").getLoot().size())).getDrop();
							event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().add(0.5, 0.5, 0.5), drop);
						}
					}
				}
			}
		}
	}
}