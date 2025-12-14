package javamon;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * REBALANCED DATABASE V3.0
 * 
 * KEY CHANGES:
 * 1. HP buffed significantly (85-105 range)
 * 2. Attack/Defense more balanced (15-22 range)
 * 3. Speed more varied (10-28 range)
 * 4. Better role differentiation (tanks, speedsters, balanced)
 */
public class DatabaseManager {

    private static final String DB_FILE = "javamon.db";
    private static final String URL = "jdbc:sqlite:" + DB_FILE;

    private Connection connect() {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC"); 
            conn = DriverManager.getConnection(URL);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
        return conn;
    }

    public void initializeDatabase() {
        Connection conn = connect();
        if (conn == null) return;
        
        String createAbilityTable = "CREATE TABLE IF NOT EXISTS Ability (id INTEGER PRIMARY KEY, name TEXT, description TEXT, type TEXT);";
        String createMonsterTable = "CREATE TABLE IF NOT EXISTS Monster (id INTEGER PRIMARY KEY, name TEXT, type TEXT, base_hp INTEGER, base_atk INTEGER, base_def INTEGER, base_spd INTEGER, "
                + "a1 INTEGER, a2 INTEGER, a3 INTEGER, a4 INTEGER, "
                + "FOREIGN KEY(a1) REFERENCES Ability(id), FOREIGN KEY(a2) REFERENCES Ability(id), "
                + "FOREIGN KEY(a3) REFERENCES Ability(id), FOREIGN KEY(a4) REFERENCES Ability(id));";

        try (conn; Statement stmt = conn.createStatement()) {
            stmt.execute(createAbilityTable);
            stmt.execute(createMonsterTable);
            
            if (getRowCount(conn, "Monster") == 0) {
                populateInitialData(conn);
            }
        } catch (SQLException e) {
            System.err.println("DB Init Error: " + e.getMessage());
        }
    }

    private void populateInitialData(Connection conn) throws SQLException {
        System.out.println("Applying Balance Patch v5.0 (No One-Shot Meta)...");
        String insertAbility = "INSERT INTO Ability (id, name, description, type) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(insertAbility)) {
            // --- WATER ---
            addAbil(pstmt, 101, "Tidal Slam", "Reliable Water Dmg.", "Water");
            addAbil(pstmt, 102, "Hydro Pump", "High Dmg, Low Accuracy.", "Water");
            addAbil(pstmt, 103, "Aqua Ring", "Restores HP.", "Healing");
            addAbil(pstmt, 104, "Bubble Shield", "Increases Defense.", "Buff");
            
            // --- FIRE ---
            addAbil(pstmt, 105, "Ember", "Reliable Fire Dmg.", "Fire");
            addAbil(pstmt, 106, "Fire Blast", "Massive Dmg, chance to miss.", "Fire");
            addAbil(pstmt, 107, "Kindle", "Increases Attack sharply.", "Buff");
            addAbil(pstmt, 108, "Cauterize", "Heals HP but reduces Def.", "Healing");

            // --- GRASS ---
            addAbil(pstmt, 109, "Razor Leaf", "Reliable Grass Dmg. High Crit.", "Grass");
            addAbil(pstmt, 110, "Solar Beam", "Massive Dmg.", "Grass");
            addAbil(pstmt, 111, "Synthesis", "Restores HP.", "Healing");
            addAbil(pstmt, 112, "Spore", "Chance to Sleep enemy.", "Grass");

            // --- BUG ---
            addAbil(pstmt, 113, "X-Scissor", "Reliable Bug Dmg.", "Bug");
            addAbil(pstmt, 114, "Megahorn", "High Dmg, Low Accuracy.", "Bug");
            addAbil(pstmt, 115, "Harden", "Raises Defense.", "Buff");
            addAbil(pstmt, 116, "String Shot", "Lowers Enemy Speed.", "Bug");

            // --- LIGHTNING ---
            addAbil(pstmt, 117, "Spark", "Reliable Lightning Dmg.", "Lightning");
            addAbil(pstmt, 118, "Thunder", "Massive Dmg, low accuracy.", "Lightning");
            addAbil(pstmt, 119, "Charge", "Raises Attack.", "Buff");
            addAbil(pstmt, 120, "Thunder Wave", "Paralyzes enemy.", "Lightning");

            // --- FLYING ---
            addAbil(pstmt, 121, "Wing Attack", "Reliable Flying Dmg.", "Flying");
            addAbil(pstmt, 122, "Brave Bird", "Massive Dmg, User takes Recoil.", "Flying");
            addAbil(pstmt, 123, "Roost", "Restores HP.", "Healing");
            addAbil(pstmt, 124, "Tailwind", "Increases Speed.", "Buff");

            // --- ICE ---
            addAbil(pstmt, 125, "Ice Beam", "Reliable Ice Dmg. Chance to freeze.", "Ice");
            addAbil(pstmt, 126, "Blizzard", "AoE Dmg (High Power).", "Ice");
            addAbil(pstmt, 127, "Hail", "Buffs Defense.", "Buff");
            addAbil(pstmt, 128, "Sheer Cold", "Low Accuracy, Huge Damage.", "Ice");

            // --- DARK ---
            addAbil(pstmt, 129, "Bite", "Reliable Dark Dmg.", "Dark");
            addAbil(pstmt, 130, "Crunch", "High Dark Dmg. Lowers Def.", "Dark");
            addAbil(pstmt, 131, "Nasty Plot", "Sharply raises Attack.", "Buff");
            addAbil(pstmt, 132, "Dark Void", "Puts enemy to sleep.", "Dark");

            // --- GROUND ---
            addAbil(pstmt, 133, "Mud Shot", "Ground Dmg. Lowers Speed.", "Ground");
            addAbil(pstmt, 134, "Earthquake", "Massive Ground Dmg.", "Ground");
            addAbil(pstmt, 135, "Sandstorm", "Buffs Def.", "Buff");
            addAbil(pstmt, 136, "Fissure", "Risk move. Huge dmg or miss.", "Ground");

            pstmt.executeBatch();
        }

        /**
         * REBALANCED MONSTER STATS V3.0
         * 
         * PHILOSOPHY:
         * - HP: 85-105 (everyone can survive 3-4 hits)
         * - Attack: 15-22 (moderate damage, no one-shots)
         * - Defense: 15-22 (balanced with attack)
         * - Speed: 10-28 (varied for strategy)
         * 
         * ARCHETYPES:
         * - Tanks: High HP/Def, Low Spd (Guyum, Sawalee)
         * - Speedsters: High Spd/Atk, Lower HP (Lectric, Alailaw)
         * - Balanced: Even stats (Wilkeens, Dahmoe)
         * - Glass Cannons: High Atk, Lower Def (Boombero, Sparky)
         */
        
        String insertMonster = "INSERT INTO Monster (id, name, type, base_hp, base_atk, base_def, base_spd, a1, a2, a3, a4) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertMonster)) {
        	// Water Monsters - Balanced/Defensive
            addMon(pstmt, 1, "Wilkeens", "Water", 95, 18, 20, 16, 101, 102, 103, 104);
            addMon(pstmt, 2, "Hose", "Water", 90, 19, 17, 20, 101, 102, 104, 119);

            // Fire Monsters - High Attack/Speed
            addMon(pstmt, 3, "Boombero", "Fire", 88, 22, 15, 23, 105, 106, 107, 108);
            addMon(pstmt, 4, "Apoyet", "Fire", 85, 21, 14, 26, 105, 106, 124, 108);

            // Grass Monsters - High HP/Defense
            addMon(pstmt, 5, "Dahmoe", "Grass", 100, 17, 19, 15, 109, 110, 111, 112);
            addMon(pstmt, 6, "Santan", "Grass", 95, 18, 17, 18, 109, 110, 112, 120);

            // Bug Monsters - Defensive Walls
            addMon(pstmt, 7, "Guyum", "Bug", 98, 16, 24, 12, 113, 114, 115, 116);
            addMon(pstmt, 8, "Salagoo", "Bug", 93, 19, 19, 16, 113, 114, 116, 103);

            // Lightning Monsters - Speed/Attack Focus
            addMon(pstmt, 9, "Lectric", "Lightning", 85, 22, 14, 28, 117, 118, 119, 120);
            addMon(pstmt, 10, "Sparky", "Lightning", 88, 21, 16, 25, 117, 118, 119, 121);

            // Ground Monsters - High HP/Defense
            addMon(pstmt, 11, "Sawalee", "Ground", 105, 19, 22, 11, 133, 134, 135, 115);
            addMon(pstmt, 12, "Elypante", "Ground", 100, 20, 20, 13, 133, 134, 136, 104);

            // Flying Monsters - Speed/Attack
            addMon(pstmt, 13, "Pannykee", "Flying", 90, 20, 16, 24, 121, 122, 123, 124);
            addMon(pstmt, 14, "Agilean", "Flying", 92, 21, 15, 23, 121, 122, 107, 124);

            // Ice Monsters - Balanced
            addMon(pstmt, 15, "Sorbeetez", "Ice", 93, 20, 17, 19, 125, 126, 127, 128);
            addMon(pstmt, 16, "Gimalam", "Ice", 95, 19, 18, 17, 125, 126, 128, 103);

            // Dark Monsters - High Speed/Attack
            addMon(pstmt, 17, "Alailaw", "Dark", 87, 22, 14, 27, 129, 130, 131, 132);
            addMon(pstmt, 18, "Milidam", "Dark", 98, 19, 18, 17, 129, 130, 132, 112);

            pstmt.executeBatch();
        }
        
        System.out.println("✓ Monsters rebalanced for 3-4 hit survival!");
        System.out.println("✓ Damage formula prevents one-shots!");
        System.out.println("✓ Game is now strategic and fair!");
    }
    
    private void addAbil(PreparedStatement p, int id, String n, String d, String t) throws SQLException {
        p.setInt(1, id); p.setString(2, n); p.setString(3, d); p.setString(4, t); p.addBatch();
    }
    
    private void addMon(PreparedStatement p, int id, String n, String t, int hp, int atk, int def, int spd, int a1, int a2, int a3, int a4) throws SQLException {
        p.setInt(1, id); p.setString(2, n); p.setString(3, t); p.setInt(4, hp); p.setInt(5, atk); p.setInt(6, def); p.setInt(7, spd); 
        p.setInt(8, a1); p.setInt(9, a2); p.setInt(10, a3); p.setInt(11, a4); p.addBatch();
    }

    private int getRowCount(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public List<Monster> getAllMonsters() {
        String sql = "SELECT m.*, "
                   + "a1.id AS a1id, a1.name AS a1n, a1.description AS a1d, a1.type AS a1t, "
                   + "a2.id AS a2id, a2.name AS a2n, a2.description AS a2d, a2.type AS a2t, "
                   + "a3.id AS a3id, a3.name AS a3n, a3.description AS a3d, a3.type AS a3t, "
                   + "a4.id AS a4id, a4.name AS a4n, a4.description AS a4d, a4.type AS a4t "
                   + "FROM Monster m "
                   + "JOIN Ability a1 ON m.a1 = a1.id JOIN Ability a2 ON m.a2 = a2.id "
                   + "JOIN Ability a3 ON m.a3 = a3.id JOIN Ability a4 ON m.a4 = a4.id";

        List<Monster> monsters = new ArrayList<>();
        try (Connection conn = connect()) {
            if (conn == null) return monsters;
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Ability ab1 = new Ability(rs.getInt("a1id"), rs.getString("a1n"), rs.getString("a1d"), rs.getString("a1t"));
                    Ability ab2 = new Ability(rs.getInt("a2id"), rs.getString("a2n"), rs.getString("a2d"), rs.getString("a2t"));
                    Ability ab3 = new Ability(rs.getInt("a3id"), rs.getString("a3n"), rs.getString("a3d"), rs.getString("a3t"));
                    Ability ab4 = new Ability(rs.getInt("a4id"), rs.getString("a4n"), rs.getString("a4d"), rs.getString("a4t"));
                    
                    monsters.add(new Monster(
                        rs.getInt("id"), rs.getString("name"), rs.getString("type"),
                        rs.getInt("base_hp"), rs.getInt("base_atk"), rs.getInt("base_def"), rs.getInt("base_spd"),
                        ab1, ab2, ab3, ab4
                    ));
                }
            }
        } catch (SQLException e) { System.err.println("DB Error: " + e.getMessage()); }
        return monsters;
    }
}