package javamon;

import java.util.Arrays;
import java.util.List;

// Ability is in the same package, so no import needed.

public class Monster {
    private final int id;
    private final String name;
    private final String type;
    private final int baseHP;
    private final int baseAttack;
    private final int baseDefense;
    private final int baseSpeed;
    private final List<Ability> abilities;

    public Monster(int id, String name, String type, int baseHP, int baseAttack, int baseDefense, int baseSpeed, Ability ability1, Ability ability2) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.baseHP = baseHP;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.baseSpeed = baseSpeed;
        this.abilities = Arrays.asList(ability1, ability2);
    }

    // --- Getters ---

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getBaseHP() {
        return baseHP;
    }

    public int getBaseAttack() {
        return baseAttack;
    }

    public int getBaseDefense() {
        return baseDefense;
    }

    public int getBaseSpeed() {
        return baseSpeed;
    }

    public List<Ability> getAbilities() {
        return abilities;
    }

    @Override
    public String toString() {
        return String.format("%s (Type: %s, HP: %d, ATK: %d, DEF: %d, SPD: %d)", 
                             name, type, baseHP, baseAttack, baseDefense, baseSpeed);
    }
}