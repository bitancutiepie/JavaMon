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
import javax.imageio.ImageIO;

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
    
    // Size for the hover pop-up image
    private static final int HOVER_IMAGE_SIZE = 200; 
    
    // --- CONFIRMATION DIALOG CONSTANTS ---
    private static final int CONFIRM_DIALOG_SIZE = 400;
    private static final int CONFIRM_IMAGE_SIZE = 150;

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
    private static final int PLAYER_NAME_X = 221; 
    private static final int PLAYER_NAME_1_Y = 229; // Original 209 + 20
    private static final int PLAYER_NAME_2_Y = 365; // Original 345 + 20
    private static final int PLAYER_NAME_3_Y = 494; // Original 474 + 20
    
    private static final int PLAYER_TYPE_1_Y = 250; // Original 230 + 20
    private static final int PLAYER_TYPE_2_Y = 387; // Original 367 + 20
    private static final int PLAYER_TYPE_3_Y = 515; // Original 495 + 20

    private static final int ENEMY_NAME_X = 933; 
    private static final int ENEMY_NAME_1_Y = 236; // Original 216 + 20
    private static final int ENEMY_NAME_2_Y = 358; // Original 338 + 20
    private static final int ENEMY_NAME_3_Y = 490; // Original 470 + 20

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
    private JLabel centerBox; 
    
    // New component for image pop-up hover effect
    private JWindow hoverWindow; 
    
    // New component for the custom confirmation dialog
    private JDialog confirmationDialog; 

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

        // Initialize the transparent pop-up window
        hoverWindow = new JWindow(this);
        hoverWindow.setFocusableWindowState(false);
        hoverWindow.setBackground(new Color(0, 0, 0, 0)); 
        
        // Initialize the custom confirmation dialog
        confirmationDialog = new JDialog(this, "Confirm Selection", Dialog.ModalityType.APPLICATION_MODAL);
        confirmationDialog.setSize(CONFIRM_DIALOG_SIZE, CONFIRM_DIALOG_SIZE);
        confirmationDialog.setLocationRelativeTo(this);
        confirmationDialog.setUndecorated(true); // Remove default title bar for custom look
        
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
        // 4 rows, 5 columns (20 total slots)
        centerSelectionPanel.setLayout(new GridLayout(4, 5, 5, 5)); 
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
                
                GameWindow game = new GameWindow();
                game.setVisible(true);
                
                DraftSelection.this.dispose(); 
            } else {
                // Use custom dialog instead of JOptionPane
                showCustomMessageDialog("Draft Incomplete", "Draft not complete. Pick 3 Mons First.");
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
            centerSelectionPanel.setLayout(new GridLayout(4, 5, 5, 5)); 
            
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
                monsterBtn.addMouseListener(new newMonsterHoverAdapter(monster, monsterBtn, normalIcon, hoverIcon));

                // *** CHANGE: Now shows confirmation dialog instead of selecting directly ***
                monsterBtn.addActionListener(e -> showConfirmationDialog(monster));
                
                centerSelectionPanel.add(monsterBtn);
            }
            
            // Fill remaining grid spots with empty placeholders (now transparent)
            int totalGridSlots = 4 * 5; // 4 rows * 5 columns = 20
            int placeholdersNeeded = totalGridSlots - remainingMonsters.size();
            for (int i = 0; i < placeholdersNeeded; i++) {
                JLabel placeholder = new JLabel(); 
                centerSelectionPanel.add(placeholder);
            }
        }
        
        centerSelectionPanel.revalidate();
        centerSelectionPanel.repaint();
        updateTeamSlots(); 
    }
    
    // *** NEW METHOD: Displays the custom confirmation dialog ***
    private void showConfirmationDialog(Monster monster) {
        if (!isPlayerTurn || playerTeam.size() >= MAX_SELECTION) {
            return;
        }
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(20, 20, 20)); // Dark background
        
        Color typeColor = getColorForType(monster.getType());
        panel.setBorder(BorderFactory.createLineBorder(typeColor.brighter(), 5)); // Themed border
        
        // --- 1. Header (Confirmation Text) ---
        JLabel header = new JLabel("CONFIRM PICK", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setForeground(Color.YELLOW);
        panel.add(header, BorderLayout.NORTH);

        // --- 2. Center Content (Image and Info) ---
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        
        // Monster Image
        JLabel imageLabel = new JLabel();
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        Image largeImage = createMonsterPFPIcon(monster, CONFIRM_IMAGE_SIZE);
        imageLabel.setIcon(new ImageIcon(largeImage));
        imageLabel.setBorder(BorderFactory.createLineBorder(typeColor, 3));
        center.add(Box.createVerticalStrut(15));
        center.add(imageLabel);
        
        // Name Label
        JLabel nameLabel = new JLabel(monster.getName(), SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 28));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(nameLabel);
        
        // Type Label
        JLabel typeLabel = new JLabel("(" + monster.getType() + ")", SwingConstants.CENTER);
        typeLabel.setFont(new Font("Arial", Font.ITALIC, 18));
        typeLabel.setForeground(typeColor.brighter());
        typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(typeLabel);
        
        panel.add(center, BorderLayout.CENTER);
        
        // --- 3. Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setOpaque(false);

        // Confirm Button
        JButton confirmBtn = new JButton("Confirm Pick");
        confirmBtn.setFont(new Font("Arial", Font.BOLD, 16));
        confirmBtn.setBackground(new Color(50, 200, 50)); // Green for Confirm
        confirmBtn.setForeground(Color.BLACK);
        confirmBtn.setFocusPainted(false);
        confirmBtn.addActionListener(e -> {
            confirmationDialog.dispose();
            confirmSelection(monster); // Execute the actual selection logic
        });
        
        // Cancel Button
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 16));
        cancelBtn.setBackground(new Color(200, 50, 50)); // Red for Cancel
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> confirmationDialog.dispose());
        
        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Finalize Dialog
        confirmationDialog.setContentPane(panel);
        confirmationDialog.revalidate();
        confirmationDialog.repaint();
        confirmationDialog.setVisible(true); // Show the dialog
    }

    // *** MODIFIED: Renamed the core logic handler and made it private ***
    private void confirmSelection(Monster monster) {
        if (playerTeam.size() < MAX_SELECTION && isPlayerTurn) {
            // 1. Add monster to team
            playerTeam.add(monster);
            
            // 2. Hide the hover window *immediately* after selection
            hideHoverImage(); 
            
            // 3. Update UI
            updateTeamSlots();
            
            // 4. Change turn and re-render
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
                playerMonPFPLabels[i].setIcon(new ImageIcon(createMonsterPFPIcon(m, PFP_SIZE)));
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
                enemyMonPFPLabels[i].setIcon(new ImageIcon(createMonsterPFPIcon(m, PFP_SIZE)));
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
            case "water" -> new Color(100, 150, 255); // Lighter Blue
            case "fire" -> new Color(255, 120, 100);  // Lighter Red/Orange
            case "grass" -> new Color(150, 255, 100); // Lighter Green
            case "lightning" -> new Color(255, 255, 100); // Bright Yellow
            case "bug" -> new Color(190, 200, 50); 
            case "dark" -> new Color(180, 100, 255); // Purple
            case "flying" -> new Color(150, 255, 255); // Light Cyan
            case "ice" -> Color.WHITE;
            case "ground" -> new Color(255, 180, 100); // Light Orange/Brown
            default -> Color.GRAY;
        };
    }
    
    // Updated PFP Icon method to accept size
    private BufferedImage createMonsterPFPIcon(Monster monster, int size) {
        int w = size; 
        int h = size;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Color typeColor = getColorForType(monster.getType());
        
        g2d.setColor(typeColor);
        // Draw a circle
        g2d.fillOval(0, 0, w, h); 

        // Draw the Monster Image only if it exists
        if (monster.getImage() != null) {
            Image monImage = monster.getImage().getScaledInstance(w - 10, h - 10, Image.SCALE_SMOOTH);
            int x = (w - monImage.getWidth(null)) / 2;
            int y = (h - monImage.getHeight(null)) / 2;
            g2d.drawImage(monImage, x, y, null);
        }

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        // Draw a circle border
        g2d.drawOval(0, 0, w - 1, h - 1);
        
        g2d.dispose();
        return image;
    }
    
    private BufferedImage createMonsterPFPIcon(Monster monster) {
        return createMonsterPFPIcon(monster, PFP_SIZE);
    }


    /**
     * Creates the icon used for the clickable buttons in the center panel.
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
        
        // 1. Draw Background
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, w, h);

        // 2. Draw Monster Icon (PFP/Image)
        int iconSize = 50; 
        int arc = 15;
        int iconX = (w - iconSize) / 2;
        int iconY = (h - iconSize) / 2 - 15; 

        // Draw the colored type square behind the image
        g2d.setColor(typeColor.darker());
        g2d.fillRoundRect(iconX, iconY, iconSize, iconSize, arc, arc);

        // Draw the actual Monster Image only if it exists
        if (monster.getImage() != null) {
            Image monImage = monster.getImage().getScaledInstance(iconSize - 10, iconSize - 10, Image.SCALE_SMOOTH);
            int imgX = iconX + 5;
            int imgY = iconY + 5;
            g2d.drawImage(monImage, imgX, imgY, null);
        }

        // Draw border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(iconX, iconY, iconSize, iconSize, arc, arc);
        
        // 3. Draw Text
         g2d.setColor(Color.WHITE);
         
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
    
    // --- MONSTER HOVER WINDOW UTILITY ---
    
    private void showHoverImage(Monster monster, int x, int y) {
        if (monster.getImage() == null) {
            // Only show pop-up for monsters with an actual image
            return; 
        }
        
        // Panel for the content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        
        // Create a large, scaled image icon
        Image scaledImage = monster.getImage().getScaledInstance(HOVER_IMAGE_SIZE, HOVER_IMAGE_SIZE, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
        
        // Add a subtle border or background to the pop-up
        imageLabel.setBorder(BorderFactory.createLineBorder(getColorForType(monster.getType()), 5));
        
        contentPanel.add(imageLabel, BorderLayout.CENTER);
        
        // Set the content and size of the JWindow
        hoverWindow.setContentPane(contentPanel);
        hoverWindow.setSize(HOVER_IMAGE_SIZE, HOVER_IMAGE_SIZE);
        
        // Position the window slightly below and to the right of the mouse cursor
        hoverWindow.setLocation(x + 10, y + 10);
        hoverWindow.setVisible(true);
    }
    
    private void hideHoverImage() {
        hoverWindow.setVisible(false);
    }
    
    // --- Custom Message Dialog (Replaces JOptionPane) ---
    private void showCustomMessageDialog(String title, String message) {
        JDialog messageDialog = new JDialog(this, title, Dialog.ModalityType.APPLICATION_MODAL);
        messageDialog.setSize(300, 150);
        messageDialog.setLocationRelativeTo(this);
        messageDialog.setUndecorated(true);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(20, 20, 20));
        panel.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 4));
        
        JLabel msgLabel = new JLabel("  " + message, SwingConstants.CENTER);
        msgLabel.setForeground(Color.WHITE);
        msgLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JButton okBtn = new JButton("OK");
        okBtn.setBackground(new Color(50, 100, 200)); // Blue OK button
        okBtn.setForeground(Color.WHITE);
        okBtn.addActionListener(e -> messageDialog.dispose());
        
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(okBtn);
        
        panel.add(msgLabel, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        messageDialog.setContentPane(panel);
        messageDialog.setVisible(true);
    }

    // --- HELPER CLASSES AND METHODS ---
    
    public static class Monster {
        private final String name;
        private final String type;
        private final Image image; 
        
        public Monster(String name, String type, String imagePath) {
            this.name = name;
            this.type = type;
            // The image loading utility now gracefully handles null or missing files.
            this.image = (imagePath != null) ? loadImagePreferResource(imagePath, LOCAL_DIR + new File(imagePath).getName()) : null;
        }
        
        public String getName() { return name; }
        public String getType() { return type; }
        public Image getImage() { return image; } 
    }
    
    /**
     * MouseAdapter implementation to handle hover effects and the image pop-up.
     */
    private class newMonsterHoverAdapter extends MouseAdapter {
        private final Monster monster;
        private final JButton button;
        private final ImageIcon normalIcon;
        private final ImageIcon hoverIcon;

        public newMonsterHoverAdapter(Monster monster, JButton button, ImageIcon normalIcon, ImageIcon hoverIcon) {
            this.monster = monster;
            this.button = button;
            this.normalIcon = normalIcon;
            this.hoverIcon = hoverIcon;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            button.setIcon(hoverIcon);
            // Get the absolute position of the button on the screen
            Point p = button.getLocationOnScreen();
            showHoverImage(monster, p.x + button.getWidth(), p.y - (HOVER_IMAGE_SIZE / 2));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            button.setIcon(normalIcon);
            hideHoverImage();
        }
    }
    
    /**
     * Creates 18 monsters total (12 named, 6 placeholders).
     */
    private List<Monster> createPlaceholderMonsters() {
        List<Monster> monsters = new ArrayList<>();
        
        // --- 1. CORE MONSTERS (18 TOTAL) ---
        // Water
        monsters.add(new Monster("Wilkeens", "Water", "/javamon/assets/wilkeens.png")); 
        monsters.add(new Monster("Hose", "Water", "/javamon/assets/hose.png")); 
        // Fire
        monsters.add(new Monster("Boombero", "Fire", "/javamon/assets/boombero.png")); 
        monsters.add(new Monster("Apoyet", "Fire", "/javamon/assets/apoyet.png")); 
        // Grass
        monsters.add(new Monster("Dahmoe", "Grass", "/javamon/assets/dahmoe.png")); 
        monsters.add(new Monster("Santan", "Grass", "/javamon/assets/Santan.png")); 
        // Bug
        monsters.add(new Monster("Guyum", "Bug", "/javamon/assets/guyum.png")); 
        monsters.add(new Monster("Salagoo", "Bug", "/javamon/assets/salagoo.png")); 
        // Lightning
        monsters.add(new Monster("Lectric", "Lightning", "/javamon/assets/lectric.png")); 
        monsters.add(new Monster("Patricky", "Lightning", "/javamon/assets/patricky.png")); 
        // Ground
        monsters.add(new Monster("Sawalee", "Ground", "/javamon/assets/Sawalee.png")); 
        monsters.add(new Monster("Elypante", "Ground", "/javamon/assets/Elypante.png")); 
        // Flying
        monsters.add(new Monster("Pannykee", "Flying", "/javamon/assets/Pannykee.png")); 
        monsters.add(new Monster("Agilean", "Flying", "/javamon/assets/agilean.png"));  
        // Ice
        monsters.add(new Monster("Sorbeetez", "Ice", "/javamon/assets/sorbeetez.png")); 
        monsters.add(new Monster("Gimalam", "Ice", "/javamon/assets/gimalam.png"));    
        // Dark
        monsters.add(new Monster("Alailaw", "Dark", "/javamon/assets/alailaw.png")); 
        monsters.add(new Monster("Milidam", "Dark", "/javamon/assets/milidam.png")); 
        
        // Shuffle the list to randomize the pool order
        java.util.Collections.shuffle(monsters);
        
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

    private static Image loadImagePreferResource(String resourcePath, String localPath) {
        try {
            URL url = DraftSelection.class.getResource(resourcePath);
            if (url != null) {
                BufferedImage img = ImageIO.read(url);
                if (img != null) return img;
            }
        } catch (Exception ignored) {}

        try {
            File f = new File(localPath);
            if (f.exists()) {
                BufferedImage img = ImageIO.read(f);
                if (img != null) return img;
            }
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