package javamon;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

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
    private int xpToNextLevel; // PRIVATE FIELD

    private int currentHP;
    private Image image;
    private List<StatusEffect> activeStatuses;

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
            this.xpToNextLevel = 100 * this.level; 
            leveledUp = true;
            this.currentHP = getMaxHP();
        }
        return leveledUp;
    }

    public int getMaxHP() { 
        return baseHP + (level * 15); 
    }
    
    public int getAttack() { 
        return baseAttack + (level * 3); 
    }
    
    public int getDefense() { 
        return baseDefense + (level * 2); 
    }
    
    public int getSpeed() { 
        return baseSpeed + (level * 2); 
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public int getLevel() { return level; }
    
    // --- NEW PUBLIC GETTERS FOR XP BAR FIX ---
    public int getXP() { return xp; }
    public int getXpToNextLevel() { return xpToNextLevel; }
    // ------------------------------------------
    
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

    public void addStatus(String name, int duration, String type) {
        activeStatuses.removeIf(s -> s.name.equals(name));
        activeStatuses.add(new StatusEffect(name, duration, type));
    }
    
    public List<StatusEffect> getActiveStatuses() {
        return activeStatuses;
    }
    
    public boolean canMove(StringBuilder log) {
        for (StatusEffect s : activeStatuses) {
            if (s.name.equals("Freeze") || s.name.equals("Sleep")) {
                log.append(name).append(" is immobilized!");
                return false;
            }
            if (s.name.equals("Paralysis") && Math.random() < 0.25) { 
                log.append(name).append(" is paralyzed!");
                return false;
            }
        }
        return true;
    }
    
    public int processEndOfTurn(StringBuilder log) {
        int damageTaken = 0;
        Iterator<StatusEffect> it = activeStatuses.iterator();
        
        while (it.hasNext()) {
            StatusEffect s = it.next();
            
            if (s.type.equals("DMG")) {
                int dmg = (int)(getMaxHP() * 0.0625); 
                if(dmg < 1) dmg = 1;
                damageTaken += dmg;
                log.append(name).append(" hurt by ").append(s.name).append("! ");
            }
            
            s.duration--;
            if (s.duration <= 0) {
                log.append(name).append("'s ").append(s.name).append(" wore off. ");
                it.remove();
            }
        }
        
        this.currentHP -= damageTaken;
        if(this.currentHP < 0) this.currentHP = 0;
        
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