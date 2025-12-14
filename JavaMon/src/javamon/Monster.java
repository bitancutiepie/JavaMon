package javamon;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

/**
 * IMPROVED MONSTER CLASS V2.0
 * 
 * KEY CHANGES:
 * 1. Better HP scaling (survives 3-4 hits minimum)
 * 2. More balanced stat growth per level
 * 3. Added stat stage modifiers for buffs/debuffs
 * 4. Improved status effect system
 */
public class Monster {
    private final int id;
    private final String name;
    private final String type;
    
    private final int baseHP;
    private final int baseAttack;
    private final int baseDefense;
    private final int baseSpeed;
    
    private final List<Ability> abilities;
    
    private int level;
    private int xp;
    private int xpToNextLevel;

    private int currentHP;
    private Image image;
    private List<StatusEffect> activeStatuses;
    
    // NEW: Stat stage modifiers (-6 to +6 like Pokemon)
    private int atkStage = 0;
    private int defStage = 0;
    private int spdStage = 0;

    public Monster(int id, String name, String type, int baseHP, int baseAttack, int baseDefense, int baseSpeed, 
                   Ability a1, Ability a2, Ability a3, Ability a4) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.baseHP = baseHP;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.baseSpeed = baseSpeed;
        this.abilities = Arrays.asList(a1, a2, a3, a4);
        
        this.level = 1;
        this.xp = 0;
        this.xpToNextLevel = 100;
        
        this.activeStatuses = new ArrayList<>();
        this.currentHP = getMaxHP(); 
    }

    public Monster copy() {
        Monster m = new Monster(
            this.id, this.name, this.type, this.baseHP, 
            this.baseAttack, this.baseDefense, this.baseSpeed, 
            this.abilities.get(0), this.abilities.get(1), this.abilities.get(2), this.abilities.get(3)
        );
        m.setLevel(this.level);
        m.xp = this.xp;
        m.xpToNextLevel = this.xpToNextLevel;
        m.currentHP = m.getMaxHP(); 
        return m;
    }

    public void setLevel(int lvl) {
        this.level = lvl;
        this.xpToNextLevel = 100 * lvl;
        this.currentHP = getMaxHP(); 
    }

    public boolean gainXP(int amount) {
        this.xp += amount;
        boolean leveledUp = false;
        while (this.xp >= xpToNextLevel) {
            this.xp -= xpToNextLevel;
            this.level++;
            // Smoother XP curve
            this.xpToNextLevel = 80 + (this.level * 20); 
            leveledUp = true;
            this.currentHP = getMaxHP(); // Full heal on level up
        }
        return leveledUp;
    }

    /**
     * IMPROVED HP SCALING
     * - Higher base multiplier (12 instead of 10)
     * - Ensures monsters can survive multiple hits
     */
    
    public int getMaxHP() { 
        return baseHP + (level * 12); // Buffed from 10
    }
    
    public int getAttack() { 
        int baseAtk = baseAttack + (level * 3); // Buffed from 2
        return applyStatStage(baseAtk, atkStage);
    }
    
    public int getDefense() { 
        int baseDef = baseDefense + (level * 3); // Buffed from 2
        return applyStatStage(baseDef, defStage);
    }
    
    public int getSpeed() { 
        int baseSpd = baseSpeed + (level * 2); // Buffed from 1
        return applyStatStage(baseSpd, spdStage);
    }
    
    private int applyStatStage(int baseStat, int stage) {
        if (stage == 0) return baseStat;
        
        double multiplier = 1.0;
        if (stage > 0) {
            multiplier = 1.0 + (stage * 0.5);
        } else {
            multiplier = Math.max(0.25, 1.0 + (stage * 0.15));
        }
        
        return (int)(baseStat * multiplier);
    }
    
    public void modifyStatStage(String stat, int change) {
        if (stat.equalsIgnoreCase("attack")) {
            atkStage = Math.max(-6, Math.min(6, atkStage + change));
        } else if (stat.equalsIgnoreCase("defense")) {
            defStage = Math.max(-6, Math.min(6, defStage + change));
        } else if (stat.equalsIgnoreCase("speed")) {
            spdStage = Math.max(-6, Math.min(6, spdStage + change));
        }
    }
    
    public void resetStatStages() {
        atkStage = 0;
        defStage = 0;
        spdStage = 0;
    }
    
    /**
     * NEW: Get stat stage for UI display
     */
    public int getStatStage(String stat) {
        if (stat.equalsIgnoreCase("attack")) return atkStage;
        if (stat.equalsIgnoreCase("defense")) return defStage;
        if (stat.equalsIgnoreCase("speed")) return spdStage;
        return 0;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public int getLevel() { return level; }
    
    public int getXP() { return xp; }
    public int getXpToNextLevel() { return xpToNextLevel; }
    
    public int getBaseHP() { return baseHP; } 
    public int getBaseAttack() { return baseAttack; }
    public int getBaseDefense() { return baseDefense; }
    public int getBaseSpeed() { return baseSpeed; }

    public List<Ability> getAbilities() { return abilities; }
    public int getCurrentHP() { return currentHP; }
    
    public void setCurrentHP(int hp) {
        this.currentHP = Math.max(0, Math.min(hp, getMaxHP())); 
    }
    
    public boolean isFainted() {
        return currentHP <= 0;
    }

    /**
     * IMPROVED: Add status with duplicate checking
     */
    public void addStatus(String name, int duration, String type) {
        // Remove existing status of same type
        activeStatuses.removeIf(s -> s.name.equals(name));
        
        // Don't add if already has a "STOP" type status
        if (type.equals("STOP")) {
            for (StatusEffect s : activeStatuses) {
                if (s.type.equals("STOP")) {
                    return; // Can't be frozen AND asleep
                }
            }
        }
        
        activeStatuses.add(new StatusEffect(name, duration, type));
    }
    
    public List<StatusEffect> getActiveStatuses() {
        return activeStatuses;
    }
    
    /**
     * IMPROVED: Better status effect checking
     */
    public boolean canMove(StringBuilder log) {
        for (StatusEffect s : activeStatuses) {
            if (s.name.equals("Sleep")) {
                log.append(name).append(" is fast asleep!");
                return false;
            }
            
            if (s.name.equals("Freeze")) {
                if (Math.random() > 0.2) {
                    log.append(name).append(" is frozen solid!");
                    return false;
                } else {
                    log.append(name).append(" thawed out! ");
                    activeStatuses.removeIf(st -> st.name.equals("Freeze"));
                }
            }
            
            if (s.name.equals("Paralysis") && Math.random() < 0.25) { 
                log.append(name).append(" is fully paralyzed!");
                return false;
            }
        }
        return true;
    }
    
    /**
     * IMPROVED: Better end-of-turn damage calculation
     */
    public int processEndOfTurn(StringBuilder log) {
        int damageTaken = 0;
        Iterator<StatusEffect> it = activeStatuses.iterator();
        
        while (it.hasNext()) {
            StatusEffect s = it.next();
            
            // Burn/Poison damage (6.25% of max HP)
            if (s.type.equals("DMG")) {
                int dmg = (int)(getMaxHP() * 0.0625); 
                if (dmg < 1) dmg = 1;
                damageTaken += dmg;
                log.append(name).append(" is hurt by ").append(s.name).append("! ");
            }
            
            s.duration--;
            if (s.duration <= 0) {
                log.append(name).append("'s ").append(s.name).append(" wore off. ");
                it.remove();
            }
        }
        
        this.currentHP -= damageTaken;
        if (this.currentHP < 0) this.currentHP = 0;
        
        return damageTaken;
    }

    public Image getImage() {
        if (image == null) {
            String filename = name.toLowerCase() + ".png";
            image = AssetLoader.loadImagePreferResource("/javamon/assets/" + filename, filename);
        }
        return image;
    }
    
    @Override
    public String toString() {
        return String.format("%s Lvl %d (HP: %d/%d)", name, level, currentHP, getMaxHP());
    }
}