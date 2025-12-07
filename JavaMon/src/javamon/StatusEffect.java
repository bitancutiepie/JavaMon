package javamon;

public class StatusEffect {
    public String name;
    public int duration; // How many turns it lasts
    public String type;  // "DMG" (Burn), "STOP" (Freeze/Paralyze/Sleep), "BUFF" (Stats)

    public StatusEffect(String name, int duration, String type) {
        this.name = name;
        this.duration = duration;
        this.type = type;
    }
}