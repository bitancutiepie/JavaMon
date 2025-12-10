package javamon;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class GameWindow extends JFrame {
    
    // --- Data ---
    private List<Monster> playerTeam;
    private List<Monster> enemyTeam;
    private List<Monster> masterMonsterPool; 
    private String trainerClass; 
    
    private Monster activePlayerMon;
    private Monster activeEnemyMon;
    private int currentFloor = 1;
    
    private boolean isTurnInProgress = false;

    // --- UI Components ---
    private FadableSprite jMon1Label, jMon2Label; 
    private JLabel txtJName1, txtJName2; 
    private JLabel txtJLives1, txtJLives2; 
    private SmoothBatteryBar hpBar1, hpBar2;    
    private JTextArea txtWhat; // Changed to JTextArea for multi-line support
    private JLabel txtFloor;
    private JPanel bgPanel;
    private JLayeredPane layeredPane;
    
    // Overlays
    private JPanel fightPanel;
    private JPanel switchPanel;
    private JPanel helpPanel;
    
    // Hover Info
    private JPanel infoOverlay;
    private JLabel infoTitle;
    private JTextPane infoBody;
    
    private JLabel turnBanner;
    private JPanel flashPanel;

    // Indicators
    private TypeBadge typeBadge1, typeBadge2;
    private StatusTray statusTray1, statusTray2;

    // --- Colors & Fonts ---
    private static final Color TRANSPARENT_BLACK = new Color(0, 0, 0, 220);
    private static final Color DARK_CARD_BG = new Color(30, 30, 30, 240);
    private static final Color NEON_CYAN = new Color(0, 255, 255);
    private static final Color NEON_GREEN = new Color(50, 255, 50);
    private static final Color NEON_GOLD = new Color(255, 215, 0);
    private static final Color DAMAGE_RED = new Color(255, 50, 50);
    private static final Color EFFECTIVE_COLOR = new Color(100, 255, 100);
    private static final Color WEAK_COLOR = new Color(255, 100, 100);
    private static final Color INFO_BG = new Color(20, 20, 30, 230);
    
    private static final Font PIXEL_FONT = new Font("Monospaced", Font.BOLD, 28);
    private static final Font LOG_FONT = new Font("Monospaced", Font.BOLD, 22);
    private static final Font UI_FONT = new Font("Arial", Font.BOLD, 14); 
    private static final Font DMG_FONT = new Font("Arial", Font.BOLD, 40);
    private static final Font BANNER_FONT = new Font("Impact", Font.ITALIC, 60);
    private static final Font BADGE_FONT = new Font("Arial", Font.BOLD, 12);
    
    private static final int PLAYER_X = 95, PLAYER_Y = 239;
    private static final int ENEMY_X = 749, ENEMY_Y = 49;

    public GameWindow(List<Monster> playerTeam, List<Monster> enemyTeam, List<Monster> masterPool, String trainerClass) {
        this.playerTeam = (playerTeam != null && !playerTeam.isEmpty()) ? playerTeam : createFallbackTeam("Player");
        this.enemyTeam = (enemyTeam != null && !enemyTeam.isEmpty()) ? enemyTeam : createFallbackTeam("Enemy");
        this.masterMonsterPool = (masterPool != null) ? masterPool : this.playerTeam;
        this.trainerClass = (trainerClass != null) ? trainerClass : "ELEMENTALIST"; 

        this.activePlayerMon = this.playerTeam.get(0);
        this.activeEnemyMon = this.enemyTeam.get(0);

        setTitle("JavaMon Battle - Class: " + this.trainerClass);
        setSize(1280, 760);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, 1280, 760);
        setContentPane(layeredPane);

        bgPanel = new JPanel() {
            private Image bg = new ImageIcon(getClass().getResource("/javamon/assets/GameBG.png")).getImage();
            protected void paintComponent(Graphics g) { super.paintComponent(g); if (bg != null) g.drawImage(bg, 0, 0, getWidth(), getHeight(), this); }
        };
        bgPanel.setLayout(null);
        bgPanel.setBounds(0, 0, 1280, 760);
        layeredPane.add(bgPanel, JLayeredPane.DEFAULT_LAYER);

        initializeBaseUI();
        createOverlays(layeredPane);
        updateBattleState();
        
        SwingUtilities.invokeLater(() -> animateTurnBanner("PLAYER TURN", NEON_CYAN));
    }
    
    private List<Monster> createFallbackTeam(String prefix) {
        List<Monster> fallback = new ArrayList<>();
        Ability a = new Ability(0, "Glitch", "...", "Normal");
        fallback.add(new Monster(999, "MissingNo", "Normal", 100, 50, 50, 50, a, a, a, a));
        return fallback;
    }

    private void initializeBaseUI() {
        jMon1Label = new FadableSprite(); jMon1Label.setBounds(PLAYER_X, PLAYER_Y, 384, 345); bgPanel.add(jMon1Label);
        jMon2Label = new FadableSprite(); jMon2Label.setBounds(ENEMY_X, ENEMY_Y, 384, 345); bgPanel.add(jMon2Label);
        addMonsterHover(jMon1Label, true); addMonsterHover(jMon2Label, false);

        ImageIcon platformIcon = new ImageIcon(getClass().getResource("/javamon/assets/PLATFORM.png"));
        JLabel p1 = new JLabel(platformIcon); p1.setBounds(76, 416, 415, 106); bgPanel.add(p1);
        JLabel p2 = new JLabel(platformIcon); p2.setBounds(734, 199, 415, 106); bgPanel.add(p2);

        ImageIcon uBox = new ImageIcon(getClass().getResource("/javamon/assets/UPPER_TEXTBOX.png"));
        JLabel box1 = new JLabel(uBox); box1.setBounds(0, 21, 366, 195); bgPanel.add(box1);
        JLabel box2 = new JLabel(uBox); box2.setBounds(900, 314, 366, 195); bgPanel.add(box2);

        hpBar1 = new SmoothBatteryBar(); hpBar1.setBounds(30, 90, 300, 35); bgPanel.add(hpBar1); bgPanel.setComponentZOrder(hpBar1, 0); 
        hpBar2 = new SmoothBatteryBar(); hpBar2.setBounds(930, 380, 300, 35); bgPanel.add(hpBar2); bgPanel.setComponentZOrder(hpBar2, 0); 
        
        statusTray1 = new StatusTray(); statusTray1.setBounds(30, 130, 300, 25); bgPanel.add(statusTray1); bgPanel.setComponentZOrder(statusTray1, 0);
        statusTray2 = new StatusTray(); statusTray2.setBounds(930, 420, 300, 25); bgPanel.add(statusTray2); bgPanel.setComponentZOrder(statusTray2, 0);

        txtJName1 = createText(40, 50, 200, 30, Color.BLACK, true); bgPanel.add(txtJName1); bgPanel.setComponentZOrder(txtJName1, 0);
        typeBadge1 = new TypeBadge(); typeBadge1.setBounds(250, 55, 80, 20); bgPanel.add(typeBadge1); bgPanel.setComponentZOrder(typeBadge1, 0);
        
        txtJLives1 = createText(60, 155, 300, 30, Color.GRAY, false); bgPanel.add(txtJLives1); bgPanel.setComponentZOrder(txtJLives1, 0);

        txtJName2 = createText(960, 341, 200, 30, Color.BLACK, false); bgPanel.add(txtJName2); bgPanel.setComponentZOrder(txtJName2, 0);
        typeBadge2 = new TypeBadge(); typeBadge2.setBounds(940, 346, 80, 20); bgPanel.add(typeBadge2); bgPanel.setComponentZOrder(typeBadge2, 0);
        
        txtJLives2 = createText(930, 445, 300, 30, Color.GRAY, false); bgPanel.add(txtJLives2); bgPanel.setComponentZOrder(txtJLives2, 0);

        txtFloor = new JLabel("FLOOR 1", SwingConstants.LEFT);
        txtFloor.setBounds(25, 545, 300, 40); 
        txtFloor.setFont(new Font("Monospaced", Font.BOLD, 35));
        txtFloor.setForeground(Color.BLACK);
        bgPanel.add(txtFloor); bgPanel.setComponentZOrder(txtFloor, 0);

        ImageIcon lBox = new ImageIcon(getClass().getResource("/javamon/assets/LOWER_TEXTBOX.png"));
        JLabel bottomBox = new JLabel(lBox); bottomBox.setBounds(-10, 500, lBox.getIconWidth(), lBox.getIconHeight());
        bgPanel.add(bottomBox);

        // JTextArea for wrapping
        txtWhat = new JTextArea("What will " + activePlayerMon.getName() + " do?");
        txtWhat.setBounds(170, 615, 800, 90); 
        txtWhat.setFont(LOG_FONT); 
        txtWhat.setForeground(Color.BLACK);
        txtWhat.setOpaque(false);
        txtWhat.setEditable(false);
        txtWhat.setLineWrap(true);       
        txtWhat.setWrapStyleWord(true); 
        txtWhat.setHighlighter(null);    
        bgPanel.add(txtWhat); bgPanel.setComponentZOrder(txtWhat, 0);

        ImageIcon fightIcon = new ImageIcon(getClass().getResource("/javamon/assets/FIGHTBTN.png"));
        JButton fightButton = createAnimatedButton(fightIcon);
        fightButton.setBounds(650, 600, fightIcon.getIconWidth(), fightIcon.getIconHeight());
        fightButton.addActionListener(e -> { if (!isTurnInProgress) { updateFightPanel(); showOverlay(fightPanel); } });
        bgPanel.add(fightButton); bgPanel.setComponentZOrder(fightButton, 0);

        ImageIcon jMonIcon = new ImageIcon(getClass().getResource("/javamon/assets/JAVAMONBTN.png"));
        JButton jMonButton = createAnimatedButton(jMonIcon);
        jMonButton.setBounds(920, 600, jMonIcon.getIconWidth(), jMonIcon.getIconHeight());
        jMonButton.addActionListener(e -> { if (!isTurnInProgress) { updateSwitchPanel(); showOverlay(switchPanel); } });
        bgPanel.add(jMonButton); bgPanel.setComponentZOrder(jMonButton, 0);

        ImageIcon helpIcon = new ImageIcon(getClass().getResource("/javamon/assets/HELPBTN.png"));
        JButton helpButton = createAnimatedButton(helpIcon);
        helpButton.setBounds(780, 660, helpIcon.getIconWidth(), helpIcon.getIconHeight());
        helpButton.addActionListener(e -> showOverlay(helpPanel));
        bgPanel.add(helpButton); bgPanel.setComponentZOrder(helpButton, 0);
    }

    private void createOverlays(JLayeredPane lp) {
        fightPanel = new JPanel(new GridLayout(3, 2, 10, 10)); 
        fightPanel.setBounds(150, 420, 1000, 260); 
        fightPanel.setBackground(TRANSPARENT_BLACK);
        fightPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        fightPanel.setVisible(false);
        lp.add(fightPanel, JLayeredPane.PALETTE_LAYER);

        switchPanel = new JPanel(new GridBagLayout());
        switchPanel.setBounds(0, 0, 1280, 760);
        switchPanel.setBackground(TRANSPARENT_BLACK);
        switchPanel.setVisible(false);
        lp.add(switchPanel, JLayeredPane.MODAL_LAYER);

        helpPanel = new JPanel(new BorderLayout());
        helpPanel.setBounds(340, 100, 600, 500);
        helpPanel.setBackground(new Color(20, 20, 20));
        helpPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        JTextArea helpText = new JTextArea("--- TYPE CHART ---\nWATER > Fire, Ground\nFIRE > Grass, Bug, Ice\n..."); helpText.setFont(new Font("Monospaced", Font.PLAIN, 18)); helpText.setForeground(Color.WHITE); helpText.setBackground(new Color(20, 20, 20)); helpText.setEditable(false);
        helpPanel.add(new JScrollPane(helpText), BorderLayout.CENTER);
        JButton closeHelp = new JButton("CLOSE"); closeHelp.setBackground(Color.RED); closeHelp.setForeground(Color.WHITE); closeHelp.addActionListener(e -> helpPanel.setVisible(false)); helpPanel.add(closeHelp, BorderLayout.SOUTH);
        helpPanel.setVisible(false); lp.add(helpPanel, JLayeredPane.POPUP_LAYER);
        
        infoOverlay = new JPanel(new BorderLayout()); infoOverlay.setBounds(0, 0, 350, 180); infoOverlay.setBackground(INFO_BG); infoOverlay.setBorder(BorderFactory.createLineBorder(NEON_CYAN, 2)); infoOverlay.setVisible(false);
        infoTitle = new JLabel("", SwingConstants.CENTER); infoTitle.setFont(new Font("Arial", Font.BOLD, 18)); infoTitle.setForeground(NEON_CYAN); infoOverlay.add(infoTitle, BorderLayout.NORTH);
        infoBody = new JTextPane(); infoBody.setEditable(false); infoBody.setContentType("text/html"); infoBody.setOpaque(false); infoOverlay.add(infoBody, BorderLayout.CENTER);
        lp.add(infoOverlay, JLayeredPane.DRAG_LAYER);
        
        turnBanner = new JLabel("", SwingConstants.CENTER); turnBanner.setFont(BANNER_FONT); turnBanner.setOpaque(true); turnBanner.setBounds(-1280, 300, 1280, 100);
        lp.add(turnBanner, JLayeredPane.POPUP_LAYER);
        
        flashPanel = new JPanel(); flashPanel.setBounds(0,0,1280,760); flashPanel.setBackground(Color.WHITE); flashPanel.setVisible(false);
        lp.add(flashPanel, JLayeredPane.DRAG_LAYER);
    }

    private JButton createAnimatedButton(ImageIcon icon) {
        JButton b = new JButton(icon);
        b.setBorderPainted(false); b.setContentAreaFilled(false);
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setLocation(b.getX(), b.getY()-3); b.setBorder(BorderFactory.createLineBorder(NEON_CYAN, 2)); b.setBorderPainted(true); }
            public void mouseExited(MouseEvent e) { b.setLocation(b.getX(), b.getY()+3); b.setBorderPainted(false); }
        }); return b;
    }

    private void animateTurnBanner(String text, Color bg) {
        turnBanner.setText(text); turnBanner.setBackground(bg); turnBanner.setForeground(Color.WHITE);
        Timer timer = new Timer(5, null); final int[] x = {-1280}; final int[] pause = {0};
        timer.addActionListener(e -> {
            if (x[0] < 0) { x[0] += 40; turnBanner.setLocation(x[0], 300); } 
            else if (pause[0] < 30) { pause[0]++; } 
            else { x[0] += 40; turnBanner.setLocation(x[0], 300); if (x[0] > 1280) timer.stop(); }
        }); timer.start();
    }

    private void updateFightPanel() {
        fightPanel.removeAll();
        for (Ability a : activePlayerMon.getAbilities()) {
            double effectiveness = BattleMechanics.getTypeMultiplier(a.getType(), activeEnemyMon.getType(), trainerClass);
            String effText = (effectiveness > 1.0) ? "▲ Effective" : (effectiveness < 1.0) ? "▼ Weak" : "• Normal";
            Color textColor = (effectiveness > 1.0) ? EFFECTIVE_COLOR : (effectiveness < 1.0) ? WEAK_COLOR : Color.WHITE;
            Color borderColor = (effectiveness > 1.0) ? EFFECTIVE_COLOR : (effectiveness < 1.0) ? WEAK_COLOR : Color.GRAY;

            JButton btn = new JButton("<html><center>" + a.getName() + "<br/><small style='color:rgb(200,200,200)'>" + a.getType() + "</small><br/><small><b>" + effText + "</b></small></center></html>");
            btn.setFont(UI_FONT); btn.setBackground(new Color(40, 40, 40)); btn.setForeground(textColor); btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borderColor, 3), BorderFactory.createEmptyBorder(5, 15, 5, 15)));
            
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { showAbilityTooltip(a, btn, effectiveness); }
                public void mouseExited(MouseEvent e) { infoOverlay.setVisible(false); }
            });
            btn.addActionListener(e -> { if (!isTurnInProgress) { initiateCombatRound(a); } });
            fightPanel.add(btn);
        }
        
        // --- WITTY SECRET SKILL BUTTON ---
        Ability secret = generateSecretAbility();
        JButton secretBtn = new JButton("<html><center>★ " + secret.getName().toUpperCase() + " ★<br/><small style='color:yellow'>CLASS ULT</small></center></html>");
        secretBtn.setFont(UI_FONT); secretBtn.setBackground(new Color(60, 50, 0)); secretBtn.setForeground(NEON_GOLD);
        secretBtn.setBorder(BorderFactory.createLineBorder(NEON_GOLD, 3));
        secretBtn.setFocusPainted(false);
        secretBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { showAbilityTooltip(secret, secretBtn, 1.0); }
            public void mouseExited(MouseEvent e) { infoOverlay.setVisible(false); }
        });
        secretBtn.addActionListener(e -> { if (!isTurnInProgress) { initiateCombatRound(secret); } }); 
        fightPanel.add(secretBtn);

        JButton cancel = new JButton("CANCEL"); cancel.setFont(UI_FONT); cancel.setBackground(Color.DARK_GRAY); cancel.setForeground(Color.WHITE); cancel.addActionListener(e -> fightPanel.setVisible(false)); fightPanel.add(cancel);
        fightPanel.revalidate(); fightPanel.repaint();
    }
    
    // --- WITTY NAME GENERATOR (EXPANDED FOR EVERY COMBINATION) ---
    private Ability generateSecretAbility() {
        String name = "Secret";
        String type = activePlayerMon.getType();
        String c = trainerClass.toUpperCase();
        
        // ELEMENTALIST (Damage Focus)
        if (c.equals("ELEMENTALIST")) {
            if(type.equals("Water")) name = "Tsunami Surfer";
            else if(type.equals("Fire")) name = "Spicy Meatball";
            else if(type.equals("Grass")) name = "Salad Tosser";
            else if(type.equals("Bug")) name = "System Bug";
            else if(type.equals("Lightning")) name = "Unlimited Power";
            else if(type.equals("Ground")) name = "Dirty Deeds";
            else if(type.equals("Flying")) name = "Yeet Upwards";
            else if(type.equals("Ice")) name = "Cold Shoulder";
            else name = "Edgelord Slash";
        
        // STRATEGIST (Buffs/Stats)
        } else if (c.equals("STRATEGIST")) {
            if(type.equals("Water")) name = "Liquid Assets";
            else if(type.equals("Fire")) name = "Hot Take";
            else if(type.equals("Grass")) name = "Grassroots Move";
            else if(type.equals("Bug")) name = "Hive Mind";
            else if(type.equals("Lightning")) name = "Brainstorm";
            else if(type.equals("Ground")) name = "High Ground";
            else if(type.equals("Flying")) name = "Cloud Storage";
            else if(type.equals("Ice")) name = "Cool Head";
            else name = "Shadow Ban";
            
        // AGGRESSOR (Reckless)
        } else if (c.equals("AGGRESSOR")) {
            if(type.equals("Water")) name = "Belly Flop";
            else if(type.equals("Fire")) name = "Roast Battle";
            else if(type.equals("Grass")) name = "Lawn Mower";
            else if(type.equals("Bug")) name = "Zerg Rush";
            else if(type.equals("Lightning")) name = "Rage Quit";
            else if(type.equals("Ground")) name = "Earth Shatter";
            else if(type.equals("Flying")) name = "Kamikaze Dive";
            else if(type.equals("Ice")) name = "Brain Freeze";
            else name = "Cheap Shot";
            
        // BEASTMASTER (Primal/Nature)
        } else if (c.equals("BEASTMASTER")) {
            if(type.equals("Water")) name = "Hydrate Check";
            else if(type.equals("Fire")) name = "Campfire Song";
            else if(type.equals("Grass")) name = "Touch Grass";
            else if(type.equals("Bug")) name = "Protein Shake";
            else if(type.equals("Lightning")) name = "Energy Drink";
            else if(type.equals("Ground")) name = "Mud Bath";
            else if(type.equals("Flying")) name = "Bird Up";
            else if(type.equals("Ice")) name = "Chill Pill";
            else name = "Midnight Snack";
            
        // MYSTIC (Curses/Status)
        } else { 
            if(type.equals("Water")) name = "Soggy Socks";
            else if(type.equals("Fire")) name = "Gaslight";
            else if(type.equals("Grass")) name = "Allergy Season";
            else if(type.equals("Bug")) name = "Creepy Crawly";
            else if(type.equals("Lightning")) name = "Static Cling";
            else if(type.equals("Ground")) name = "Quicksand";
            else if(type.equals("Flying")) name = "Bad Turbulence";
            else if(type.equals("Ice")) name = "Slippery Slope";
            else name = "Demon Time";
        }
        
        return new Ability(999, name, "Class Secret Skill", type);
    }
    
    private void showAbilityTooltip(Ability a, Component anchor, double mult) {
        infoTitle.setText(a.getName() + " (" + a.getType() + ")");
        String colorHex = (mult > 1.0) ? "#00FF00" : (mult < 1.0) ? "#FF5555" : "#FFFFFF";
        String html = "<html><body style='font-family:Arial; color:white; font-size:12px; padding:5px;'>" + "<b>Desc:</b> " + a.getDescription() + "<br/><br/>" + "<b>Vs " + activeEnemyMon.getName() + ":</b> <span style='color:" + colorHex + "'>x" + mult + " Dmg</span>" + "</body></html>";
        infoBody.setText(html);
        Point loc = anchor.getLocationOnScreen(); SwingUtilities.convertPointFromScreen(loc, layeredPane);
        int y = loc.y - 190; if(y < 0) y = loc.y + 80; infoOverlay.setLocation(loc.x, y); infoOverlay.setVisible(true);
    }
    
    private void showTypeTooltip(String type, Component anchor) {
        infoTitle.setText(type);
        String weakTo = "", strongVs = "";
        if(type.equals("Water")) { strongVs = "Fire, Ground"; weakTo = "Grass, Lightning"; }
        else if(type.equals("Fire")) { strongVs = "Grass, Bug, Ice"; weakTo = "Water, Ground"; }
        else if(type.equals("Grass")) { strongVs = "Water, Ground"; weakTo = "Fire, Flying"; }
        else if(type.equals("Lightning")) { strongVs = "Water, Flying"; weakTo = "Ground"; }
        else if(type.equals("Ground")) { strongVs = "Fire, Lightning"; weakTo = "Grass, Ice"; }
        else { strongVs = "???"; weakTo = "???"; }
        
        String html = "<html><body style='font-family:Arial; color:white; font-size:12px; padding:5px;'>" + "<span style='color:#00FF00'>Strong Vs:</span> " + strongVs + "<br/>" + "<span style='color:#FF5555'>Weak To:</span> " + weakTo + "</body></html>";
        infoBody.setText(html);
        Point loc = anchor.getLocationOnScreen(); SwingUtilities.convertPointFromScreen(loc, layeredPane);
        infoOverlay.setLocation(loc.x, loc.y + 30); infoOverlay.setVisible(true);
    }
    
    private void addMonsterHover(Component c, boolean isPlayer) {
        c.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                Monster m = isPlayer ? activePlayerMon : activeEnemyMon;
                infoTitle.setText(m.getName());
                String html = "<html><body style='font-family:Arial; color:white; font-size:12px; padding:5px;'>" + "Type: <span style='color:#00FFFF'>" + m.getType() + "</span><br/>" + "ATK: " + m.getBaseAttack() + " | DEF: " + m.getBaseDefense() + "<br/>" + "SPD: " + m.getBaseSpeed() + "</body></html>";
                infoBody.setText(html);
                Point loc = c.getLocationOnScreen(); SwingUtilities.convertPointFromScreen(loc, layeredPane);
                infoOverlay.setLocation(loc.x + 50, loc.y + 50); infoOverlay.setVisible(true);
            }
            public void mouseExited(MouseEvent e) { infoOverlay.setVisible(false); }
        });
    }
    
    // --- TURN LOGIC ---
    private void initiateCombatRound(Ability playerMove) {
        fightPanel.setVisible(false); infoOverlay.setVisible(false); isTurnInProgress = true; 
        
        Ability enemyMove = activeEnemyMon.getAbilities().get((int)(Math.random() * 4)); 
        boolean playerFirst = (activePlayerMon.getBaseSpeed() >= activeEnemyMon.getBaseSpeed());
        
        if (playerFirst) {
            runTurnSequence(activePlayerMon, activeEnemyMon, playerMove, true, enemyMove);
        } else {
            runTurnSequence(activeEnemyMon, activePlayerMon, enemyMove, false, playerMove);
        }
    }
    
    private void runTurnSequence(Monster first, Monster second, Ability firstMove, boolean isFirstPlayer, Ability secondMove) {
        StringBuilder statusLog = new StringBuilder();
        if (!first.canMove(statusLog)) {
            txtWhat.setText(statusLog.toString());
            Timer t = new Timer(1500, e -> triggerSecondHalf(second, first, secondMove, !isFirstPlayer));
            t.setRepeats(false); t.start();
            return;
        }
        performAttackSequence(first, second, firstMove, isFirstPlayer, () -> {
            if (second.isFainted()) {
                if (isFirstPlayer) handleEnemyFaint(); else handlePlayerFaint();
            } else {
                triggerSecondHalf(second, first, secondMove, !isFirstPlayer);
            }
        });
    }
    
    private void triggerSecondHalf(Monster attacker, Monster defender, Ability move, boolean isPlayer) {
        if (!isPlayer) {
            animateTurnBanner("ENEMY TURN", DAMAGE_RED);
            Timer t = new Timer(1500, e -> runSecondHalfActual(attacker, defender, move, isPlayer));
            t.setRepeats(false); t.start();
        } else {
            runSecondHalfActual(attacker, defender, move, isPlayer);
        }
    }

    private void runSecondHalfActual(Monster attacker, Monster defender, Ability move, boolean isPlayer) {
        StringBuilder statusLog = new StringBuilder();
        if (!attacker.canMove(statusLog)) {
            txtWhat.setText(statusLog.toString());
            Timer t = new Timer(1500, e -> endTurn());
            t.setRepeats(false); t.start();
            return;
        }
        performAttackSequence(attacker, defender, move, isPlayer, () -> endTurn());
    }

    private void endTurn() {
        if (activePlayerMon.isFainted()) { handlePlayerFaint(); return; }
        if (activeEnemyMon.isFainted()) { handleEnemyFaint(); return; }
        
        StringBuilder log = new StringBuilder();
        int pDmg = activePlayerMon.processEndOfTurn(log);
        int eDmg = activeEnemyMon.processEndOfTurn(log);
        
        if(pDmg > 0 || eDmg > 0) {
            hpBar1.slideHealth(activePlayerMon.getCurrentHP(), activePlayerMon.getBaseHP(), null);
            hpBar2.slideHealth(activeEnemyMon.getCurrentHP(), activeEnemyMon.getBaseHP(), () -> {
                txtWhat.setText(log.toString());
                updateBattleState(); 
                Timer t = new Timer(1500, e -> resetTurn());
                t.setRepeats(false); t.start();
            });
        } else {
            resetTurn();
        }
    }
    
    private void resetTurn() {
        txtWhat.setText("What will " + activePlayerMon.getName() + " do?");
        animateTurnBanner("PLAYER TURN", NEON_CYAN);
        isTurnInProgress = false; 
    }
    
    private void performAttackSequence(Monster attacker, Monster defender, Ability move, boolean isPlayer, Runnable onComplete) {
        FadableSprite atkLabel = isPlayer ? jMon1Label : jMon2Label;
        FadableSprite defLabel = isPlayer ? jMon2Label : jMon1Label;
        SmoothBatteryBar atkBar = isPlayer ? hpBar1 : hpBar2;
        SmoothBatteryBar defBar = isPlayer ? hpBar2 : hpBar1;
        
        txtWhat.setText(attacker.getName() + " used " + move.getName() + "!");
        
        int direction = isPlayer ? 1 : -1;
        int originalX = isPlayer ? PLAYER_X : ENEMY_X;
        int originalY = isPlayer ? PLAYER_Y : ENEMY_Y;
        
        // --- ANTICIPATION: WIND UP BEFORE ATTACK ---
        Timer windUp = new Timer(15, null);
        final int[] windUpFrame = {0};
        windUp.addActionListener(e -> {
            windUpFrame[0]++;
            // Squash and stretch effect
            if(windUpFrame[0] < 8) {
                atkLabel.setLocation(originalX - (direction * 3 * windUpFrame[0]), originalY);
            }
            if(windUpFrame[0] >= 8) {
                windUp.stop();
                // --- LAUNCH FORWARD ---
                performLunge(atkLabel, defLabel, attacker, defender, move, isPlayer, 
                            originalX, originalY, direction, atkBar, defBar, onComplete);
            }
        });
        windUp.start();
    }
    
    private void performLunge(FadableSprite atkLabel, FadableSprite defLabel, Monster attacker, 
            Monster defender, Ability move, boolean isPlayer,
            int originalX, int originalY, int direction,
            SmoothBatteryBar atkBar, SmoothBatteryBar defBar, Runnable onComplete) {

        Timer lungeOut = new Timer(8, null);
        final int[] speed = {10}; // Start fast

        lungeOut.addActionListener(e -> {
            atkLabel.setLocation(atkLabel.getX() + (speed[0] * direction), atkLabel.getY() - 1); // Slight hop
            speed[0] = Math.max(3, speed[0] - 1); // Decelerate

            if (Math.abs(atkLabel.getX() - originalX) > 80) {
                lungeOut.stop();

                // --- IMPACT MOMENT ---
                int preDefHp = defender.getCurrentHP();
                int preAtkHp = attacker.getCurrentHP(); 

                String log = AbilityLogic.execute(move, attacker, defender, 
                                                isPlayer ? playerTeam : enemyTeam, 
                                                isPlayer ? trainerClass : "ENEMY");

                int damageTaken = preDefHp - defender.getCurrentHP();
                int selfHeal = attacker.getCurrentHP() - preAtkHp;

                // --- ENHANCED HIT REACTIONS ---
                if (log.contains("[CRIT]")) { 
                   flashScreen();
                   multiShake(defLabel, 18, 25); // Violent shake
                   animateHitFlash(defLabel, new Color(255, 255, 0, 200)); // Yellow crit flash
                   Timer delay = new Timer(100, evt -> {
                       animateHitFlash(defLabel, new Color(255, 0, 0, 180));
                       ((Timer)evt.getSource()).stop();
                   });
                   delay.setRepeats(false);
                   delay.start();
                } 
                else if (damageTaken > 0) { 
                   animateHitFlash(defLabel, new Color(255, 0, 0, 150)); 
                   multiShake(defLabel, 12, 15); // Moderate shake
                   // Add impact freeze frame
                   Timer freeze = new Timer(80, evt -> ((Timer)evt.getSource()).stop());
                   freeze.setRepeats(false);
                   freeze.start();
                }

                // --- ENHANCED FLOATING TEXT ---
                if (damageTaken > 0) {
                   showEnhancedDamageText("-"+damageTaken, DAMAGE_RED, defLabel, log.contains("[CRIT]"));
                } 
                else if (selfHeal > 0) {
                   showEnhancedFloatingText("+" + selfHeal, NEON_GREEN, atkLabel, true);
                }
                else if (log.contains("Rose") || log.contains("Increased")) {
                   showEnhancedFloatingText("BOOST!", NEON_CYAN, atkLabel, false);
                   animateStatBurst(atkLabel);
                }
                else if(log.contains("missed")) {
                   showEnhancedFloatingText("MISS", Color.GRAY, defLabel, false);
                } else if(damageTaken == 0) {
                   showEnhancedFloatingText("NO EFFECT", Color.WHITE, defLabel, false);
                }
                
                // --- BOUNCE BACK ---
                Timer bounceBack = new Timer(8, null);
                final int[] bounceSpeed = {3};

                bounceBack.addActionListener(ev -> {
                   atkLabel.setLocation(atkLabel.getX() - (bounceSpeed[0] * direction), 
                                       atkLabel.getY() + 1);
                   bounceSpeed[0] = Math.min(12, bounceSpeed[0] + 1); // Accelerate back
                   
                   if ((isPlayer && atkLabel.getX() <= originalX) || 
                       (!isPlayer && atkLabel.getX() >= originalX)) {
                       bounceBack.stop(); 
                       atkLabel.setLocation(originalX, originalY);
                       
                       // --- SETTLE AND BOUNCE ---
                       animateSettle(atkLabel, originalX, originalY, () -> {
                           txtJLives1.setText(activePlayerMon.getCurrentHP() + " / " + activePlayerMon.getBaseHP());
                           txtJLives2.setText(activeEnemyMon.getCurrentHP() + " / " + activeEnemyMon.getBaseHP());

                           Runnable afterHeal = () -> {
                               defBar.slideHealth(defender.getCurrentHP(), defender.getBaseHP(), () -> {
                                   txtWhat.setText(log.replace("[CRIT] ", ""));
                                   updateBattleState(); 
                                   Timer pause = new Timer(1000, evt -> { 
                                       if (onComplete != null) onComplete.run(); 
                                   });
                                   pause.setRepeats(false); 
                                   pause.start();
                               });
                           };
                           
                           if(selfHeal > 0 || damageTaken > 0) {
                               atkBar.slideHealth(attacker.getCurrentHP(), attacker.getBaseHP(), afterHeal);
                           } else {
                               afterHeal.run();
                           }
                       });
                   }
                }); 
                bounceBack.start();
            }
        }); 
        lungeOut.start();
    }
    
    // --- ANIMATION HELPER METHODS (MOVED FROM INSIDE performLunge) ---
    
    private void multiShake(JComponent c, int duration, int intensity) {
        final Point original = c.getLocation();
        final Timer shakeTimer = new Timer(25, null); 
        final int[] count = {0};
        
        shakeTimer.addActionListener(e -> {
            int offsetX = (int)(Math.sin(count[0] * 0.8) * intensity) - (intensity/2);
            int offsetY = (int)(Math.cos(count[0] * 0.6) * intensity/2);
            c.setLocation(original.x + offsetX, original.y + offsetY);
            
            if (++count[0] > duration) { 
                shakeTimer.stop(); 
                c.setLocation(original); 
            }
        }); 
        shakeTimer.start();
    }

    private void animateStatBurst(JComponent target) {
        Point p = target.getLocation();
        
        for(int i = 0; i < 8; i++) {
            JLabel particle = new JLabel("★");
            particle.setFont(new Font("Arial", Font.BOLD, 24));
            particle.setForeground(NEON_CYAN);
            particle.setBounds(p.x + 150, p.y + 150, 30, 30);
            layeredPane.add(particle, JLayeredPane.POPUP_LAYER);
            
            final double angle = (Math.PI * 2 * i) / 8;
            final int[] life = {0};
            
            Timer burst = new Timer(20, null);
            burst.addActionListener(e -> {
                life[0]++;
                int distance = life[0] * 4;
                int newX = p.x + 150 + (int)(Math.cos(angle) * distance);
                int newY = p.y + 150 + (int)(Math.sin(angle) * distance);
                particle.setLocation(newX, newY);
                
                int alpha = Math.max(0, 255 - (life[0] * 12));
                particle.setForeground(new Color(0, 255, 255, alpha));
                
                if(life[0] > 20) {
                    burst.stop();
                    layeredPane.remove(particle);
                    layeredPane.repaint();
                }
            });
            burst.start();
        }
    }

    private void animateSettle(FadableSprite sprite, int targetX, int targetY, Runnable onComplete) {
        final int[] bounceFrame = {0};
        final int[] yOffset = {0};
        
        Timer settle = new Timer(20, null);
        settle.addActionListener(e -> {
            bounceFrame[0]++;
            if(bounceFrame[0] < 5) {
                yOffset[0] = 3;
            } else if(bounceFrame[0] < 8) {
                yOffset[0] = -2;
            } else if(bounceFrame[0] < 10) {
                yOffset[0] = 1;
            } else {
                yOffset[0] = 0;
                settle.stop();
                sprite.setLocation(targetX, targetY);
                if(onComplete != null) onComplete.run();
                return;
            }
            sprite.setLocation(targetX, targetY + yOffset[0]);
        });
        settle.start();
    }

    private void showEnhancedFloatingText(String text, Color color, JComponent target, boolean isHeal) {
        JLabel floatLbl = new JLabel(text, SwingConstants.CENTER); 
        floatLbl.setFont(new Font("Arial", Font.BOLD, 32)); 
        floatLbl.setForeground(color);
        
        Point p = target.getLocation(); 
        floatLbl.setBounds(p.x + 80, p.y + 100, 200, 50); 
        layeredPane.add(floatLbl, JLayeredPane.POPUP_LAYER);
        
        final int[] alpha = {255};
        final int[] frames = {0};
        
        Timer floatTimer = new Timer(30, null);
        floatTimer.addActionListener(e -> {
            frames[0]++;
            floatLbl.setLocation(floatLbl.getX() + (isHeal ? 1 : 0), floatLbl.getY() - 2);
            
            if(frames[0] > 15) {
                alpha[0] -= 15;
                floatLbl.setForeground(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, alpha[0])));
            }
            
            if (alpha[0] <= 0) { 
                floatTimer.stop(); 
                layeredPane.remove(floatLbl); 
                layeredPane.repaint(); 
            }
        }); 
        floatTimer.start();
    }

    private void showEnhancedDamageText(String text, Color color, JComponent target, boolean isCrit) {
        JLabel floatLbl = new JLabel(text, SwingConstants.CENTER); 
        floatLbl.setFont(isCrit ? DMG_FONT.deriveFont(60f) : DMG_FONT); 
        floatLbl.setForeground(color);
        
        if(isCrit) {
            floatLbl.setBorder(BorderFactory.createLineBorder(NEON_GOLD, 2));
        }
        
        Point p = target.getLocation(); 
        floatLbl.setBounds(p.x + 50, p.y + 50, 250, 80); 
        layeredPane.add(floatLbl, JLayeredPane.POPUP_LAYER);
        
        final float[] scale = {0.5f};
        final int[] alpha = {255};
        final int[] frames = {0};
        
        Timer floatTimer = new Timer(25, null);
        floatTimer.addActionListener(e -> {
            frames[0]++;
            
            // Pop in effect (frames 0-8)
            if(frames[0] < 8) {
                scale[0] += 0.1f;
                floatLbl.setFont(floatLbl.getFont().deriveFont(DMG_FONT.getSize() * scale[0]));
            }
            // Hold (frames 8-20)
            else if(frames[0] < 20) {
                floatLbl.setLocation(floatLbl.getX(), floatLbl.getY() - 1);
            }
            // Fade out (frames 20+)
            else {
                floatLbl.setLocation(floatLbl.getX(), floatLbl.getY() - 3);
                alpha[0] -= 12;
                floatLbl.setForeground(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, alpha[0])));
            }
            
            if (alpha[0] <= 0) { 
                floatTimer.stop(); 
                layeredPane.remove(floatLbl); 
                layeredPane.repaint(); 
            }
        }); 
        floatTimer.start();
    }
    
    private void animateHitFlash(FadableSprite sprite, Color flashColor) {
        sprite.flash(flashColor);
    }
    
    private void shakeComponent(JComponent c, boolean isViolent) {
        final Point original = c.getLocation();
        final Timer shakeTimer = new Timer(30, null); final int[] count = {0};
        int intensity = isViolent ? 20 : 10;
        shakeTimer.addActionListener(e -> {
            c.setLocation(original.x + (int)(Math.random()*intensity)-(intensity/2), original.y + (int)(Math.random()*intensity)-(intensity/2));
            if (++count[0] > 10) { shakeTimer.stop(); c.setLocation(original); }
        }); shakeTimer.start();
    }
    
    private void flashScreen() {
        flashPanel.setVisible(true);
        final int[] pulses = {0};
        
        Timer t = new Timer(60, null);
        t.addActionListener(e -> {
            pulses[0]++;
            flashPanel.setVisible(!flashPanel.isVisible());
            
            if(pulses[0] >= 4) {
                flashPanel.setVisible(false);
                t.stop();
            }
        });
        t.start();
    }
    
    private void showFloatingText(String text, Color color, JComponent target) {
        JLabel floatLbl = new JLabel(text, SwingConstants.CENTER); floatLbl.setFont(DMG_FONT); floatLbl.setForeground(color);
        Point p = target.getLocation(); floatLbl.setBounds(p.x + 50, p.y + 50, 200, 50); layeredPane.add(floatLbl, JLayeredPane.POPUP_LAYER);
        Timer floatTimer = new Timer(30, null); final int[] alpha = {255};
        floatTimer.addActionListener(e -> {
            floatLbl.setLocation(floatLbl.getX(), floatLbl.getY() - 2); alpha[0] -= 5;
            if (alpha[0] <= 0) { floatTimer.stop(); layeredPane.remove(floatLbl); layeredPane.repaint(); }
        }); floatTimer.start();
    }

    private void animateSwitchIn(FadableSprite sprite, int targetX, Runnable onComplete) {
        sprite.setAlpha(0f); 
        sprite.setLocation(targetX - 100, sprite.getY()); 
        
        Timer slideIn = new Timer(15, null);
        final float[] alpha = {0f};
        
        slideIn.addActionListener(e -> {
            sprite.setLocation(sprite.getX() + 8, sprite.getY());
            alpha[0] += 0.08f;
            sprite.setAlpha(Math.min(1.0f, alpha[0]));
            
            if (sprite.getX() >= targetX) { 
                sprite.setLocation(targetX, sprite.getY()); 
                sprite.setAlpha(1.0f);
                slideIn.stop(); 
                
                // Add landing effect
                animateSettle(sprite, targetX, sprite.getY(), onComplete);
            }
        }); 
        slideIn.start();
    }
    private void animateFaint(FadableSprite sprite, Runnable onComplete) {
        Timer sink = new Timer(20, null); 
        final float[] opacity = {1.0f};
        final int[] rotation = {0};
        final Point original = sprite.getLocation();
        
        sink.addActionListener(e -> {
            sprite.setLocation(sprite.getX(), sprite.getY() + 4); 
            opacity[0] -= 0.03f; 
            sprite.setAlpha(opacity[0]);
            
            // Slight wobble as it faints
            rotation[0] += 5;
            int wobble = (int)(Math.sin(rotation[0] * 0.1) * 10);
            sprite.setLocation(sprite.getX() + wobble, sprite.getY());
            
            if (opacity[0] <= 0.05f) { 
                sink.stop(); 
                sprite.setAlpha(0f); 
                sprite.setLocation(original);
                onComplete.run(); 
            }
        }); 
        sink.start();
    }


    private void animateSwitchOut(FadableSprite sprite, Runnable onComplete) {
        Timer slideOut = new Timer(5, null);
        slideOut.addActionListener(e -> {
            sprite.setLocation(sprite.getX() - 30, sprite.getY());
            if (sprite.getX() < -400) { slideOut.stop(); onComplete.run(); }
        }); slideOut.start();
    }

    // --- GAME LOGIC ---
    private void handleEnemyFaint() {
        animateFaint(jMon2Label, () -> {
            Monster nextEnemy = null;
            for(Monster m : enemyTeam) if(!m.isFainted()) { nextEnemy = m; break; }
            if(nextEnemy != null) {
                activeEnemyMon = nextEnemy; updateBattleState(); 
                txtWhat.setText("Enemy sends out " + activeEnemyMon.getName() + "!");
                animateTurnBanner("ENEMY TURN", DAMAGE_RED);
                jMon2Label.setLocation(ENEMY_X, ENEMY_Y); 
                animateSwitchIn(jMon2Label, ENEMY_X, () -> { isTurnInProgress = false; });
            } else { handleFloorClear(); }
        });
    }

    private void handleFloorClear() {
        JOptionPane.showMessageDialog(this, "FLOOR CLEARED! Team HP Restored.");
        for(Monster m : playerTeam) m.setCurrentHP(m.getBaseHP()); 
        currentFloor++; txtFloor.setText("FLOOR " + currentFloor);
        enemyTeam.clear(); List<Monster> pool = new ArrayList<>(masterMonsterPool); java.util.Collections.shuffle(pool);
        for(int i=0; i<3 && i<pool.size(); i++) enemyTeam.add(pool.get(i).copy());
        activeEnemyMon = enemyTeam.get(0); updateBattleState();
        jMon1Label.setLocation(PLAYER_X, PLAYER_Y); jMon1Label.setAlpha(1.0f);
        jMon2Label.setLocation(ENEMY_X, ENEMY_Y); jMon2Label.setAlpha(0f);
        txtWhat.setText("Wild " + activeEnemyMon.getName() + " appeared!");
        animateTurnBanner("PLAYER TURN", NEON_CYAN);
        animateSwitchIn(jMon2Label, ENEMY_X, () -> { isTurnInProgress = false; });
    }
    
    private void handlePlayerFaint() {
        animateFaint(jMon1Label, () -> {
            txtWhat.setText(activePlayerMon.getName() + " fainted!");
            boolean lost = true; for(Monster m : playerTeam) if(!m.isFainted()) lost = false;
            if(lost) { JOptionPane.showMessageDialog(this, "GAME OVER\nFloors Cleared: " + currentFloor); System.exit(0); }
            else { updateSwitchPanel(); showOverlay(switchPanel); isTurnInProgress = false; }
        });
    }

    // --- STATE UPDATERS ---

    private void updateSwitchPanel() {
        switchPanel.removeAll();
        JPanel contentBox = new JPanel(); contentBox.setLayout(new BoxLayout(contentBox, BoxLayout.Y_AXIS)); contentBox.setOpaque(false);
        JLabel title = new JLabel("TEAM ROSTER"); title.setFont(new Font("Arial", Font.BOLD, 40)); title.setForeground(Color.WHITE); title.setAlignmentX(Component.CENTER_ALIGNMENT); contentBox.add(title);
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0)); cardsPanel.setOpaque(false);
        
        for (Monster m : playerTeam) {
            JPanel card = new JPanel(); card.setPreferredSize(new Dimension(220, 320)); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setBackground(DARK_CARD_BG);
            Color borderCol = (m == activePlayerMon) ? NEON_CYAN : (m.isFainted() ? Color.RED : Color.GRAY);
            card.setBorder(BorderFactory.createLineBorder(borderCol, 3));
            JLabel icon = new JLabel(); if(m.getImage() != null) icon.setIcon(new ImageIcon(m.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH))); icon.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel name = new JLabel(m.getName()); name.setFont(new Font("Arial", Font.BOLD, 20)); name.setForeground(Color.WHITE); name.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel hpText = new JLabel(m.getCurrentHP() + "/" + m.getBaseHP()); hpText.setFont(new Font("Monospaced", Font.BOLD, 18)); hpText.setForeground(m.isFainted() ? Color.RED : NEON_GREEN); hpText.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(Box.createVerticalStrut(20)); card.add(icon); card.add(Box.createVerticalStrut(10)); card.add(name); card.add(Box.createVerticalStrut(10)); card.add(hpText);
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if(m.isFainted() || m == activePlayerMon) return;
                    isTurnInProgress = true; switchPanel.setVisible(false); 
                    animateSwitchOut(jMon1Label, () -> {
                        activePlayerMon = m; updateBattleState(); txtWhat.setText("Go! " + m.getName() + "!");
                        jMon1Label.setLocation(-400, PLAYER_Y);
                        animateSwitchIn(jMon1Label, PLAYER_X, () -> {
                            Timer t = new Timer(1000, evt -> performAttackSequence(activeEnemyMon, activePlayerMon, activeEnemyMon.getAbilities().get(0), false, () -> endTurn()));
                            t.setRepeats(false); t.start();
                        });
                    });
                }
            }); cardsPanel.add(card);
        }
        contentBox.add(cardsPanel);
        JButton closeBtn = new JButton("CLOSE"); closeBtn.setBackground(Color.RED); closeBtn.setForeground(Color.WHITE); closeBtn.addActionListener(e->switchPanel.setVisible(false)); contentBox.add(closeBtn);
        switchPanel.add(contentBox); switchPanel.revalidate(); switchPanel.repaint();
    }

    private void updateBattleState() {
        txtJName1.setText(activePlayerMon.getName()); txtJLives1.setText(activePlayerMon.getCurrentHP() + " / " + activePlayerMon.getBaseHP()); 
        hpBar1.snapHealth(activePlayerMon.getCurrentHP(), activePlayerMon.getBaseHP()); 
        typeBadge1.setType(activePlayerMon.getType());
        statusTray1.updateStatuses(activePlayerMon.getActiveStatuses());
        String allyPath = "/javamon/assets/ally_" + activePlayerMon.getName().toLowerCase() + ".png"; jMon1Label.setIcon(loadImage(allyPath, activePlayerMon));
        
        txtJName2.setText(activeEnemyMon.getName()); txtJLives2.setText(activeEnemyMon.getCurrentHP() + " / " + activeEnemyMon.getBaseHP()); 
        hpBar2.snapHealth(activeEnemyMon.getCurrentHP(), activeEnemyMon.getBaseHP()); 
        typeBadge2.setType(activeEnemyMon.getType());
        statusTray2.updateStatuses(activeEnemyMon.getActiveStatuses());
        String enemyPath = "/javamon/assets/enemy_" + activeEnemyMon.getName().toLowerCase() + ".png"; jMon2Label.setIcon(loadImage(enemyPath, activeEnemyMon));
        
        jMon1Label.setAlpha(1.0f); jMon2Label.setAlpha(1.0f);
        updateFightPanel();
    }

    // --- HELPERS ---
    private JLabel createText(int x, int y, int w, int h, Color c, boolean left) {
        JLabel l = new JLabel("", left ? SwingConstants.LEFT : SwingConstants.RIGHT);
        l.setBounds(x, y, w, h); l.setFont(PIXEL_FONT); l.setForeground(c); return l;
    }
    
    private ImageIcon loadImage(String path, Monster m) {
        String fileName = path.substring(path.lastIndexOf('/') + 1); Image img = AssetLoader.loadImagePreferResource(path, fileName);
        if(img == null && m.getImage() != null) img = m.getImage();
        if(img != null) return new ImageIcon(img.getScaledInstance(300, 300, Image.SCALE_SMOOTH));
        return null;
    }
    
    private void showOverlay(JPanel p) {
        fightPanel.setVisible(false); switchPanel.setVisible(false); helpPanel.setVisible(false); if(p != null) p.setVisible(true);
    }
    
    // --- CUSTOM COMPONENTS ---
    
    static class FadableSprite extends JLabel {
        private float alpha = 1.0f;
        private Color flashColor = null;

        public void setAlpha(float value) { this.alpha = Math.max(0.0f, Math.min(1.0f, value)); repaint(); }

        public void flash(Color c) {
            this.flashColor = c;
            repaint();
            Timer t = new Timer(150, e -> { flashColor = null; repaint(); });
            t.setRepeats(false); t.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (flashColor != null && getIcon() instanceof ImageIcon) {
                Image img = ((ImageIcon)getIcon()).getImage();
                BufferedImage bImg = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D gBI = bImg.createGraphics();
                gBI.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                gBI.setComposite(AlphaComposite.SrcAtop); 
                gBI.setColor(flashColor);
                gBI.fillRect(0, 0, getWidth(), getHeight());
                gBI.dispose();
                g.drawImage(bImg, 0, 0, null);
            } else {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                super.paintComponent(g2d);
                g2d.dispose();
            }
        }
    }

    static class SmoothBatteryBar extends JPanel {
        private int currentHP = 100, maxHP = 100; private double displayHP = 100;
        private final int TOTAL_CELLS = 10; private Timer smoothTimer; private Runnable onComplete;

        public SmoothBatteryBar() { setOpaque(false); }

        public void snapHealth(int current, int max) {
            this.currentHP = current; this.maxHP = max; this.displayHP = current;
            if(smoothTimer != null) smoothTimer.stop(); repaint();
        }

        public void slideHealth(int current, int max, Runnable callback) {
            this.currentHP = current; this.maxHP = max; this.onComplete = callback;
            if(smoothTimer != null && smoothTimer.isRunning()) smoothTimer.stop();
            smoothTimer = new Timer(15, e -> {
                if (Math.abs(displayHP - currentHP) < 2.0) {
                    displayHP = currentHP; ((Timer)e.getSource()).stop(); repaint(); if(onComplete != null) onComplete.run();
                } else {
                    displayHP += (displayHP < currentHP) ? 2.5 : -2.5; repaint();
                }
            }); smoothTimer.start();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2d = (Graphics2D) g; g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double percent = displayHP / maxHP; int cellsFilled = (int) Math.ceil(percent * TOTAL_CELLS);
            Color barColor = (percent > 0.5) ? new Color(0, 230, 0) : (percent > 0.2) ? new Color(255, 215, 0) : new Color(255, 50, 50);
            int w = getWidth(), h = getHeight(), cellGap = 4, cellWidth = (w - (TOTAL_CELLS - 1) * cellGap) / TOTAL_CELLS;
            for (int i = 0; i < TOTAL_CELLS; i++) {
                if (i < cellsFilled) { g2d.setColor(barColor); g2d.fillRect(i * (cellWidth + cellGap), 0, cellWidth, h); g2d.setColor(new Color(255, 255, 255, 70)); g2d.fillRect(i * (cellWidth + cellGap), 0, cellWidth, h / 2); } 
                else { g2d.setColor(new Color(40, 40, 40, 200)); g2d.fillRect(i * (cellWidth + cellGap), 0, cellWidth, h); }
                g2d.setColor(Color.BLACK); g2d.setStroke(new BasicStroke(2)); g2d.drawRect(i * (cellWidth + cellGap), 0, cellWidth, h);
            }
        }
    }
    
    // New Helper Classes
    class TypeBadge extends JLabel {
        public TypeBadge() {
            setOpaque(true); 
            setFont(BADGE_FONT); 
            setForeground(Color.WHITE); 
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new LineBorder(Color.WHITE, 1, true));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { showTypeTooltip(getText(), TypeBadge.this); }
                public void mouseExited(MouseEvent e) { infoOverlay.setVisible(false); }
            });
        }
        
        public void setType(String type) {
            setText(type.toUpperCase());
            setBackground(getColorForType(type));
        }
        
        private Color getColorForType(String type) {
            if(type.equals("Fire")) return new Color(220, 50, 0);
            if(type.equals("Water")) return new Color(50, 100, 255);
            if(type.equals("Grass")) return new Color(50, 200, 50);
            if(type.equals("Lightning")) return new Color(220, 200, 0);
            if(type.equals("Ice")) return new Color(100, 200, 255);
            return Color.GRAY;
        }
    }
    
    class StatusTray extends JPanel {
        public StatusTray() {
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        }
        
        public void updateStatuses(List<StatusEffect> statuses) {
            removeAll();
            for(StatusEffect s : statuses) {
                JLabel icon = new JLabel(s.name.substring(0, 3).toUpperCase());
                icon.setOpaque(true);
                icon.setFont(new Font("Arial", Font.BOLD, 10));
                icon.setForeground(Color.WHITE);
                if(s.type.equals("DMG")) icon.setBackground(new Color(255, 100, 0));
                else if(s.type.equals("STOP")) icon.setBackground(new Color(100, 100, 255));
                else icon.setBackground(new Color(50, 200, 50));
                
                icon.setBorder(new LineBorder(Color.BLACK, 1));
                icon.setPreferredSize(new Dimension(30, 20));
                icon.setHorizontalAlignment(SwingConstants.CENTER);
                add(icon);
            }
            revalidate();
            repaint();
        }
    }
}