package javamon;

import java.util.List;
import java.util.Random;

public class AbilityLogic {

    private static final Random random = new Random();

    public static String execute(Ability ability, Monster user, Monster target, List<Monster> team, String trainerClass) {
        String type = ability.getType(); 
        StringBuilder log = new StringBuilder();

        // --- 1. SUPPORT MOVES (Target SELF) ---
        if (type.equalsIgnoreCase("Healing") || type.equalsIgnoreCase("Buff")) {
            return executeSupportMove(ability, user, trainerClass);
        }

        // --- 2. OFFENSIVE MOVES (Target ENEMY) ---
        
        // Check Critical Hit
     // REBALANCED CRITICAL HIT SYSTEM
        double critChance = 0.0625; // Base 6.25%
        
        if ("AGGRESSOR".equalsIgnoreCase(trainerClass)) {
            critChance = 0.15; // 15% for Aggressor
        }
        
        if (ability.getName().contains("Razor") || ability.getName().contains("Slash")) {
            critChance += 0.10;
        }
        
        boolean isCrit = random.nextDouble() < critChance;
        double critMult = isCrit ? BattleMechanics.CRIT_MULTIPLIER : 1.0;
        
        if (isCrit) log.append("[CRIT] ");

        // Class Perk: Mystic (Random Status)
        if ("MYSTIC".equalsIgnoreCase(trainerClass) && random.nextDouble() < 0.30) {
            applyRandomStatus(target, log);
        }
        
        // Specific Debuff Logic based on Move Name
        if (ability.getName().contains("Spore") || ability.getName().contains("Void")) {
             target.addStatus("Sleep", 2, "STOP");
             log.append(" Enemy fell asleep! ");
        }
        else if (ability.getName().contains("Thunder Wave")) {
             target.addStatus("Paralysis", 3, "STOP");
             log.append(" Enemy Paralyzed! ");
        }
        else if (ability.getName().contains("String Shot")) {
             target.addStatus("Slow", 3, "DEBUFF"); // Just a visual tag for now
             log.append(" Speed fell! ");
        }

        // Secret Skill Effects (Attack Variations)
        if (ability.getId() == 999) {
             if ("AGGRESSOR".equalsIgnoreCase(trainerClass)) {
                 user.addStatus("Def Down", 2, "DEBUFF");
                 log.append(" (Reckless!)");
             }
        }

        // Calculate Damage using SCALED stats
        int baseDmg = BattleMechanics.calculateDamage(user, target, ability, trainerClass);
        int finalDmg = (int)(baseDmg * critMult);
        
        target.setCurrentHP(target.getCurrentHP() - finalDmg);
        
        // Type Effectiveness Log
        double mult = BattleMechanics.getTypeMultiplier(type, target.getType(), trainerClass);
        if (mult > 1.0) log.append("Super Effective! ");
        else if (mult < 1.0) log.append("Not Effective... ");
        
        log.append("Hit for ").append(finalDmg).append(" damage.");
        
        // Recoil Check
        if (ability.getName().contains("Brave Bird")) {
            int recoil = finalDmg / 4;
            user.setCurrentHP(user.getCurrentHP() - recoil);
            log.append(" Took recoil!");
        }

        return log.toString();
    }

    private static String executeSupportMove(Ability a, Monster user, String trainerClass) {
        double boostMult = "STRATEGIST".equalsIgnoreCase(trainerClass) ? 1.25 : 1.0;
        
        if (a.getType().equalsIgnoreCase("Healing")) {
            int healAmt = (int)(user.getMaxHP() * 0.40 * boostMult);
            user.setCurrentHP(user.getCurrentHP() + healAmt);
            return "Restored " + healAmt + " HP!";
        } 
        else if (a.getType().equalsIgnoreCase("Buff")) {
            // Apply generic buffs
            if (a.getName().contains("Defense") || a.getName().contains("Shield") || a.getName().contains("Harden") || a.getName().contains("Hail")) {
                user.addStatus("Def Up", 3, "BUFF");
                return "Defense Rose!";
            }
            if (a.getName().contains("Kindle") || a.getName().contains("Plot") || a.getName().contains("Charge")) {
                user.addStatus("Atk Up", 3, "BUFF");
                return "Attack Rose!";
            }
            if (a.getName().contains("Tailwind")) {
                user.addStatus("Spd Up", 3, "BUFF");
                return "Speed Rose!";
            }
            
            // Default Buff Fallback
            user.addStatus("Powered Up", 3, "BUFF");
            return "Stats increased!";
        }
        return "Used " + a.getName();
    }
    
    private static void applyRandomStatus(Monster target, StringBuilder log) {
        double r = random.nextDouble();
        if (r < 0.33) { target.addStatus("Burn", 3, "DMG"); log.append(" Burned!"); }
        else if (r < 0.66) { target.addStatus("Paralysis", 3, "STOP"); log.append(" Paralyzed!"); }
        else { target.addStatus("Freeze", 1, "STOP"); log.append(" Frozen!"); }
    }
}