package javamon;

public class Ability {
    private final int id;
    private final String name;
    private final String description;
    private final String type; // e.g., "Water", "Healing", "Buff"

    public Ability(int id, String name, String description, String type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
    }

    // --- Getters ---

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s)", name, type);
    }
}