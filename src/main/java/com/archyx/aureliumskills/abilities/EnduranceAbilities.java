package com.archyx.aureliumskills.abilities;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.skills.PlayerSkill;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.SkillLoader;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import java.util.Random;

public class EnduranceAbilities extends AbilityProvider implements Listener {

    private final Random r = new Random();

    public EnduranceAbilities(AureliumSkills plugin) {
        super(plugin, Skill.ENDURANCE);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void antiHunger(FoodLevelChangeEvent event) {
        if (blockDisabled(Ability.ANTI_HUNGER)) return;
        if (!event.isCancelled()) {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                if (blockAbility(player)) return;
                //Checks if food level would be decreased
                if (player.getFoodLevel() > event.getFoodLevel()) {
                    if (SkillLoader.playerSkills.containsKey(player.getUniqueId())) {
                        PlayerSkill playerSkill = SkillLoader.playerSkills.get(player.getUniqueId());
                        double chance = getValue(Ability.ANTI_HUNGER, playerSkill) / 100;
                        if (r.nextDouble() < chance) {
                            event.setFoodLevel(player.getFoodLevel());
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void goldenHealAndRecovery(EntityRegainHealthEvent event) {
        if (OptionL.isEnabled(Skill.ENDURANCE)) {
            if (!event.isCancelled()) {
                if (event.getEntity() instanceof Player) {
                    Player player = (Player) event.getEntity();
                    if (blockAbility(player)) return;
                    if (SkillLoader.playerSkills.containsKey(player.getUniqueId())) {
                        PlayerSkill playerSkill = SkillLoader.playerSkills.get(player.getUniqueId());
                        //Golden Heal
                        if (event.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.MAGIC_REGEN)) {
                            if (isEnabled(Ability.GOLDEN_HEAL)) {
                                //Applies modifier
                                double modifier = getValue(Ability.GOLDEN_HEAL, playerSkill) / 100;
                                event.setAmount(event.getAmount() * (1 + modifier));
                            }
                        }
                        //Recovery
                        else if (event.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.SATIATED)) {
                            if (isEnabled(Ability.RECOVERY)) {
                                //Gets health
                                AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                                if (attribute != null) {
                                    double currentHealth = player.getHealth();
                                    double maxHealth = attribute.getValue();
                                    //Checks if health is less than half of max
                                    if (currentHealth < (maxHealth / 2)) {
                                        //Applies modifier
                                        double modifier = getValue(Ability.RECOVERY, playerSkill) / 100;
                                        event.setAmount(event.getAmount() * (1 + modifier));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void mealSteal(EntityDamageByEntityEvent event) {
        if (blockDisabled(Ability.MEAL_STEAL)) return;
        if (!event.isCancelled()) {
            //Checks if entity and damager are players
            if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                Player enemy = (Player) event.getEntity();
                if (blockAbility(player)) return;
                if (SkillLoader.playerSkills.containsKey(player.getUniqueId())) {
                    PlayerSkill playerSkill = SkillLoader.playerSkills.get(player.getUniqueId());
                    //Calculates chance
                    double chance = getValue(Ability.MEAL_STEAL, playerSkill) / 100;
                    if (r.nextDouble() < chance) {
                        //Removes food from enemy
                        if (enemy.getFoodLevel() >= 1) {
                            enemy.setFoodLevel(enemy.getFoodLevel() - 1);
                        }
                        //Adds food level to player
                        if (player.getFoodLevel() < 20) {
                            player.setFoodLevel(player.getFoodLevel() + 1);
                        }
                        //Adds saturation if food is full
                        else if (player.getSaturation() < 20f) {
                            player.setSaturation(player.getSaturation() + 1f);
                        }
                    }
                }
            }
        }
    }

}