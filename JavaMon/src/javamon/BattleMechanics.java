package javamon;

import java.util.HashMap;
import java.util.Map;

public class BattleMechanics {

private static final Map<String, String[]> STRENGTH_MAP = new HashMap<>();
    
    // Balance Constants
    private static final double BASE_DAMAGE_MULTIPLIER = 0.4; // Reduced from 0.8
    public static final double CRIT_MULTIPLIER = 1.3; // Reduced from 1.5
    private static final double MAX_DAMAGE_PERCENT = 0.45; // Max 45% HP per hit
    private static final double MIN_DAMAGE_PERCENT = 0.08; // Min 8% HP per hit

    

    static {
        STRENGTH_MAP.put("Water", new String[]{"Fire", "Ground"});
        STRENGTH_MAP.put("Fire", new String[]{"Grass", "Bug", "Ice"});
        STRENGTH_MAP.put("Grass", new String[]{"Water", "Ground"});
        STRENGTH_MAP.put("Bug", new String[]{"Grass", "Dark"});
        STRENGTH_MAP.put("Lightning", new String[]{"Water", "Flying"});
        STRENGTH_MAP.put("Ground", new String[]{"Fire", "Lightning"});
        STRENGTH_MAP.put("Flying", new String[]{"Grass", "Bug"});
        STRENGTH_MAP.put("Ice", new String[]{"Flying", "Grass", "Ground"});
        STRENGTH_MAP.put("Dark", new String[]{"Ice", "Fire"});
    }

    public static double getTypeMultiplier(String attackType, String defenderType, String trainerClass) {
        double multiplier = 1.0;
        
        if (STRENGTH_MAP.containsKey(attackType)) {
            for (String type : STRENGTH_MAP.get(attackType)) {
                if (type.equalsIgnoreCase(defenderType)) {
                    multiplier = 1.5; // Base super effective
                    
                    // Elementalist bonus: 1.5x → 1.8x (not 2.0x)
                    if ("ELEMENTALIST".equalsIgnoreCase(trainerClass)) {
                        multiplier = 1.8; 
                    }
                    return multiplier;
                }
            }
        }
        
        if (isWeakAgainst(attackType, defenderType)) {
            return 0.6; // Buffed from 0.5
        }
        
        return 1.0; 
    }
    
    private static boolean isWeakAgainst(String atk, String def) {
        switch (atk) {
            case "Water": return def.equals("Grass") || def.equals("Lightning");
            case "Fire": return def.equals("Water") || def.equals("Ground");
            case "Grass": return def.equals("Fire") || def.equals("Flying") || def.equals("Bug");
            case "Lightning": return def.equals("Ground");
            case "Ground": return def.equals("Grass") || def.equals("Ice");
            case "Flying": return def.equals("Lightning") || def.equals("Ice");
            case "Ice": return def.equals("Fire");
            case "Bug": return def.equals("Fire") || def.equals("Flying");
            case "Dark": return def.equals("Lightning");
            default: return false;
        }
    }

    public static int calculateDamage(Monster attacker, Monster defender, Ability ability, String trainerClass) {
        // Base power determination
        int basePower = getBasePower(ability, trainerClass);
        
        // Get stats with status modifiers
        double atkStat = getModifiedAttack(attacker);
        double defStat = getModifiedDefense(defender);
        
        // Type effectiveness
        double typeMult = getTypeMultiplier(ability.getType(), defender.getType(), trainerClass);
        
        // Class-specific bonuses
        double classMult = getClassMultiplier(attacker, trainerClass, ability);
        
        // === NEW DAMAGE FORMULA WITH DIMINISHING RETURNS ===
        
        // 1. Calculate stat ratio with square root
        double statRatio = Math.sqrt(atkStat / Math.max(1, defStat));
        
        // 2. Base damage with scaling
        double baseDamage = basePower * statRatio * BASE_DAMAGE_MULTIPLIER;
        
        // 3. Apply multipliers
        double finalDamage = baseDamage * typeMult * classMult;
        
        // 4. Level bonus
        double levelBonus = 1.0 + (attacker.getLevel() * 0.02);
        finalDamage *= levelBonus;
        
        // 5. Reduced variance
        double variance = 0.92 + (Math.random() * 0.08);
        finalDamage *= variance;
        
        // 6. Damage caps based on defender's max HP
        int maxDamage = (int)(defender.getMaxHP() * MAX_DAMAGE_PERCENT);
        int minDamage = (int)(defender.getMaxHP() * MIN_DAMAGE_PERCENT);
        
        finalDamage = Math.max(minDamage, Math.min(maxDamage, finalDamage));
        
        return (int) finalDamage;
    }
    
    private static int getBasePower(Ability ability, String trainerClass) {
        if (ability.getId() == 999) {
            int power = 90;
            if ("AGGRESSOR".equalsIgnoreCase(trainerClass)) power = 110;
            else if ("ELEMENTALIST".equalsIgnoreCase(trainerClass)) power = 100;
            return power;
        }
        
        String moveName = ability.getName().toLowerCase();
        
        if (moveName.contains("blast") || moveName.contains("thunder") || 
            moveName.contains("blizzard") || moveName.contains("beam")) {
            return 75;
        }
        
        if (moveName.contains("slam") || moveName.contains("wing") || 
            moveName.contains("crunch") || moveName.contains("earthquake")) {
            return 65;
        }
        
        return 50;
    }
    
    private static double getModifiedAttack(Monster attacker) {
        double atkStat = attacker.getAttack();
        
        for (StatusEffect s : attacker.getActiveStatuses()) {
            if (s.name.contains("Atk Up")) atkStat *= 1.4;
            if (s.name.contains("Powered Up")) atkStat *= 1.25;
        }
        
        return atkStat;
    }
    
    private static double getModifiedDefense(Monster defender) {
        double defStat = defender.getDefense();
        
        for (StatusEffect s : defender.getActiveStatuses()) {
            if (s.name.contains("Def Up")) defStat *= 1.4;
            if (s.name.contains("Def Down")) defStat *= 0.75;
        }
        
        return defStat;
    }
    
    private static double getClassMultiplier(Monster attacker, String trainerClass, Ability ability) {
        double mult = 1.0;
        
        if ("AGGRESSOR".equalsIgnoreCase(trainerClass) && !isSupportMove(ability)) {
            mult = 1.15;
        }
        
        if ("BEASTMASTER".equalsIgnoreCase(trainerClass)) {
            double hpPercent = (double) attacker.getCurrentHP() / attacker.getMaxHP();
            
            if (hpPercent < 0.3) {
                mult = 1.35;
            } else if (hpPercent < 0.5) {
                mult = 1.2;
            }
        }
        
        return mult;
    }
    
    public static String getDamagePreview(Ability move, Monster attacker, Monster defender, String trainerClass) {
        if (isSupportMove(move)) {
            return "N/A";
        }
        
        int basePower = getBasePower(move, trainerClass);
        double atkStat = getModifiedAttack(attacker);
        double defStat = getModifiedDefense(defender);
        double typeMult = getTypeMultiplier(move.getType(), defender.getType(), trainerClass);
        double classMult = getClassMultiplier(attacker, trainerClass, move);
        
        double statRatio = Math.sqrt(atkStat / Math.max(1, defStat));
        double baseDamage = basePower * statRatio * BASE_DAMAGE_MULTIPLIER;
        double fullDamage = baseDamage * typeMult * classMult;
        
        double levelBonus = 1.0 + (attacker.getLevel() * 0.02);
        fullDamage *= levelBonus;
        
        int minDmg = (int)(fullDamage * 0.92);
        int maxDmg = (int)(fullDamage * 1.0);
        
        int hardMax = (int)(defender.getMaxHP() * MAX_DAMAGE_PERCENT);
        int hardMin = (int)(defender.getMaxHP() * MIN_DAMAGE_PERCENT);
        
        minDmg = Math.max(hardMin, Math.min(hardMax, minDmg));
        maxDmg = Math.max(hardMin, Math.min(hardMax, maxDmg));
        
        return minDmg + " - " + maxDmg;
    }
    
    public static boolean isSupportMove(Ability a) {
        String type = a.getType().toLowerCase();
        return type.contains("healing") || type.contains("buff") || type.contains("defense") || type.contains("debuff");
    }
    
 // Inside the public class BattleMechanics { ... }

    public static int calculateApproxDamage(Ability move, Monster attacker, Monster defender, String trainerClass) {
        if (move.getType().equals("Buff") || move.getType().equals("Healing")) {
            return 0;
        }
        
        // Simplistic damage approximation formula:
        // Damage = ((Attacker_ATK / Defender_DEF) * Base_Power) * Type_Multiplier * Class_Bonus

        // Base damage (using a standard base power like 50 for all moves for approximation)
        int basePower = (move.getId() == 999) ? 100 : 50; 
        
        double attackStat = attacker.getAttack();
        double defenseStat = defender.getDefense();
        
        // Type Multiplier (You already have this in GameWindow.java's logic)
        double typeMultiplier = BattleMechanics.getTypeMultiplier(move.getType(), defender.getType(), trainerClass);
        
        // Implement the Class Bonus approximation (matching class bonus logic if applicable)
        double classBonus = 1.0;
        if (trainerClass.equalsIgnoreCase("ELEMENTALIST") && typeMultiplier > 1.0) {
            classBonus = 1.5; 
        }
        
        // Calculation: Damage = (ATK / DEF * Base Power * Multipliers)
        double rawDamage = (attackStat / defenseStat) * basePower * typeMultiplier * classBonus;
        
        // Add a random variance for a slightly better prediction (e.g., +/- 10%)
        double variance = (Math.random() * 0.2) + 0.9; // Value between 0.9 and 1.1
        
        int finalDamage = (int) (rawDamage * variance);
        
        // Ensure minimum damage
        return Math.max(1, finalDamage);
    }
}