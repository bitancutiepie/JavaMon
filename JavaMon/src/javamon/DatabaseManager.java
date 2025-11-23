package javamon;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    // The name of the SQLite database file. This file will be cloned with your app.
    private static final String DB_FILE = "javamon.db";
    private static final String URL = "jdbc:sqlite:" + DB_FILE;

    /**
     * Establishes a connection to the database.
     */
    private Connection connect() {
        Connection conn = null;
        try {
            // Register the driver (optional in modern Java, but required if Class.forName fails)
            Class.forName("org.sqlite.JDBC"); 
            conn = DriverManager.getConnection(URL);
            // System.out.println("Connection to SQLite has been established.");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite Driver not found. Ensure the jar is in your classpath: " + e.getMessage());
            // conn remains null here
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
        return conn;
    }

    /**
     * Initializes all necessary tables (Ability, Monster).
     */
    public void initializeDatabase() {
        // --- Added check to prevent NullPointerException ---
        Connection conn = connect();
        if (conn == null) {
            System.err.println("Database initialization skipped due to connection failure.");
            return; // Exit early if connection failed.
        }
        // ----------------------------------------------------
        
        String createAbilityTable = "CREATE TABLE IF NOT EXISTS Ability (\n"
                + "    id INTEGER PRIMARY KEY,\n"
                + "    name TEXT NOT NULL,\n"
                + "    description TEXT,\n"
                + "    type TEXT NOT NULL\n"
                + ");";

        String createMonsterTable = "CREATE TABLE IF NOT EXISTS Monster (\n"
                + "    id INTEGER PRIMARY KEY,\n"
                + "    name TEXT NOT NULL,\n"
                + "    type TEXT NOT NULL,\n"
                + "    base_hp INTEGER,\n"
                + "    base_atk INTEGER,\n"
                + "    base_def INTEGER,\n"
                + "    base_spd INTEGER,\n"
                + "    ability1_id INTEGER,\n"
                + "    ability2_id INTEGER,\n"
                + "    FOREIGN KEY(ability1_id) REFERENCES Ability(id),\n"
                + "    FOREIGN KEY(ability2_id) REFERENCES Ability(id)\n"
                + ");";

        // We use the conn object from above and close it in the try-with-resources block
        try (conn; 
             Statement stmt = conn.createStatement()) {
            
            // Execute table creation statements
            stmt.execute(createAbilityTable);
            stmt.execute(createMonsterTable);
            
            // Populate data if needed (Idempotent: checks if data already exists)
            populateInitialData(conn);
            
            System.out.println("Database and tables initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }

    /**
     * Populates the Ability and Monster tables with initial game data.
     */
    private void populateInitialData(Connection conn) throws SQLException {
        // --- Ability Data ---
        if (getRowCount(conn, "Ability") == 0) {
            System.out.println("Populating Ability data...");
            String insertAbility = "INSERT INTO Ability (id, name, description, type) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertAbility)) {
                // Wilkeens Abilities
                insertAbility(pstmt, 101, "Tidal Slam", "Deals moderate water damage. 20% chance Reduce enemy speed.", "Water");
                insertAbility(pstmt, 102, "Hydro Cool", "Restore 10% HP for whole team.", "Healing");
                // Hose Abilities
                insertAbility(pstmt, 103, "Aqua Pool", "Powerful attack but loses 5% hp.", "Water");
                insertAbility(pstmt, 104, "Shield Bubble", "Reduces next attack by 30%.", "Defense");
                // Boombero Abilities
                insertAbility(pstmt, 105, "Flaming Flare", "Deals fire daamge, 20% chance to Burn enemy.", "Fire");
                insertAbility(pstmt, 106, "Overheat", "Damage bonus by 30%, but reduces defense by 20%.", "Buff");
                // Apoyet Abilities
                insertAbility(pstmt, 107, "Flame Aura", "Damage per turn (DoT).", "Fire");
                insertAbility(pstmt, 108, "Blazing Blaze", "Heals 15% HP and gain 15% speed boost.", "Healing");
                // Dahmoe Abilities
                insertAbility(pstmt, 109, "Leaf Cutter", "Deals Grass Damage, 25% chance to reduce enemy defense.", "Grass");
                insertAbility(pstmt, 110, "Revitalize", "Restores 25% HP.", "Healing");
                // Santan Abilities
                insertAbility(pstmt, 111, "Vine Wrap", "Deals moderate grass damage, freeze opponent for a turn.", "Grass");
                insertAbility(pstmt, 112, "Fertilize", "Increases damage by 20%.", "Buff");
                // Guyum Abilities
                insertAbility(pstmt, 113, "Rocky Barrage", "Hits 3 Times consecutive.", "Bug");
                insertAbility(pstmt, 114, "Harden Shell", "Reduces damage by 25% for 2 turns.", "Defense");
                // Salagoo Abilities
                insertAbility(pstmt, 115, "Sticky Goo", "Deals small damage. Reflect damage back to opponent.", "Bug");
                insertAbility(pstmt, 116, "Sticky Morph", "WHEN HP <40%, 2x attack.", "Buff");
                // Lectric Abilities
                insertAbility(pstmt, 117, "Thunder Buzz", "Deals Lightning damage, 25% chance to Paralyze.", "Lightning");
                insertAbility(pstmt, 118, "Volt Guard", "Reduces incoming damage by 25% for next turn.", "Defense");
                // Sparky Abilities
                insertAbility(pstmt, 119, "Spark Build-up", "Next Lightning Attack is boosted 50%.", "Buff");
                insertAbility(pstmt, 120, "Spark Boom", "Double hit attack with moderate damage.", "Lightning");
                // Pannykee Abilities
                insertAbility(pstmt, 121, "Gust Wing", "Fast Flying attack that always hits first. Aggressor synergy.", "Flying");
                insertAbility(pstmt, 122, "Wind Barrier", "Reduces damage taken by 30% next turn. Strategist synergy.", "Defense");
                // Agilean Abilities
                insertAbility(pstmt, 123, "Aerial Slash", "High crit rate Flying attack.", "Flying");
                insertAbility(pstmt, 124, "Tailwind", "Boosts speed for 3 turns.", "Buff");
                // Sorbeetez Abilities
                insertAbility(pstmt, 125, "Frost Spike", "Deals Ice damage, 25% chance to Freeze.", "Ice");
                insertAbility(pstmt, 126, "Chill Veil", "Reduces enemy attack for 2 turns.", "Debuff");
                // Gimalam Abilities
                insertAbility(pstmt, 127, "Snow Burst", "Hits all enemies. 15% Freeze chance.", "Ice");
                insertAbility(pstmt, 128, "Ice Heal", "Restores 20% HP and slightly raises defense.", "Healing");
                // Alailaw Abilities
                insertAbility(pstmt, 129, "Shadow Claw", "High critical hit rate attack.", "Dark");
                insertAbility(pstmt, 130, "Fear Gaze", "25% chance to make the opponent skip turn.", "Debuff");
                // Milidam Abilities
                insertAbility(pstmt, 131, "Dark Mirage", "20% chance to dodge next attack automatically.", "Defense");
                insertAbility(pstmt, 132, "Soul Drain", "Deals damage and heals 25% of the dealt amount.", "Drain");
                
                pstmt.executeBatch();
            }
        }

        // --- Monster Data ---
        if (getRowCount(conn, "Monster") == 0) {
            System.out.println("Populating Monster data...");
            String insertMonster = "INSERT INTO Monster (id, name, type, base_hp, base_atk, base_def, base_spd, ability1_id, ability2_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertMonster)) {
                // Base stats are placeholders, adjust as needed. (HP, ATK, DEF, SPD)
                // Water
                insertMonster(pstmt, 1, "Wilkeens", "Water", 100, 80, 90, 60, 101, 102);
                insertMonster(pstmt, 2, "Hose", "Water", 90, 100, 70, 70, 103, 104);
                // Fire
                insertMonster(pstmt, 3, "Boombero", "Fire", 110, 95, 65, 50, 105, 106);
                insertMonster(pstmt, 4, "Apoyet", "Fire", 85, 110, 80, 80, 107, 108);
                // Grass
                insertMonster(pstmt, 5, "Dahmoe", "Grass", 95, 75, 85, 55, 109, 110);
                insertMonster(pstmt, 6, "Santan", "Grass", 80, 90, 70, 90, 111, 112);
                // Bug
                insertMonster(pstmt, 7, "Guyum", "Bug", 70, 85, 120, 40, 113, 114);
                insertMonster(pstmt, 8, "Salagoo", "Bug", 75, 105, 75, 65, 115, 116);
                // Lightning
                insertMonster(pstmt, 9, "Lectric", "Lightning", 90, 100, 70, 85, 117, 118);
                insertMonster(pstmt, 10, "Sparky", "Lightning", 80, 120, 60, 100, 119, 120);
                // Ground 
                insertMonster(pstmt, 11, "Sawalee", "Ground", 120, 80, 110, 30, 101, 102); 
                insertMonster(pstmt, 12, "Elypante", "Ground", 105, 95, 100, 45, 103, 104); 
                // Flying
                insertMonster(pstmt, 13, "Pannykee", "Flying", 70, 115, 65, 110, 121, 122);
                insertMonster(pstmt, 14, "Agilean", "Flying", 85, 90, 75, 125, 123, 124);
                // Ice
                insertMonster(pstmt, 15, "Sorbeetez", "Ice", 90, 105, 80, 70, 125, 126);
                insertMonster(pstmt, 16, "Gimalam", "Ice", 100, 90, 95, 60, 127, 128);
                // Dark
                insertMonster(pstmt, 17, "Alailaw", "Dark", 70, 130, 55, 90, 129, 130);
                insertMonster(pstmt, 18, "Milidam", "Dark", 80, 110, 70, 105, 131, 132);

                pstmt.executeBatch();
            }
        }
    }
    
    // Helper methods for insertion
    private void insertAbility(PreparedStatement pstmt, int id, String name, String desc, String type) throws SQLException {
        pstmt.setInt(1, id);
        pstmt.setString(2, name);
        pstmt.setString(3, desc);
        pstmt.setString(4, type);
        pstmt.addBatch();
    }
    
    private void insertMonster(PreparedStatement pstmt, int id, String name, String type, int hp, int atk, int def, int spd, int abil1Id, int abil2Id) throws SQLException {
        pstmt.setInt(1, id);
        pstmt.setString(2, name);
        pstmt.setString(3, type);
        pstmt.setInt(4, hp);
        pstmt.setInt(5, atk);
        pstmt.setInt(6, def);
        pstmt.setInt(7, spd);
        pstmt.setInt(8, abil1Id);
        pstmt.setInt(9, abil2Id);
        pstmt.addBatch();
    }

    private int getRowCount(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Retrieves all Monster objects with their associated Ability objects from the database.
     */
    public List<Monster> getAllMonsters() {
        // --- FIXED SQL QUERY: Removed the extra 'JOIN' ---
        String sql = "SELECT m.*, a1.id AS a1_id, a1.name AS a1_name, a1.description AS a1_desc, a1.type AS a1_type, "
                   + "a2.id AS a2_id, a2.name AS a2_name, a2.description AS a2_desc, a2.type AS a2_type "
                   + "FROM Monster m "
                   + "INNER JOIN Ability a1 ON m.ability1_id = a1.id "
                   + "INNER JOIN Ability a2 ON m.ability2_id = a2.id"; // <--- CORRECTED

        List<Monster> monsters = new ArrayList<>();

        try (Connection conn = connect()) {
            // New check to prevent NPE here too
            if (conn == null) return monsters;
            
            try (Statement stmt  = conn.createStatement();
                 ResultSet rs    = stmt.executeQuery(sql)){

                while (rs.next()) {
                    // 1. Create Ability objects
                    Ability ability1 = new Ability(
                        rs.getInt("a1_id"), rs.getString("a1_name"), rs.getString("a1_desc"), rs.getString("a1_type")
                    );
                    Ability ability2 = new Ability(
                        rs.getInt("a2_id"), rs.getString("a2_name"), rs.getString("a2_desc"), rs.getString("a2_type")
                    );
                    
                    // 2. Create Monster object using Ability objects
                    Monster monster = new Monster(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getInt("base_hp"),
                        rs.getInt("base_atk"),
                        rs.getInt("base_def"),
                        rs.getInt("base_spd"),
                        ability1,
                        ability2
                    );
                    monsters.add(monster);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving monsters: " + e.getMessage());
        }
        return monsters;
    }

    public static void main(String[] args) {
        // Used for testing/initial setup
        DatabaseManager db = new DatabaseManager();
        db.initializeDatabase();
        
        List<Monster> allMonsters = db.getAllMonsters();
        System.out.println("\n--- All Monsters from DB ---");
        for (Monster m : allMonsters) {
            System.out.println(m.getName() + " | Abilities: " + m.getAbilities().get(0).getName() + ", " + m.getAbilities().get(1).getName());
        }
    }
}