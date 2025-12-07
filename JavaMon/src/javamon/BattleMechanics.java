package javamon;

import java.util.HashMap;
import java.util.Map;

public class BattleMechanics {

    private static final Map<String, String[]> STRENGTH_MAP = new HashMap<>();

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
                    multiplier = 1.5;
                    if ("ELEMENTALIST".equalsIgnoreCase(trainerClass)) multiplier = 2.0; 
                    return multiplier;
                }
            }
        }
        if (isWeakAgainst(attackType, defenderType)) return 0.5;
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
        int power = 60; 
        
        if (ability.getId() == 999) {
            power = 100; // Secret Skill Base
            if ("AGGRESSOR".equalsIgnoreCase(trainerClass)) power = 130;
            if ("ELEMENTALIST".equalsIgnoreCase(trainerClass)) power = 120;
        }

        double typeMult = getTypeMultiplier(ability.getType(), defender.getType(), trainerClass);
        double classMult = ("AGGRESSOR".equalsIgnoreCase(trainerClass) && !isSupportMove(ability)) ? 1.25 : 1.0;
        
        if ("BEASTMASTER".equalsIgnoreCase(trainerClass) && attacker.getCurrentHP() < (attacker.getBaseHP() / 2)) {
            classMult += 0.3;
        }

        double damage = ((double)attacker.getBaseAttack() / Math.max(1, defender.getBaseDefense())) * power * 1.5 * typeMult * classMult;
        damage *= (0.9 + (Math.random() * 0.2)); 

        return (int) Math.max(10, damage); 
    }
    
    public static boolean isSupportMove(Ability a) {
        String type = a.getType().toLowerCase();
        return type.contains("healing") || type.contains("buff") || type.contains("defense") || type.contains("debuff");
    }
}