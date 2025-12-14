package javamon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.net.URL; 
import java.util.prefs.Preferences; 
import javax.sound.sampled.*; 
import java.io.IOException; 

public class DraftSelection extends JFrame {

    private static final String LOCAL_DIR = "/mnt/data/"; 
    private final int MAX_SELECTION = 3;
    
    // --- UI CONSTANTS ---
    private static final int SLOT_WIDTH = 282;
    private static final int SLOT_HEIGHT = 112;
    private static final int PFP_SIZE = 72;
    private static final int NAME_LABEL_WIDTH = 150; 
    private static final int HOVER_IMAGE_SIZE = 200; 
    private static final int CONFIRM_DIALOG_SIZE = 400;

    // --- POSITIONS ---
    private static final int PLAYER_SLOT_1_X = 73, PLAYER_SLOT_1_Y = 207; 
    private static final int PLAYER_SLOT_2_X = 73, PLAYER_SLOT_2_Y = 333;
    private static final int PLAYER_SLOT_3_X = 73, PLAYER_SLOT_3_Y = 463; 
    
    private static final int PLAYER_NAME_X = 221; 
    private static final int[] PLAYER_NAME_Y = {229, 365, 494};
    private static final int PLAYER_TYPE_X = 221;
    private static final int[] PLAYER_TYPE_Y = {250, 387, 515};
    
    private static final int PFP_ICON_Y_OFFSET = (SLOT_HEIGHT - PFP_SIZE) / 2;
    private static final int PFP_ICON_X_PLAYER = PLAYER_SLOT_1_X + 15;

    // --- ENEMY SLOTS ---
    private static final int ENEMY_SLOT_1_X = 863, ENEMY_SLOT_1_Y = 214; 
    private static final int ENEMY_SLOT_2_X = 863, ENEMY_SLOT_2_Y = 338; 
    private static final int ENEMY_SLOT_3_X = 863, ENEMY_SLOT_3_Y = 468; 
    
    private static final int ENEMY_NAME_X = 933; 
    private static final int[] ENEMY_NAME_Y = {236, 358, 490};
    private static final int ENEMY_TYPE_X = 933;
    private static final int[] ENEMY_TYPE_Y = {257, 380, 509};

    private static final int PFP_ICON_X_ENEMY = ENEMY_SLOT_1_X + SLOT_WIDTH - PFP_SIZE - 15;

    // --- STATE ---
    private List<Monster> allAvailableMonsters; 
    private List<Monster> playerTeam;
    private List<Monster> enemyTeam;
    private String selectedTrainerClass;
    
    // Added: SoundManager instance
    private final SoundManager soundManager = SoundManager.getInstance();
    
    // --- COMPONENTS ---
    private JPanel centerSelectionPanel;
    private JScrollPane scrollPane;
    
    private final JLabel[] playerFrameLabels = new JLabel[MAX_SELECTION];
    private final JLabel[] playerMonNameLabels = new JLabel[MAX_SELECTION];
    private final JLabel[] playerMonTypeLabels = new JLabel[MAX_SELECTION];
    private final JLabel[] playerMonPFPLabels = new JLabel[MAX_SELECTION];

    private final JLabel[] enemyFrameLabels = new JLabel[MAX_SELECTION];
    private final JLabel[] enemyMonNameLabels = new JLabel[MAX_SELECTION];
    private final JLabel[] enemyMonTypeLabels = new JLabel[MAX_SELECTION];
    private final JLabel[] enemyMonPFPLabels = new JLabel[MAX_SELECTION];
    
    private JLabel statusLabel;
    private JButton battleBtn;
    private JWindow hoverWindow; 
    private JDialog confirmationDialog; 

    private ImageIcon playerFrameIcon;
    private ImageIcon enemyFrameIcon;
    
    public DraftSelection(String trainerClass) {
        this.selectedTrainerClass = trainerClass;
        initUI();
    }
    
    public DraftSelection() {
        this.selectedTrainerClass = "ELEMENTALIST";
        initUI();
    }

    private void initUI() {
        setTitle("Draft Selection - " + selectedTrainerClass);
        setSize(1280, 760); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Music: Ensure menu music is playing
        soundManager.playMenuMusic();
        
        // 1. Database
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.initializeDatabase(); 
        allAvailableMonsters = dbManager.getAllMonsters();
        if (allAvailableMonsters == null) allAvailableMonsters = new ArrayList<>();

        playerTeam = new ArrayList<>();
        enemyTeam = new ArrayList<>();
        generateGauntletEnemyTeam();

        hoverWindow = new JWindow(this);
        hoverWindow.setFocusableWindowState(false);
        hoverWindow.setBackground(new Color(0, 0, 0, 0)); 
        
        confirmationDialog = new JDialog(this, "Confirm Selection", Dialog.ModalityType.APPLICATION_MODAL);
        confirmationDialog.setSize(CONFIRM_DIALOG_SIZE, CONFIRM_DIALOG_SIZE);
        confirmationDialog.setLocationRelativeTo(this);
        confirmationDialog.setUndecorated(true);
        
        playerFrameIcon = AssetLoader.loadIcon("/javamon/assets/PlayerBlue.png", "PlayerBlue.png");
        enemyFrameIcon = AssetLoader.loadIcon("/javamon/assets/PlayerRed.png", "PlayerRed.png");
        Image icon = AssetLoader.loadImage("/javamon/assets/icon.png", "icon.png");
        if (icon != null) setIconImage(icon);
        setVisible(true);

        JPanel bgPanel = new JPanel() {
            private final Image bg = AssetLoader.loadImage("/javamon/assets/DRAFTING PHASE BG.png", "DRAFTING PHASE BG.png");
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bg != null) g.drawImage(bg, 0, 0, 1280, 760, this);
                else { g.setColor(Color.DARK_GRAY); g.fillRect(0, 0, getWidth(), getHeight()); }
            }
        };
        bgPanel.setLayout(null);
        setContentPane(bgPanel);
        
        // Assets
        addIconLabel(bgPanel, "/javamon/assets/Top Bar.png", -40, -6, 1319, 56);
        addIconLabel(bgPanel, "/javamon/assets/DraftingPhase.png", 420, 50, 371, 44);
        addIconLabel(bgPanel, "/javamon/assets/CenterBox.png", 353, 142, 498, 498);
        
        statusLabel = new JLabel("Class: " + selectedTrainerClass + " | Choose 3 Monsters!", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 20));
        statusLabel.setForeground(Color.YELLOW);
        statusLabel.setBounds(300, 100, 680, 30);
        bgPanel.add(statusLabel);
        
        // --- TIGHT GRID PANEL ---
        centerSelectionPanel = new JPanel();
        centerSelectionPanel.setOpaque(false);
        // FIX: ZERO GAPS for seamless look
        centerSelectionPanel.setLayout(new GridLayout(0, 5, 5, 5)); // 5px gaps for better spacing
        
        // Even though we aim to fit without scrolling, JScrollPane is kept for safety
        scrollPane = new JScrollPane(centerSelectionPanel);
        scrollPane.setBounds(372, 161, 460, 460);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        bgPanel.add(scrollPane);
        bgPanel.setComponentZOrder(statusLabel, 0); 
        bgPanel.setComponentZOrder(scrollPane, 1); 

        // --- PLAYER SLOTS ---
        int[] pY = {PLAYER_SLOT_1_Y, PLAYER_SLOT_2_Y, PLAYER_SLOT_3_Y};
        for (int i = 0; i < MAX_SELECTION; i++) {
            JLabel slot = new JLabel(playerFrameIcon);
            slot.setBounds(PLAYER_SLOT_1_X, pY[i], SLOT_WIDTH, SLOT_HEIGHT);
            playerFrameLabels[i] = slot;
            bgPanel.add(slot);
            
            playerMonPFPLabels[i] = createLabel(bgPanel, PFP_ICON_X_PLAYER, pY[i] + PFP_ICON_Y_OFFSET, PFP_SIZE, PFP_SIZE);
            playerMonNameLabels[i] = createTextLabel(bgPanel, PLAYER_NAME_X, PLAYER_NAME_Y[i], NAME_LABEL_WIDTH, 30, 18, Color.WHITE);
            playerMonTypeLabels[i] = createTextLabel(bgPanel, PLAYER_TYPE_X, PLAYER_TYPE_Y[i], NAME_LABEL_WIDTH, 20, 14, Color.CYAN);
        }

        // --- ENEMY SLOTS ---
        int[] eY = {ENEMY_SLOT_1_Y, ENEMY_SLOT_2_Y, ENEMY_SLOT_3_Y};
        for (int i = 0; i < MAX_SELECTION; i++) {
            JLabel slot = new JLabel(enemyFrameIcon);
            slot.setBounds(ENEMY_SLOT_1_X, eY[i], SLOT_WIDTH, SLOT_HEIGHT);
            enemyFrameLabels[i] = slot;
            bgPanel.add(slot);
            
            enemyMonPFPLabels[i] = createLabel(bgPanel, PFP_ICON_X_ENEMY, eY[i] + PFP_ICON_Y_OFFSET, PFP_SIZE, PFP_SIZE);
            enemyMonNameLabels[i] = createTextLabel(bgPanel, ENEMY_NAME_X, ENEMY_NAME_Y[i], NAME_LABEL_WIDTH, 30, 18, Color.WHITE);
            enemyMonTypeLabels[i] = createTextLabel(bgPanel, ENEMY_TYPE_X, ENEMY_TYPE_Y[i], NAME_LABEL_WIDTH, 20, 14, Color.ORANGE);
        }

        // Battle Button
        ImageIcon battleIcon = AssetLoader.loadIcon("/javamon/assets/BattleButton.png", "BattleButton.png");
        battleBtn = new JButton(battleIcon);
        battleBtn.setBounds(500, 640, 208, 71); 
        battleBtn.setBorderPainted(false); battleBtn.setContentAreaFilled(false); 
        battleBtn.setFocusPainted(false);
        battleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        battleBtn.setEnabled(false); 
        
        // FIX: Call the robust sound synchronization method
        battleBtn.addActionListener(e -> {
            if (playerTeam.size() == MAX_SELECTION) {
                playAndDelayTransition("/javamon/assets/battlebtnsound.wav");
            }
        });
        bgPanel.add(battleBtn);

        JButton backButton = new JButton("Back");
        backButton.setBounds(20, 20, 120, 40);
        backButton.addActionListener(e -> { 
            playSoundEffect("/javamon/assets/ButtonsFx.wav"); 
            new TrainerSelection().setVisible(true); 
            dispose(); 
        });
        bgPanel.add(backButton);
        
        renderAvailableMonsters();
        updateTeamSlots();
    }
    
    // --- New dedicated method for battle sound and window transition ---
    private void playAndDelayTransition(String soundPath) {
        try {
            URL url = getClass().getResource(soundPath);
            if (url == null) {
                System.err.println("Battle sound file not found. Proceeding immediately.");
                initiateBattleTransition();
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);

            // Set volume based on preferences
            Preferences prefs = Preferences.userNodeForPackage(DraftSelection.class);
            int savedVolume = prefs.getInt("volume", 50);
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float range = gain.getMaximum() - gain.getMinimum();
                float gainVal = (range * (savedVolume / 100f)) + gain.getMinimum();
                gain.setValue(gainVal);
            }
            
            // Disable button during transition
            battleBtn.setEnabled(false); 
            
            // --- Synchronization Key: Wait for the sound to START before continuing ---
            clip.addLineListener(new LineListener() {
                private boolean started = false;
                @Override
                public void update(LineEvent event) {
                    if (event.getType() == LineEvent.Type.START && !started) {
                        started = true;
                        // Sound has started playing on the audio thread.
                        SwingUtilities.invokeLater(() -> {
                            // Use a short delay here to ensure the user hears the initial *click* of the sound
                            Timer t = new Timer(200, evt -> initiateBattleTransition());
                            t.setRepeats(false);
                            t.start();
                        });
                    }
                    if (event.getType() == LineEvent.Type.STOP) {
                        // Clean up the clip when it finishes
                        clip.close();
                    }
                }
            });

            clip.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            System.err.println("Error playing battle sound: " + ex.getMessage() + ". Proceeding immediately.");
            initiateBattleTransition();
        }
    }

    // --- New Method to Handle Transition ---
    private void initiateBattleTransition() {
        // This is where the switch actually happens, guaranteed to be after the sound started
        soundManager.stopMenuMusic(); // Only stop the menu music
        
        List<Monster> battlePlayerTeam = new ArrayList<>();
        for(Monster m : playerTeam) battlePlayerTeam.add(m.copy());
        List<Monster> battleEnemyTeam = new ArrayList<>();
        for(Monster m : enemyTeam) battleEnemyTeam.add(m.copy());

        new GameWindow(battlePlayerTeam, battleEnemyTeam, allAvailableMonsters, selectedTrainerClass).setVisible(true);
        this.dispose(); 
    }
    
    
    private void generateGauntletEnemyTeam() {
        if(allAvailableMonsters.isEmpty()) return;
        List<Monster> pool = new ArrayList<>(allAvailableMonsters);
        Collections.shuffle(pool);
        for (int i = 0; i < MAX_SELECTION && i < pool.size(); i++) {
            enemyTeam.add(pool.get(i)); 
        }
    }

    private void renderAvailableMonsters() {
        centerSelectionPanel.removeAll();
        List<Monster> remaining = allAvailableMonsters.stream()
            .filter(m -> !playerTeam.contains(m)) 
            .collect(Collectors.toList());

        if (playerTeam.size() == MAX_SELECTION) {
             statusLabel.setText("TEAM READY! Press BATTLE.");
             centerSelectionPanel.setLayout(new BorderLayout());
             JLabel doneLbl = new JLabel("Team Complete.", SwingConstants.CENTER);
             doneLbl.setForeground(Color.WHITE); doneLbl.setFont(new Font("Arial", Font.BOLD, 24));
             centerSelectionPanel.add(doneLbl, BorderLayout.CENTER);
             battleBtn.setEnabled(true); 
        } else {
            // No gaps for tight fit
            centerSelectionPanel.setLayout(new GridLayout(0, 5, 0, 0)); 
            statusLabel.setText("Choose Member " + (playerTeam.size() + 1) + " of " + MAX_SELECTION);
            
            for (Monster m : remaining) {
                // Precise Size: 460px width / 5 cols = 92px. Setting 92x90 for snug fit.
            	// Better proportions: 85x95 for taller cards
            	ImageIcon icon = new ImageIcon(createMonsterCardIcon(m, false, 85, 95));
            	ImageIcon hover = new ImageIcon(createMonsterCardIcon(m, true, 85, 95));
                JButton btn = new JButton(icon);
                btn.setBorderPainted(false); 
                btn.setContentAreaFilled(false); 
                btn.setFocusPainted(false);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                // FIXED: Remove all margins and insets to eliminate gaps
                btn.setMargin(new Insets(0, 0, 0, 0));
                btn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                btn.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        playSoundEffect("/javamon/assets/ButtonsFx.wav"); // <--- APPLIED SOUND EFFECT to selection buttons
                        showConfirmationDialog(m); 
                    }
                    public void mouseEntered(MouseEvent e) { btn.setIcon(hover); showHoverImage(m, btn); }
                    public void mouseExited(MouseEvent e) { btn.setIcon(icon); hoverWindow.setVisible(false); }
                });
                centerSelectionPanel.add(btn);
            }
        }
        centerSelectionPanel.revalidate(); centerSelectionPanel.repaint(); updateTeamSlots();
    }
    
    private void updateTeamSlots() {
        for (int i = 0; i < MAX_SELECTION; i++) {
            if (i < playerTeam.size()) {
                Monster m = playerTeam.get(i);
                playerMonPFPLabels[i].setIcon(new ImageIcon(createMonsterPFPIcon(m, PFP_SIZE)));
                playerMonNameLabels[i].setText(m.getName());
                playerMonTypeLabels[i].setText("(" + m.getType() + ")");
                playerFrameLabels[i].setText("");
            } else {
                playerMonPFPLabels[i].setIcon(null);
                playerMonNameLabels[i].setText("");
                playerMonTypeLabels[i].setText("");
                playerFrameLabels[i].setText(i == playerTeam.size() ? "PICK" : "");
                playerFrameLabels[i].setForeground(Color.CYAN);
                playerFrameLabels[i].setFont(new Font("Arial", Font.BOLD, 18));
                playerFrameLabels[i].setHorizontalTextPosition(SwingConstants.CENTER);
                playerFrameLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            }
        }
        for (int i = 0; i < MAX_SELECTION; i++) {
            if (i < enemyTeam.size()) {
                enemyFrameLabels[i].setText("?");
                enemyFrameLabels[i].setForeground(Color.RED);
                enemyFrameLabels[i].setFont(new Font("Arial", Font.BOLD, 24));
                enemyFrameLabels[i].setHorizontalTextPosition(SwingConstants.CENTER);
                enemyFrameLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
                enemyMonPFPLabels[i].setIcon(null); 
                enemyMonNameLabels[i].setText(""); 
                enemyMonTypeLabels[i].setText(""); 
            }
        }
    }
    
    // --- CUSTOM CONFIRMATION POPUP ---
    private void showConfirmationDialog(Monster m) {
        if(playerTeam.size() >= MAX_SELECTION) return;
        
        JDialog dialog = new JDialog(this, "Confirm Selection", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        
        // Main panel with gradient background
        JPanel panel = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Dark gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(20, 20, 30),
                    0, getHeight(), new Color(40, 40, 60)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        Color typeColor = getColorForType(m.getType());
        
        // Glowing type-colored border
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(typeColor, 5),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Header with gradient
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                    0, 0, typeColor,
                    getWidth(), 0, new Color(typeColor.getRed(), typeColor.getGreen(), 
                                            typeColor.getBlue(), 150)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BorderLayout());
        
        JLabel header = new JLabel("CONFIRM SELECTION", SwingConstants.CENTER);
        header.setFont(getEmojiFont(Font.BOLD, 28));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        headerPanel.add(header, BorderLayout.CENTER);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Center content
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Monster image with enhanced circular frame
        JPanel imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int size = 180;
                int x = (getWidth() - size) / 2;
                int y = 10;
                
                // Outer glow
                for (int i = 0; i < 10; i++) {
                    int alpha = 30 - (i * 3);
                    g2d.setColor(new Color(typeColor.getRed(), typeColor.getGreen(), 
                                          typeColor.getBlue(), alpha));
                    g2d.fillOval(x - i, y - i, size + (i * 2), size + (i * 2));
                }
                
                // Main circle with gradient
                GradientPaint circleGradient = new GradientPaint(
                    x, y, typeColor,
                    x + size, y + size, new Color(typeColor.getRed(), typeColor.getGreen(), 
                                                   typeColor.getBlue(), 200)
                );
                g2d.setPaint(circleGradient);
                g2d.fillOval(x, y, size, size);
                
                // Inner dark circle for image
                g2d.setColor(new Color(30, 30, 30));
                g2d.fillOval(x + 10, y + 10, size - 20, size - 20);
            }
        };
        imagePanel.setOpaque(false);
        imagePanel.setPreferredSize(new Dimension(200, 200));
        imagePanel.setMaximumSize(new Dimension(200, 200));
        imagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        BufferedImage pfp = createMonsterPFPIcon(m, 160);
        JLabel imageLabel = new JLabel(new ImageIcon(pfp));
        imageLabel.setBounds(20, 20, 160, 160);
        imagePanel.setLayout(null);
        imagePanel.add(imageLabel);
        
        center.add(imagePanel);
        center.add(Box.createVerticalStrut(20));
        
        // Monster name with glow effect
        JLabel nameLabel = new JLabel(m.getName().toUpperCase(), SwingConstants.CENTER);
        nameLabel.setFont(new Font("Impact", Font.BOLD, 36));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(nameLabel);
        
        center.add(Box.createVerticalStrut(10));
        
        // Type badge
        JPanel typeBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(typeColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        typeBadge.setOpaque(false);
        typeBadge.setLayout(new BorderLayout());
        typeBadge.setMaximumSize(new Dimension(200, 40));
        typeBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel typeLabel = new JLabel(m.getType().toUpperCase() + " TYPE", SwingConstants.CENTER);
        typeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        typeLabel.setForeground(Color.WHITE);
        typeLabel.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        typeBadge.add(typeLabel);
        center.add(typeBadge);
        
        center.add(Box.createVerticalStrut(20));
        
        // Stats preview
        JPanel statsPanel = new JPanel();
        statsPanel.setOpaque(false);
        statsPanel.setLayout(new GridLayout(2, 2, 15, 10));
        statsPanel.setMaximumSize(new Dimension(400, 80));
        statsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        addStatLabel(statsPanel, "HP", String.valueOf(m.getMaxHP()), new Color(255, 100, 100));
        addStatLabel(statsPanel, "ATK", String.valueOf(m.getAttack()), new Color(255, 150, 50));
        addStatLabel(statsPanel, "DEF", String.valueOf(m.getDefense()), new Color(100, 150, 255));
        addStatLabel(statsPanel, "SPD", String.valueOf(m.getSpeed()), new Color(255, 255, 100));
        
        center.add(statsPanel);
        
        panel.add(center, BorderLayout.CENTER);
        
        // Buttons with enhanced styling
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setOpaque(false);

        JButton confirmBtn = createStyledButton("✓ SELECT", new Color(50, 200, 100), 
                new Color(40, 180, 80));
		confirmBtn.setFont(getEmojiFont(Font.BOLD, 18));
		
		JButton cancelBtn = createStyledButton("✕ CANCEL", new Color(220, 60, 60), 
		               new Color(180, 40, 40));
		cancelBtn.setFont(getEmojiFont(Font.BOLD, 18));
		        
        confirmBtn.addActionListener(e -> {
            playSoundEffect("/javamon/assets/SelectMons.wav");
            dialog.dispose();
            playerTeam.add(m);
            hoverWindow.setVisible(false);
            renderAvailableMonsters();
        });
        
        cancelBtn.addActionListener(e -> {
            playSoundEffect("/javamon/assets/ButtonsFx.wav");
            dialog.dispose();
        });
        
        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }
    
 // Helper method to create emoji-compatible font
    private Font getEmojiFont(int style, int size) {
        String[] emojiSupportingFonts = {
            "Segoe UI Emoji",
            "Apple Color Emoji",
            "Noto Color Emoji",
            "Segoe UI Symbol",
            "Arial Unicode MS",
            "SansSerif"
        };
        
        for (String fontName : emojiSupportingFonts) {
            Font font = new Font(fontName, style, size);
            if (!font.getFamily().equals("Dialog")) {
                return font;
            }
        }
        
        return new Font("SansSerif", style, size);
    }
 // Helper method for stat labels
    private void addStatLabel(JPanel panel, String label, String value, Color color) {
        JPanel statBox = new JPanel(new BorderLayout(5, 0));
        statBox.setOpaque(false);
        
        JLabel lblName = new JLabel(label, SwingConstants.CENTER);
        lblName.setFont(new Font("Arial", Font.BOLD, 12));
        lblName.setForeground(new Color(180, 180, 180));
        
        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Impact", Font.BOLD, 20));
        lblValue.setForeground(color);
        
        statBox.add(lblName, BorderLayout.NORTH);
        statBox.add(lblValue, BorderLayout.CENTER);
        panel.add(statBox);
    }

    // Helper method for styled buttons
    private JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton btn = new JButton(text) {
            private boolean isHovered = false;
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color currentColor = isHovered ? hoverColor : bgColor;
                GradientPaint gradient = new GradientPaint(
                    0, 0, currentColor,
                    0, getHeight(), currentColor.darker()
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                super.paintComponent(g);
            }
        };
        
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 50));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                JButton source = (JButton)e.getSource();
                // Use reflection to set the isHovered field in the anonymous class
                try {
                    java.lang.reflect.Field field = source.getClass().getDeclaredField("isHovered");
                    field.setAccessible(true);
                    field.setBoolean(source, true);
                } catch (Exception ex) {}
                source.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                JButton source = (JButton)e.getSource();
                // Use reflection to set the isHovered field in the anonymous class
                try {
                    java.lang.reflect.Field field = source.getClass().getDeclaredField("isHovered");
                    field.setAccessible(true);
                    field.setBoolean(source, false);
                } catch (Exception ex) {}
                source.repaint();
            }
        });
        
        return btn;
    }
    
    private void showHoverImage(Monster m, Component c) {
        if(m.getImage() == null) return;
        Image scaled = m.getImage().getScaledInstance(HOVER_IMAGE_SIZE, HOVER_IMAGE_SIZE, Image.SCALE_SMOOTH);
        JLabel l = new JLabel(new ImageIcon(scaled));
        l.setBorder(BorderFactory.createLineBorder(getColorForType(m.getType()), 5));
        hoverWindow.setContentPane(l);
        hoverWindow.setSize(HOVER_IMAGE_SIZE, HOVER_IMAGE_SIZE);
        Point p = c.getLocationOnScreen();
        hoverWindow.setLocation(p.x + 100, p.y - 50);
        hoverWindow.setVisible(true);
    }
    
    private void showCustomMessageDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }
    
    // --- DRAWING HELPERS ---
    private void addIconLabel(JPanel p, String path, int x, int y, int w, int h) {
        ImageIcon ic = AssetLoader.loadIcon(path, "");
        JLabel l = new JLabel(ic); l.setBounds(x, y, w, h); p.add(l);
    }
    
    private JLabel createLabel(JPanel p, int x, int y, int w, int h) {
        JLabel l = new JLabel(); l.setBounds(x, y, w, h); p.add(l); p.setComponentZOrder(l, 0); return l;
    }
    
    private JLabel createTextLabel(JPanel p, int x, int y, int w, int h, int size, Color c) {
        JLabel l = new JLabel("", SwingConstants.LEFT); l.setFont(new Font("Arial", Font.BOLD, size));
        l.setForeground(c); l.setBounds(x, y, w, h); p.add(l); p.setComponentZOrder(l, 0); return l;
    }
    
    private Color getColorForType(String type) {
        if(type == null) return Color.GRAY;
        // Use equalsIgnoreCase to handle case variations
        String typeUpper = type.trim().toUpperCase();
        switch(typeUpper) {
            case "FIRE": return new Color(220, 50, 0);
            case "WATER": return new Color(50, 100, 255);
            case "GRASS": return new Color(50, 200, 50);
            case "LIGHTNING": 
            case "ELECTRIC": return new Color(220, 200, 0);
            case "NORMAL": return new Color(168, 167, 122);
            case "FIGHTING": return new Color(194, 46, 40);
            case "FLYING": return new Color(169, 143, 243);
            case "POISON": return new Color(163, 62, 161);
            case "GROUND": return new Color(226, 191, 101);
            case "ROCK": return new Color(182, 161, 54);
            case "BUG": return new Color(166, 185, 26);
            case "GHOST": return new Color(115, 87, 151);
            case "STEEL": return new Color(183, 183, 206);
            case "PSYCHIC": return new Color(249, 85, 135);
            case "ICE": return new Color(150, 217, 214);
            case "DRAGON": return new Color(111, 53, 252);
            case "DARK": return new Color(112, 87, 70);
            case "FAIRY": return new Color(214, 133, 173);
            default: return Color.GRAY;
        }
    }

    private BufferedImage createMonsterPFPIcon(Monster m, int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        // Just draw the monster image, no circle background
        if(m.getImage() != null) {
            Image scaledImg = m.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            g.drawImage(scaledImg, 0, 0, null);
        }
        
        g.dispose();
        return img;
    }

    private BufferedImage createMonsterCardIcon(Monster m, boolean hover, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        
        // Enable anti-aliasing for smooth graphics
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        Color typeColor = getColorForType(m.getType());
        
        // Background with gradient effect
        if (hover) {
            GradientPaint gradient = new GradientPaint(0, 0, new Color(40, 40, 40), 
                                                        0, h, new Color(60, 60, 60));
            g.setPaint(gradient);
            g.fillRoundRect(0, 0, w, h, 12, 12);
            
            // Glowing border on hover
            g.setStroke(new BasicStroke(3));
            g.setColor(typeColor);
            g.drawRoundRect(1, 1, w-3, h-3, 12, 12);
        } else {
            g.setColor(new Color(25, 25, 25));
            g.fillRoundRect(0, 0, w, h, 10, 10);
            
            // Subtle border
            g.setColor(new Color(60, 60, 60));
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(1, 1, w-3, h-3, 10, 10);
        }
        
        // Type color accent bar at top
        GradientPaint typeGradient = new GradientPaint(0, 5, typeColor, 
                                                         w, 5, new Color(typeColor.getRed(), 
                                                                         typeColor.getGreen(), 
                                                                         typeColor.getBlue(), 100));
        g.setPaint(typeGradient);
        g.fillRoundRect(5, 5, w-10, 20, 8, 8);
        
        // Monster image with padding
        if (m.getImage() != null) {
            int imgSize = Math.min(w - 20, h - 40);
            Image scaledImg = m.getImage().getScaledInstance(imgSize, imgSize, Image.SCALE_SMOOTH);
            int imgX = (w - imgSize) / 2;
            int imgY = 28;
            g.drawImage(scaledImg, imgX, imgY, null);
        }
        
        // Monster name with shadow effect
        g.setFont(new Font("Arial", Font.BOLD, 10));
        FontMetrics fm = g.getFontMetrics();
        String name = m.getName();
        int nameWidth = fm.stringWidth(name);
        int nameX = (w - nameWidth) / 2;
        int nameY = h - 8;
        
        // Text shadow
        g.setColor(new Color(0, 0, 0, 150));
        g.drawString(name, nameX + 1, nameY + 1);
        
        // Text
        g.setColor(Color.WHITE);
        g.drawString(name, nameX, nameY);
        
        g.dispose();
        return img;
    }
    
    // ========================================
    // SOUND EFFECT PLAYBACK METHOD 
    // This is the generic method used for ButtonsFx.wav and SelectMons.wav
    // ========================================
    private void playSoundEffect(String path) {
        try {
            URL url = getClass().getResource(path);
            if(url == null) {
                System.err.println("Sound file not found: " + path);
                return;
            }
            
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            
            // Get volume preference from the user settings
            Preferences prefs = Preferences.userNodeForPackage(DraftSelection.class);
            int savedVolume = prefs.getInt("volume", 50);
            
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float range = gain.getMaximum() - gain.getMinimum();
                // Set the volume based on the saved preference
                float gainVal = (range * (savedVolume / 100f)) + gain.getMinimum();
                gain.setValue(gainVal);
            }
            
            clip.start();
            
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing sound effect: " + e.getMessage());
        }
    }
}