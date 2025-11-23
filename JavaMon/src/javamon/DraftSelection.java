package javamon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import javax.swing.Timer;

public class DraftSelection extends JFrame {

    private static final String LOCAL_DIR = "/mnt/data/"; 
    private final int MAX_SELECTION = 3;
    private final Random random = new Random();

    // --- DIMENSION AND POSITION CONSTANTS ---
    // Frame Size: 1280 x 760 
    private static final int SLOT_WIDTH = 282;
    private static final int SLOT_HEIGHT = 112;
    private static final int PFP_SIZE = 72;
    private static final int NAME_LABEL_WIDTH = 150; 
    private static final int LABEL_ALIGNMENT = SwingConstants.LEFT; 

    // Player (Ally) Slot Frame Positions (Background Image Position)
    private static final int PLAYER_SLOT_1_X = 73; 
    private static final int PLAYER_SLOT_1_Y = 207; 
    private static final int PLAYER_SLOT_2_X = 73; 
    private static final int PLAYER_SLOT_2_Y = 333;
    private static final int PLAYER_SLOT_3_X = 73; 
    private static final int PLAYER_SLOT_3_Y = 463; 

    // Enemy Slot Frame Positions (Background Image Position)
    private static final int ENEMY_SLOT_1_X = 863;
    private static final int ENEMY_SLOT_1_Y = 214; 
    private static final int ENEMY_SLOT_2_X = 863;
    private static final int ENEMY_SLOT_2_Y = 338; 
    private static final int ENEMY_SLOT_3_X = 863; 
    private static final int ENEMY_SLOT_3_Y = 468; 
    
    // --- MONSTER NAME/TYPE LABEL POSITIONS (Original Y + 20px offset) ---
    
    // Player Name X is constant: 221
    private static final int PLAYER_NAME_X = 221; 
    private static final int PLAYER_NAME_1_Y = 229; // Original 209 + 20
    private static final int PLAYER_NAME_2_Y = 365; // Original 345 + 20
    private static final int PLAYER_NAME_3_Y = 494; // Original 474 + 20
    
    // Player Type X is constant: 221
    private static final int PLAYER_TYPE_1_Y = 250; // Original 230 + 20
    private static final int PLAYER_TYPE_2_Y = 387; // Original 367 + 20
    private static final int PLAYER_TYPE_3_Y = 515; // Original 495 + 20

    // Enemy Name X is constant: 933 (shifted 15px left from 948)
    private static final int ENEMY_NAME_X = 933; 
    private static final int ENEMY_NAME_1_Y = 236; // Original 216 + 20
    private static final int ENEMY_NAME_2_Y = 358; // Original 338 + 20
    private static final int ENEMY_NAME_3_Y = 490; // Original 470 + 20

    // Enemy Type X is constant: 933 (shifted 15px left from 948)
    private static final int ENEMY_TYPE_1_Y = 257; // Original 237 + 20
    private static final int ENEMY_TYPE_2_Y = 380; // Original 360 + 20
    private static final int ENEMY_TYPE_3_Y = 509; // Original 489 + 20

    // --- PFP/ICON LABEL POSITIONS (Based on frame position) ---
    private static final int PFP_ICON_Y_OFFSET = (SLOT_HEIGHT - PFP_SIZE) / 2;
    
    private static final int PFP_ICON_X_PLAYER = PLAYER_SLOT_1_X + 15;
    private static final int PFP_ICON_1_Y_PLAYER = PLAYER_SLOT_1_Y + PFP_ICON_Y_OFFSET;
    private static final int PFP_ICON_2_Y_PLAYER = PLAYER_SLOT_2_Y + PFP_ICON_Y_OFFSET;
    private static final int PFP_ICON_3_Y_PLAYER = PLAYER_SLOT_3_Y + PFP_ICON_Y_OFFSET;

    private static final int PFP_ICON_X_ENEMY = ENEMY_SLOT_1_X + SLOT_WIDTH - PFP_SIZE - 15;
    private static final int PFP_ICON_1_Y_ENEMY = ENEMY_SLOT_1_Y + PFP_ICON_Y_OFFSET;
    private static final int PFP_ICON_2_Y_ENEMY = ENEMY_SLOT_2_Y + PFP_ICON_Y_OFFSET;
    private static final int PFP_ICON_3_Y_ENEMY = ENEMY_SLOT_3_Y + PFP_ICON_Y_OFFSET;
    // -------------------------------------------------------------

    // --- GAME STATE ---
    private List<Monster> allAvailableMonsters;
    private List<Monster> playerTeam;
    private List<Monster> enemyTeam;
    private boolean isPlayerTurn = true;
    
    // --- UI COMPONENTS ---
    private JPanel centerSelectionPanel;
    private final JLabel[] playerFrameLabels = new JLabel[MAX_SELECTION];
    private final JLabel[] enemyFrameLabels = new JLabel[MAX_SELECTION];
    private final JLabel[] playerMonNameLabels = new JLabel[MAX_SELECTION];
    private final JLabel[] enemyMonNameLabels = new JLabel[MAX_SELECTION];
    private final JLabel[] playerMonTypeLabels = new JLabel[MAX_SELECTION];
    private final JLabel[] enemyMonTypeLabels = new JLabel[MAX_SELECTION];
    private final JLabel[] playerMonPFPLabels = new JLabel[MAX_SELECTION];
    private final JLabel[] enemyMonPFPLabels = new JLabel[MAX_SELECTION];
    private JLabel statusLabel;
    private JButton battleBtn;
    private JLabel centerBox; // Made accessible to set Z-order

    // --- Custom Image Assets for Card Frames (Loaded once) ---
    private final ImageIcon playerFrameIcon;
    private final ImageIcon enemyFrameIcon;
    
    public DraftSelection() {
        setTitle("Draft Selection");
        setSize(1280, 760); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // --- 1. INITIALIZE MONSTERS & TEAMS ---
        allAvailableMonsters = createPlaceholderMonsters();
        playerTeam = new ArrayList<>();
        enemyTeam = new ArrayList<>();
        // -----------------------------------------------

        // Load card frame assets
        playerFrameIcon = tryCreateIcon("/javamon/assets/PlayerBlue.png", LOCAL_DIR + "PlayerBlue.png");
        enemyFrameIcon = tryCreateIcon("/javamon/assets/PlayerRed.png", LOCAL_DIR + "PlayerRed.png");

        Image icon = loadImagePreferResource("/javamon/assets/icon.png", LOCAL_DIR + "icon.png");
        if (icon != null) setIconImage(icon);
        setVisible(true);

        // Background panel
        JPanel bgPanel = new JPanel() {
            private final Image bg = loadImagePreferResource("/javamon/assets/DRAFTING PHASE BG.png",
                                                            LOCAL_DIR + "DRAFTING PHASE BG.png");
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw background scaled to 1280x760
                if (bg != null) {
                    g.drawImage(bg, 0, 0, 1280, 760, this);
                } else {
                    g.setColor(Color.DARK_GRAY);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        bgPanel.setLayout(null);
        setContentPane(bgPanel);
        
        // -------------------------
        // UI Elements
        // -------------------------
        
        // Top Bar
        ImageIcon topBarIcon = tryCreateIcon("/javamon/assets/Top Bar.png", LOCAL_DIR + "Top Bar.png");
        JLabel topBarLabel = iconLabelFor(topBarIcon, 1319, 56, "Top Bar");
        topBarLabel.setBounds(-40, -6, 1319, 56);
        bgPanel.add(topBarLabel);

        // DraftingPhase title
        ImageIcon draftingTitleIcon = tryCreateIcon("/javamon/assets/DraftingPhase.png", LOCAL_DIR + "DraftingPhase.png");
        JLabel draftingTitle = iconLabelFor(draftingTitleIcon, 371, 44, "DraftingPhase");
        draftingTitle.setBounds(420, 50, 371, 44);
        bgPanel.add(draftingTitle);

        // Center Box Background (The frame image)
        ImageIcon centerIcon = tryCreateIcon("/javamon/assets/CenterBox.png", LOCAL_DIR + "CenterBox.png");
        centerBox = iconLabelFor(centerIcon, 498, 498, "CenterBox");
        centerBox.setBounds(353, 142, 498, 498);
        bgPanel.add(centerBox);
        
        // Status Label (above center box)
        statusLabel = new JLabel("Your Turn to Choose!", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 20));
        statusLabel.setForeground(Color.YELLOW);
        statusLabel.setBounds(400, 100, 400, 30);
        bgPanel.add(statusLabel);
        
        // --- Center Selection Panel (The available pool - buttons sit here) ---
        centerSelectionPanel = new JPanel();
        centerSelectionPanel.setBounds(372, 161, 460, 460); // Area inside the center box
        centerSelectionPanel.setOpaque(false);
        centerSelectionPanel.setLayout(new GridLayout(4, 4, 5, 5)); 
        bgPanel.add(centerSelectionPanel);

        // --- Z-ORDER CORRECTION: Ensure buttons are on top of the central box UI image ---
        // Z-order 0 is the top-most layer (drawn last).
        bgPanel.setComponentZOrder(statusLabel, 0); 
        bgPanel.setComponentZOrder(centerSelectionPanel, 1); 
        bgPanel.setComponentZOrder(centerBox, 2); 
        // ----------------------------------------------------------------------------------


        // ------------------------------------------------
        // LEFT SIDE: PLAYER'S FINAL TEAM SLOTS (Blue Frames)
        // ------------------------------------------------
        int[] playerX = {PLAYER_SLOT_1_X, PLAYER_SLOT_2_X, PLAYER_SLOT_3_X};
        int[] playerY = {PLAYER_SLOT_1_Y, PLAYER_SLOT_2_Y, PLAYER_SLOT_3_Y};
        
        // New Absolute Y coordinates
        int[] playerMonNameY = {PLAYER_NAME_1_Y, PLAYER_NAME_2_Y, PLAYER_NAME_3_Y};
        int[] playerMonTypeY = {PLAYER_TYPE_1_Y, PLAYER_TYPE_2_Y, PLAYER_TYPE_3_Y};
        int[] playerPfpY = {PFP_ICON_1_Y_PLAYER, PFP_ICON_2_Y_PLAYER, PFP_ICON_3_Y_PLAYER};
        
        for (int i = 0; i < MAX_SELECTION; i++) {
            // 1. Frame Image (Background image)
            JLabel pBlueSlot = iconLabelFor(playerFrameIcon, SLOT_WIDTH, SLOT_HEIGHT, "Player Slot " + (i + 1));
            pBlueSlot.setBounds(playerX[i], playerY[i], SLOT_WIDTH, SLOT_HEIGHT);
            playerFrameLabels[i] = pBlueSlot;
            bgPanel.add(pBlueSlot);
            
            // 2. Monster Icon/PFP Label
            playerMonPFPLabels[i] = createPFPSlotLabel(PFP_ICON_X_PLAYER, playerPfpY[i], bgPanel);
            
            // 3. Name Label (Using NEW absolute X/Y, LEFT-aligned)
            playerMonNameLabels[i] = createTextLabel(PLAYER_NAME_X, playerMonNameY[i], NAME_LABEL_WIDTH, 30, LABEL_ALIGNMENT, bgPanel);
            playerMonNameLabels[i].setFont(new Font("Arial", Font.BOLD, 18));
            
            // 4. Type Label (Using NEW absolute X/Y, LEFT-aligned)
            playerMonTypeLabels[i] = createTextLabel(PLAYER_NAME_X, playerMonTypeY[i], NAME_LABEL_WIDTH, 20, LABEL_ALIGNMENT, bgPanel);
            playerMonTypeLabels[i].setFont(new Font("Arial", Font.PLAIN, 14));
            playerMonTypeLabels[i].setForeground(Color.CYAN); 
        }

        // ------------------------------------------------
        // RIGHT SIDE: ENEMY'S FINAL TEAM SLOTS (Red Frames)
        // ------------------------------------------------
        int[] enemyX = {ENEMY_SLOT_1_X, ENEMY_SLOT_2_X, ENEMY_SLOT_3_X};
        int[] enemyY = {ENEMY_SLOT_1_Y, ENEMY_SLOT_2_Y, ENEMY_SLOT_3_Y};

        // New Absolute Y coordinates
        int[] enemyMonNameY = {ENEMY_NAME_1_Y, ENEMY_NAME_2_Y, ENEMY_NAME_3_Y};
        int[] enemyMonTypeY = {ENEMY_TYPE_1_Y, ENEMY_TYPE_2_Y, ENEMY_TYPE_3_Y};
        int[] enemyPfpY = {PFP_ICON_1_Y_ENEMY, PFP_ICON_2_Y_ENEMY, PFP_ICON_3_Y_ENEMY};

        for (int i = 0; i < MAX_SELECTION; i++) {
            // 1. Frame Image (Background image)
            JLabel pRedSlot = iconLabelFor(enemyFrameIcon, SLOT_WIDTH, SLOT_HEIGHT, "Enemy Slot " + (i + 1));
            pRedSlot.setBounds(enemyX[i], enemyY[i], SLOT_WIDTH, SLOT_HEIGHT);
            enemyFrameLabels[i] = pRedSlot;
            bgPanel.add(pRedSlot);
            
            // 2. Monster Icon/PFP Label
            enemyMonPFPLabels[i] = createPFPSlotLabel(PFP_ICON_X_ENEMY, enemyPfpY[i], bgPanel);

            // 3. Name Label (Using NEW absolute X/Y, LEFT-aligned)
            enemyMonNameLabels[i] = createTextLabel(ENEMY_NAME_X, enemyMonNameY[i], NAME_LABEL_WIDTH, 30, LABEL_ALIGNMENT, bgPanel);
            enemyMonNameLabels[i].setFont(new Font("Arial", Font.BOLD, 18));

            // 4. Type Label (Using NEW absolute X/Y, LEFT-aligned)
            enemyMonTypeLabels[i] = createTextLabel(ENEMY_NAME_X, enemyMonTypeY[i], NAME_LABEL_WIDTH, 20, LABEL_ALIGNMENT, bgPanel);
            enemyMonTypeLabels[i].setFont(new Font("Arial", Font.PLAIN, 14));
            enemyMonTypeLabels[i].setForeground(Color.ORANGE); 
        }
        
        // -------------------------
        // Battle Button
        // -------------------------
        ImageIcon battleIcon = tryCreateIcon("/javamon/assets/BattleButton.png", LOCAL_DIR + "BattleButton.png");
        battleBtn = iconButtonFor(battleIcon, 208, 71, "BATTLE");
        battleBtn.setBounds(500, 640, 208, 71); 
        battleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Start disabled
        battleBtn.setEnabled(false); 
        
        battleBtn.addActionListener(e -> {
            if (playerTeam.size() == MAX_SELECTION && enemyTeam.size() == MAX_SELECTION) {
                System.out.println("Starting BATTLE!");
                
                // FIX: OPEN GameWindow and close DraftSelection
                // Assuming GameWindow class exists in the javamon package
                GameWindow gw = new GameWindow(); 
                gw.setVisible(true);
                DraftSelection.this.dispose(); 
                
            } else {
                JOptionPane.showMessageDialog(DraftSelection.this, "Draft not complete. Pick 3 Mons First.", "Draft Incomplete", JOptionPane.WARNING_MESSAGE);
            }
        });
        bgPanel.add(battleBtn);

        // Back Button
        JButton backButton = new JButton("Back");
        backButton.setBounds(20, 20, 120, 40);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setFocusPainted(false);
        backButton.setOpaque(false);
        backButton.addActionListener(e -> dispose());
        bgPanel.add(backButton);
        
        // --- START DRAFT ---
        renderAvailableMonsters();
        updateTeamSlots();
        bgPanel.revalidate();
        bgPanel.repaint();
    }
    
    // --- CORE DRAFTING LOGIC METHODS ---
    
    private void renderAvailableMonsters() {
        centerSelectionPanel.removeAll();
        
        List<Monster> remainingMonsters = allAvailableMonsters.stream()
            .filter(m -> !playerTeam.contains(m) && !enemyTeam.contains(m))
            .collect(Collectors.toList());

        if (playerTeam.size() == MAX_SELECTION && enemyTeam.size() == MAX_SELECTION) {
             statusLabel.setText("DRAFT COMPLETE! Press BATTLE.");
             centerSelectionPanel.add(new JLabel("All monsters drafted. Click BATTLE!", SwingConstants.CENTER));
             battleBtn.setEnabled(true); 
        } else if (!isPlayerTurn && enemyTeam.size() < MAX_SELECTION) {
            statusLabel.setText("Enemy is choosing...");
            Timer delayTimer = new Timer(1500, e -> computerSelects()); 
            delayTimer.setRepeats(false);
            delayTimer.start();
            
            // Show a placeholder while the computer "thinks"
            JLabel thinkingLabel = new JLabel("Awaiting enemy choice...", SwingConstants.CENTER);
            thinkingLabel.setForeground(Color.WHITE);
            centerSelectionPanel.setLayout(new BorderLayout()); 
            centerSelectionPanel.add(thinkingLabel, BorderLayout.CENTER);
            // Restore layout manager for when it's the player's turn again
            centerSelectionPanel.setLayout(new GridLayout(4, 4, 5, 5)); 
            
        } else if (isPlayerTurn && playerTeam.size() < MAX_SELECTION) {
            // Corrected status label count to show current selection number + 1
            statusLabel.setText("Your Turn to Choose! (" + (playerTeam.size() + 1) + " of " + MAX_SELECTION + ")");
            
            // --- LOOP THROUGH ALL AVAILABLE MONSTERS ---
            for (Monster monster : remainingMonsters) {
                // Initial Icon (Normal State - dark background)
                ImageIcon normalIcon = new ImageIcon(createMonsterCardIcon(monster, false, 110, 110, false));
                // Hover Icon (Brighter State - highlighted background)
                ImageIcon hoverIcon = new ImageIcon(createMonsterCardIcon(monster, true, 110, 110, false));
                
                JButton monsterBtn = new JButton(normalIcon);
                
                monsterBtn.setToolTipText(monster.getName() + " (" + monster.getType() + ")");
                monsterBtn.setBorderPainted(false); 
                monsterBtn.setContentAreaFilled(false);
                monsterBtn.setFocusPainted(false);
                monsterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                
                // QoL Enhancement: Hover Effect for Monster Cards
                monsterBtn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        monsterBtn.setIcon(hoverIcon);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        monsterBtn.setIcon(normalIcon);
                    }
                });


                monsterBtn.addActionListener(e -> handlePlayerSelection(monster));
                centerSelectionPanel.add(monsterBtn);
            }
            
            // Fill remaining grid spots with empty placeholders (now transparent)
            int placeholdersNeeded = (4 * 4) - remainingMonsters.size();
            for (int i = 0; i < placeholdersNeeded; i++) {
                // Changed to an empty JLabel to remove the placeholder visual while keeping the grid structure
                JLabel placeholder = new JLabel(); 
                centerSelectionPanel.add(placeholder);
            }
        }
        
        centerSelectionPanel.revalidate();
        centerSelectionPanel.repaint();
        updateTeamSlots(); // Call again to refresh team slot visual QoL
    }

    private void handlePlayerSelection(Monster monster) {
        if (playerTeam.size() < MAX_SELECTION && isPlayerTurn) {
            playerTeam.add(monster);
            updateTeamSlots();
            
            isPlayerTurn = false;
            
            renderAvailableMonsters(); 
        }
    }
    
    private void computerSelects() {
        if (enemyTeam.size() < MAX_SELECTION) {
            List<Monster> remainingMonsters = allAvailableMonsters.stream()
                .filter(m -> !playerTeam.contains(m) && !enemyTeam.contains(m))
                .collect(Collectors.toList());
            
            if (!remainingMonsters.isEmpty()) {
                Monster enemyChoice = remainingMonsters.get(random.nextInt(remainingMonsters.size()));
                enemyTeam.add(enemyChoice);
                updateTeamSlots();
                System.out.println("Enemy selected: " + enemyChoice.getName());
            }
            
            isPlayerTurn = true;
            
            renderAvailableMonsters(); 
        }
    }

    private void updateTeamSlots() {
        boolean draftComplete = playerTeam.size() == MAX_SELECTION && enemyTeam.size() == MAX_SELECTION;
        
        // --- Update Player Team (Left/Blue) ---
        for (int i = 0; i < MAX_SELECTION; i++) {
            
            // 1. Clean the slot label of old listeners/visual effects
            for(MouseListener ml : playerFrameLabels[i].getMouseListeners()) {
                playerFrameLabels[i].removeMouseListener(ml);
            }
            playerFrameLabels[i].setOpaque(false); 
            playerFrameLabels[i].setBackground(null); 
            
            if (i < playerTeam.size()) {
                // Slot is filled
                Monster m = playerTeam.get(i);
                playerMonPFPLabels[i].setIcon(new ImageIcon(createMonsterPFPIcon(m)));
                playerMonNameLabels[i].setText(m.getName());
                playerMonTypeLabels[i].setText("(" + m.getType() + ")");
                playerFrameLabels[i].setText("");
            } else {
                // Slot is empty
                playerMonPFPLabels[i].setIcon(null); 
                playerMonNameLabels[i].setText(""); 
                playerMonTypeLabels[i].setText(""); 
                
                // QoL Enhancement: Indicate the next available slot with text
                if (isPlayerTurn && i == playerTeam.size()) {
                    playerFrameLabels[i].setText("NEXT PICK");
                    playerFrameLabels[i].setFont(new Font("Arial", Font.BOLD, 18));
                    playerFrameLabels[i].setForeground(Color.YELLOW);
                } else {
                    playerFrameLabels[i].setText(""); 
                }
            }
        }
        
        // --- Update Enemy Team (Right/Red) ---
        for (int i = 0; i < MAX_SELECTION; i++) {
            if (i < enemyTeam.size()) {
                Monster m = enemyTeam.get(i);
                enemyMonPFPLabels[i].setIcon(new ImageIcon(createMonsterPFPIcon(m)));
                enemyMonNameLabels[i].setText(m.getName());
                enemyMonTypeLabels[i].setText("(" + m.getType() + ")");
                enemyFrameLabels[i].setText("");
                enemyFrameLabels[i].setOpaque(false);
            } else {
                enemyMonPFPLabels[i].setIcon(null); 
                enemyMonNameLabels[i].setText(""); 
                enemyMonTypeLabels[i].setText(""); 
                enemyFrameLabels[i].setText(""); 
                enemyFrameLabels[i].setOpaque(false); 
            }
        }
        
        // --- BATTLE BUTTON LOGIC ---
        battleBtn.setEnabled(draftComplete);
        
        repaint(); 
    }
    
    // --- MONSTER CARD DRAWING LOGIC ---
    
    private Color getColorForType(String type) {
        return switch (type.toLowerCase()) {
            case "water" -> Color.BLUE;
            case "fire" -> Color.RED;
            case "grass" -> Color.GREEN;
            case "lightning" -> Color.YELLOW;
            case "bug" -> new Color(170, 180, 0); 
            case "dark" -> Color.MAGENTA;
            case "flying" -> Color.CYAN;
            case "ice" -> Color.WHITE;
            case "ground" -> Color.ORANGE;
            default -> Color.GRAY;
        };
    }
    
    private BufferedImage createMonsterPFPIcon(Monster monster) {
        int w = PFP_SIZE; 
        int h = PFP_SIZE;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Color typeColor = getColorForType(monster.getType());
        
        g2d.setColor(typeColor);
        // Draw a circle
        g2d.fillOval(0, 0, w, h); 

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        // Draw a circle border
        g2d.drawOval(0, 0, w - 1, h - 1);
        
        g2d.dispose();
        return image;
    }

    /**
     * Creates the icon used for the clickable buttons in the center panel.
     * @param monster The monster data.
     * @param isHovered True if the card should use a brighter background for the hover state.
     * @param h Height.
     * @param w Width.
     * @param isSilhouette Not currently used, but kept for signature consistency.
     * @return The rendered card image.
     */
    private BufferedImage createMonsterCardIcon(Monster monster, boolean isHovered, int h, int w, boolean isSilhouette) {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color typeColor = getColorForType(monster.getType());
        
        // Card Background Logic: Solid black for high visibility
        Color normalBg = new Color(0, 0, 0, 255); // Solid Black
        Color hoverBg = new Color(20, 20, 20, 255); // Very Dark Gray for hover

        Color bgColor = isHovered ? hoverBg : normalBg;
        
        Color iconColor = typeColor; // Icon color is always the Type color
        Color textColor = Color.WHITE; // Text is always white
        
        // 1. Draw Background
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, w, h);

        // 2. Draw Monster Icon (PFP)
        int iconSize = 50; 
        int arc = 15;
        int iconX = (w - iconSize) / 2;
        int iconY = (h - iconSize) / 2 - 15; 

        g2d.setColor(iconColor);
        g2d.fillRoundRect(iconX, iconY, iconSize, iconSize, arc, arc);

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(iconX, iconY, iconSize, iconSize, arc, arc);
        
        // 3. Draw Text
         g2d.setColor(textColor);
         
         // Name (Bigger)
         g2d.setFont(new Font("Arial", Font.BOLD, 12));
         String nameText = monster.getName();
         int nameWidth = g2d.getFontMetrics().stringWidth(nameText);
         g2d.drawString(nameText, (w - nameWidth) / 2, 85);
         
         // Type (Smaller)
         g2d.setFont(new Font("Arial", Font.PLAIN, 10));
         String typeText = "(" + monster.getType() + ")";
         int typeWidth = g2d.getFontMetrics().stringWidth(typeText);
         g2d.drawString(typeText, (w - typeWidth) / 2, 98);

        g2d.dispose();
        return image;
    }
    
    // --- HELPER CLASSES AND METHODS ---
    
    public static class Monster {
        private final String name;
        private final String type;
        public Monster(String name, String type) {
            this.name = name;
            this.type = type;
        }
        public String getName() { return name; }
        public String getType() { return type; }
    }
    
    private List<Monster> createPlaceholderMonsters() {
        List<Monster> monsters = new ArrayList<>();
        String[] types = {"Water", "Fire", "Grass", "Lightning", "Bug", "Dark", "Flying", "Ice", "Ground", "Water", "Fire", "Grass", "Lightning", "Bug", "Dark", "Flying"};
        String[] names = {"Wilkeens", "Boombero", "Sparky", "Tycoon", "Hose", "Milldam", "Pannykee", "Sawaiee", "Apolet", "Guu", "Dah", "San", "Icey", "Buggy", "Darky", "Fly"};
        
        for (int i = 0; i < 16; i++) {
            monsters.add(new Monster(names[i % names.length], types[i % types.length])); 
        }
        return monsters;
    }

    private JLabel createPFPSlotLabel(int x, int y, JPanel bgPanel) {
        JLabel pfpLabel = new JLabel();
        pfpLabel.setBounds(x, y, PFP_SIZE, PFP_SIZE);
        pfpLabel.setOpaque(false);
        bgPanel.add(pfpLabel);
        bgPanel.setComponentZOrder(pfpLabel, 0); 
        return pfpLabel;
    }
    
    private JLabel createTextLabel(int x, int y, int w, int h, int alignment, JPanel bgPanel) {
        JLabel textLabel = new JLabel("", alignment);
        textLabel.setFont(new Font("Arial", Font.BOLD, 18));
        textLabel.setForeground(Color.WHITE);
        textLabel.setBounds(x, y, w, h); 
        textLabel.setOpaque(false);
        bgPanel.add(textLabel);
        bgPanel.setComponentZOrder(textLabel, 0); 
        return textLabel;
    }
    
    // --- FILE/IMAGE LOADING UTILITIES ---
    
    private JLabel iconLabelFor(ImageIcon icon, int w, int h, String fallbackText) {
        if (icon != null) {
            Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            JLabel lbl = new JLabel(new ImageIcon(scaled));
            lbl.setOpaque(false);
            return lbl;
        } else {
            JLabel lbl = new JLabel(fallbackText);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setForeground(Color.WHITE);
            lbl.setBackground(new Color(0,0,0,140));
            lbl.setOpaque(true);
            lbl.setPreferredSize(new Dimension(w, h));
            return lbl;
        }
    }

    private JButton iconButtonFor(ImageIcon icon, int w, int h, String fallbackText) {
        JButton btn;
        if (icon != null) {
            Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            btn = new JButton(new ImageIcon(scaled));
        } else {
            btn = new JButton(fallbackText);
        }
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        return btn;
    }

    private Image loadImagePreferResource(String resourcePath, String localPath) {
        try {
            URL url = getClass().getResource(resourcePath);
            if (url != null) return new ImageIcon(url).getImage();
        } catch (Exception ignored) {}

        try {
            File f = new File(localPath);
            if (f.exists()) return new ImageIcon(f.getAbsolutePath()).getImage();
        } catch (Exception ignored) {}

        System.err.println("Image not found: " + resourcePath + " | " + localPath);
        return null;
    }

    private ImageIcon tryCreateIcon(String resourcePath, String localPath) {
        Image im = loadImagePreferResource(resourcePath, localPath);
        return im != null ? new ImageIcon(im) : null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DraftSelection().setVisible(true));
    }
}