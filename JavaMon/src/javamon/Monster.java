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
    
    // Mutable Battle State
    private int currentHP;
    private Image image;
    
    // Persistent Status Effects
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
        
        this.currentHP = baseHP;
        this.activeStatuses = new ArrayList<>();
    }

    public Monster copy() {
        return new Monster(
            this.id, this.name, this.type, this.baseHP, 
            this.baseAttack, this.baseDefense, this.baseSpeed, 
            this.abilities.get(0), this.abilities.get(1), this.abilities.get(2), this.abilities.get(3)
        );
    }

    // --- Status Logic ---
    
    public void addStatus(String name, int duration, String type) {
        // Remove existing status of the same type to refresh it
        activeStatuses.removeIf(s -> s.name.equals(name));
        activeStatuses.add(new StatusEffect(name, duration, type));
    }
    
    public List<StatusEffect> getActiveStatuses() {
        return activeStatuses;
    }
    
    // Called before attacking. Returns FALSE if monster cannot move.
    public boolean canMove(StringBuilder log) {
        for (StatusEffect s : activeStatuses) {
            if (s.name.equals("Freeze") || s.name.equals("Sleep")) {
                log.append(name).append(" is immobilized!");
                return false;
            }
            if (s.name.equals("Paralysis") && Math.random() < 0.25) { // 25% chance to fail
                log.append(name).append(" is fully paralyzed!");
                return false;
            }
        }
        return true;
    }
    
    // Called at end of turn. Returns damage taken from effects.
    public int processEndOfTurn(StringBuilder log) {
        int damageTaken = 0;
        Iterator<StatusEffect> it = activeStatuses.iterator();
        
        while (it.hasNext()) {
            StatusEffect s = it.next();
            
            // Damage Effects (Burn/Poison)
            if (s.type.equals("DMG")) {
                int dmg = (int)(baseHP * 0.0625); // 1/16th HP
                if(dmg < 1) dmg = 1;
                damageTaken += dmg;
                log.append(name).append(" is hurt by ").append(s.name).append("! ");
            }
            
            // Decrement Duration
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
    

    // --- Getters (Restored) ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public int getBaseHP() { return baseHP; }
    
    // *** THESE WERE MISSING ***
    public int getBaseAttack() { return baseAttack; }
    public int getBaseDefense() { return baseDefense; }
    public int getBaseSpeed() { return baseSpeed; }
    // **************************

    public List<Ability> getAbilities() { return abilities; }
    public int getCurrentHP() { return currentHP; }
    
    public void setCurrentHP(int hp) {
        this.currentHP = Math.max(0, Math.min(hp, baseHP)); 
    }
    
    public boolean isFainted() {
        return currentHP <= 0;
    }

    public Image getImage() {
        if (image == null) {
            String filename = name.toLowerCase() + ".png";
            String resourcePath = "/javamon/assets/" + filename;
            image = AssetLoader.loadImagePreferResource(resourcePath, filename);
        }
        return image;
    }

    @Override
    public String toString() {
        return String.format("%s (HP: %d/%d)", name, currentHP, baseHP);
    }
}