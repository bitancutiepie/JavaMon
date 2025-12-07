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
        centerSelectionPanel.setLayout(new GridLayout(0, 5, 0, 0)); 
        
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
        battleBtn.setBorderPainted(false); battleBtn.setContentAreaFilled(false); battleBtn.setFocusPainted(false);
        battleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        battleBtn.setEnabled(false); 
        
        battleBtn.addActionListener(e -> {
            if (playerTeam.size() == MAX_SELECTION) {
                List<Monster> battlePlayerTeam = new ArrayList<>();
                for(Monster m : playerTeam) battlePlayerTeam.add(m.copy());
                List<Monster> battleEnemyTeam = new ArrayList<>();
                for(Monster m : enemyTeam) battleEnemyTeam.add(m.copy());

                new GameWindow(battlePlayerTeam, battleEnemyTeam, allAvailableMonsters, selectedTrainerClass).setVisible(true);
                this.dispose(); 
            }
        });
        bgPanel.add(battleBtn);

        JButton backButton = new JButton("Back");
        backButton.setBounds(20, 20, 120, 40);
        backButton.addActionListener(e -> { new TrainerSelection().setVisible(true); dispose(); });
        bgPanel.add(backButton);
        
        renderAvailableMonsters();
        updateTeamSlots();
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
                ImageIcon icon = new ImageIcon(createMonsterCardIcon(m, false, 92, 90)); 
                ImageIcon hover = new ImageIcon(createMonsterCardIcon(m, true, 92, 90));
                JButton btn = new JButton(icon);
                btn.setBorderPainted(false); 
                btn.setContentAreaFilled(false); 
                btn.setFocusPainted(false);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                // FIXED: Remove all margins and insets to eliminate gaps
                btn.setMargin(new Insets(0, 0, 0, 0));
                btn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                btn.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { btn.setIcon(hover); showHoverImage(m, btn); }
                    public void mouseExited(MouseEvent e) { btn.setIcon(icon); hoverWindow.setVisible(false); }
                });
                btn.addActionListener(e -> showConfirmationDialog(m));
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
        dialog.setSize(400, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(20, 20, 20)); 
        Color typeColor = getColorForType(m.getType());
        panel.setBorder(BorderFactory.createLineBorder(typeColor, 4)); 
        
        JLabel header = new JLabel("CONFIRM PICK", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setForeground(Color.YELLOW);
        header.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        panel.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        
        JLabel imageLabel = new JLabel();
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        BufferedImage pfp = createMonsterPFPIcon(m, 150); 
        imageLabel.setIcon(new ImageIcon(pfp));
        center.add(imageLabel);
        
        center.add(Box.createVerticalStrut(20));
        
        JLabel nameLabel = new JLabel(m.getName(), SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 28));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(nameLabel);
        
        JLabel typeLabel = new JLabel("(" + m.getType() + ")", SwingConstants.CENTER);
        typeLabel.setFont(new Font("Arial", Font.ITALIC, 18));
        typeLabel.setForeground(typeColor);
        typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(typeLabel);
        
        panel.add(center, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setOpaque(false);

        JButton confirmBtn = new JButton("CONFIRM");
        confirmBtn.setFont(new Font("Arial", Font.BOLD, 16));
        confirmBtn.setBackground(new Color(50, 200, 50)); 
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFocusPainted(false);
        
        JButton cancelBtn = new JButton("CANCEL");
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 16));
        cancelBtn.setBackground(new Color(200, 50, 50)); 
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        
        confirmBtn.addActionListener(e -> {
            dialog.dispose();
            playerTeam.add(m);
            hoverWindow.setVisible(false);
            renderAvailableMonsters();
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setContentPane(panel);
        dialog.setVisible(true);
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
        g.setColor(getColorForType(m.getType())); g.fillOval(0, 0, size, size);
        if(m.getImage() != null) g.drawImage(m.getImage().getScaledInstance(size-10, size-10, Image.SCALE_SMOOTH), 5, 5, null);
        g.dispose(); return img;
    }

    private BufferedImage createMonsterCardIcon(Monster m, boolean hover, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(hover ? new Color(60,60,60) : Color.BLACK); g.fillRect(0,0,w,h);
        
        // Debug: Print the type to console to see what's being passed
        String type = m.getType();
        System.out.println("Monster: " + m.getName() + " | Type: '" + type + "'");
        
        g.setColor(getColorForType(type)); g.fillRect(5,5,w-10,h-30);
        if(m.getImage() != null) g.drawImage(m.getImage().getScaledInstance(w-20, h-40, Image.SCALE_SMOOTH), 10, 10, null);
        g.setColor(Color.WHITE); g.drawString(m.getName(), 10, h-10);
        g.dispose(); return img;
    }
}