package javamon;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.net.URL; // Needed for playSoundEffect
import java.util.prefs.Preferences; // Needed for playSoundEffect
import javax.sound.sampled.*; // Needed for playSoundEffect
import java.io.IOException; // Needed for playSoundEffect



public class GameWindow extends JFrame {
    
    // --- Data ---
    private List<Monster> playerTeam;
    private List<Monster> enemyTeam;
    private List<Monster> masterMonsterPool; 
    private String trainerClass; 
    
 // Add these after your existing field declarations (around line 50)

 // NEW: Damage preview system
	 private JPanel damagePreviewPanel;
	 private JLabel[] damagePreviewLabels = new JLabel[4];
	 private boolean showDamagePreviews = true; // Toggle in settings
	
	 // NEW: Turn order indicator
	 private JLabel turnOrderLabel;
	 private JPanel turnOrderPanel;
	
	 // NEW: Speed comparison indicator
	 private JLabel speedIndicator;
 
    private Monster activePlayerMon;
    private Monster activeEnemyMon;
    private int currentFloor = 1;
    
    private boolean isTurnInProgress = false;

    // --- UI Components ---
    private FadableSprite jMon1Label, jMon2Label; 
    
    private JLabel lblP1Name, lblP1Level; 
    private JLabel lblP2Name, lblP2Level; 
    
    private JLabel txtJLives1, txtJLives2; 
    private SmoothBatteryBar hpBar1, hpBar2;    
    private XPBar xpBar1; 
    
    private JTextArea txtWhat;
    private JLabel txtFloor;
    private JPanel bgPanel;
    private JLayeredPane layeredPane;
    
    private JPanel fightPanel;
    private JPanel switchPanel;
    private JPanel helpPanel;
    
    private JPanel infoOverlay;
    private JLabel infoTitle;
    private JTextPane infoBody;
    
    private JLabel turnBanner;
    private JPanel flashPanel;

    private TypeBadge typeBadge1, typeBadge2;
    private StatusTray statusTray1, statusTray2;
    
    private final SoundManager soundManager = SoundManager.getInstance(); // Get SoundManager

    // --- Performance & Animation Fields ---
    private Timer idleTimer1;
    private Timer idleTimer2;
    private boolean repaintScheduled = false; 
    
    private Map<Monster, ImageIcon> scaledAllyIcons = new HashMap<>();
    private Map<Monster, ImageIcon> scaledEnemyIcons = new HashMap<>();
    private List<JLabel> particlePool = new ArrayList<>();
    private final int POOL_SIZE = 25;
    
    // START OF OPTIMIZATION FIXES
    private static final int PARTICLE_COUNT = 2;  
    private static final int XP_PARTICLE_COUNT = 3;  
    private static final int SHAKE_INTENSITY = 6;  
    // END OF OPTIMIZATION FIXES

    private static final Color TRANSPARENT_BLACK = new Color(0, 0, 0, 220);
    private static final Color DARK_CARD_BG = new Color(30, 30, 30, 240);
    private static final Color NEON_CYAN = new Color(0, 255, 255);
    private static final Color NEON_GREEN = new Color(50, 255, 50);
    private static final Color NEON_GOLD = new Color(255, 215, 0);
    private static final Color XP_BLUE = new Color(0, 150, 255);
    private static final Color DAMAGE_RED = new Color(255, 50, 50);
    private static final Color EFFECTIVE_COLOR = new Color(100, 255, 100);
    private static final Color WEAK_COLOR = new Color(255, 100, 100);
    private static final Color INFO_BG = new Color(20, 20, 30, 230);
    
    private static final Font PIXEL_FONT = new Font("Monospaced", Font.BOLD, 24);
    private static final Font LEVEL_FONT = new Font("Monospaced", Font.BOLD, 18);
    private static final Font LOG_FONT = new Font("Monospaced", Font.BOLD, 22);
    private static final Font UI_FONT = new Font("Arial", Font.BOLD, 14); 
    private static final Font DMG_FONT = new Font("Arial", Font.BOLD, 40);
    private static final Font BANNER_FONT = new Font("Impact", Font.ITALIC, 60);
    private static final Font BADGE_FONT = new Font("Arial", Font.BOLD, 11);
    
    private static final int PLAYER_X = 95, PLAYER_Y = 239;
    private static final int ENEMY_X = 749, ENEMY_Y = 49;

    public GameWindow(List<Monster> playerTeam, List<Monster> enemyTeam, List<Monster> masterPool, String trainerClass) {
        this.playerTeam = (playerTeam != null && !playerTeam.isEmpty()) ? playerTeam : createFallbackTeam("Player");
        this.enemyTeam = (enemyTeam != null && !enemyTeam.isEmpty()) ? enemyTeam : createFallbackTeam("Enemy");
        this.masterMonsterPool = (masterPool != null) ? masterPool : this.playerTeam;
        this.trainerClass = (trainerClass != null) ? trainerClass : "ELEMENTALIST"; 

        this.activePlayerMon = this.playerTeam.get(0);
        this.activeEnemyMon = this.enemyTeam.get(0);
        
        for(Monster m : this.enemyTeam) {
            m.setLevel(1);
        }

        // --- MUSIC CONTROL ---
        soundManager.stopMenuMusic(); // Stop menu music on battle start
        soundManager.playGameMusic();  // Start game music
        // --- END MUSIC CONTROL ---

        setTitle("JavaMon Battle");
        setSize(1280, 760);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
     // Set window icon
        Image icon = AssetLoader.loadImage("/javamon/assets/icon.png", "icon.png");
        if (icon != null) setIconImage(icon);
        
        layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, 1280, 760);
        setContentPane(layeredPane);
        layeredPane.setDoubleBuffered(true);

        bgPanel = new JPanel() {
            private Image bg = new ImageIcon(getClass().getResource("/javamon/assets/GameBG.png")).getImage();
            protected void paintComponent(Graphics g) { 
                super.paintComponent(g); 
                Graphics2D g2d = (Graphics2D) g; 
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                
                if (bg != null) g.drawImage(bg, 0, 0, getWidth(), getHeight(), this); 
            }
        };
        bgPanel.setLayout(null);
        bgPanel.setBounds(0, 0, 1280, 760);
        layeredPane.add(bgPanel, JLayeredPane.DEFAULT_LAYER);
        bgPanel.setDoubleBuffered(true);

        // Fix: Initialize Particle Pool
        for (int i = 0; i < POOL_SIZE; i++) {
            JLabel particle = new JLabel();
            particle.setVisible(false);
            layeredPane.add(particle, JLayeredPane.POPUP_LAYER);
            particlePool.add(particle);
        }

        initializeBaseUI();
        createOverlays(layeredPane);
        updateBattleState();
        
        // Fix: Start Idle Animations
        startIdleAnimation(jMon1Label, PLAYER_Y, true);
        startIdleAnimation(jMon2Label, ENEMY_Y, false);
        animateBattleStart();
        
        SwingUtilities.invokeLater(() -> animateTurnBanner("PLAYER TURN", NEON_CYAN));
    }
    
    @Override
    public void dispose() {
        if(idleTimer1 != null) idleTimer1.stop();
        if(idleTimer2 != null) idleTimer2.stop();
        super.dispose();
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

        lblP1Name = createText(40, 40, 220, 30, Color.BLACK, true); 
        bgPanel.add(lblP1Name); bgPanel.setComponentZOrder(lblP1Name, 0);

        lblP1Level = new JLabel("Lvl 1");
        lblP1Level.setFont(LEVEL_FONT); lblP1Level.setForeground(Color.DARK_GRAY);
        lblP1Level.setBounds(40, 70, 80, 20);
        bgPanel.add(lblP1Level); bgPanel.setComponentZOrder(lblP1Level, 0);

        typeBadge1 = new TypeBadge(); 
        typeBadge1.setBounds(130, 70, 80, 20);
        bgPanel.add(typeBadge1); bgPanel.setComponentZOrder(typeBadge1, 0);

        xpBar1 = new XPBar();
        xpBar1.setBounds(40, 95, 250, 6);
        bgPanel.add(xpBar1); bgPanel.setComponentZOrder(xpBar1, 0);

        hpBar1 = new SmoothBatteryBar(); 
        hpBar1.setBounds(40, 110, 280, 25);
        bgPanel.add(hpBar1); bgPanel.setComponentZOrder(hpBar1, 0); 
        
        txtJLives1 = createText(50, 140, 300, 30, Color.GRAY, false); 
        bgPanel.add(txtJLives1); bgPanel.setComponentZOrder(txtJLives1, 0);
        
        statusTray1 = new StatusTray(); 
        statusTray1.setBounds(40, 170, 300, 25); 
        bgPanel.add(statusTray1); bgPanel.setComponentZOrder(statusTray1, 0);

        lblP2Name = createText(940, 335, 220, 30, Color.BLACK, true);
        bgPanel.add(lblP2Name); bgPanel.setComponentZOrder(lblP2Name, 0);

        lblP2Level = new JLabel("Lvl 1");
        lblP2Level.setFont(LEVEL_FONT); lblP2Level.setForeground(Color.DARK_GRAY);
        lblP2Level.setBounds(940, 365, 80, 20);
        bgPanel.add(lblP2Level); bgPanel.setComponentZOrder(lblP2Level, 0);

        typeBadge2 = new TypeBadge(); 
        typeBadge2.setBounds(1030, 365, 80, 20);
        bgPanel.add(typeBadge2); bgPanel.setComponentZOrder(typeBadge2, 0);

        hpBar2 = new SmoothBatteryBar(); 
        hpBar2.setBounds(940, 400, 280, 25); 
        bgPanel.add(hpBar2); bgPanel.setComponentZOrder(hpBar2, 0); 
        
        txtJLives2 = createText(930, 430, 300, 30, Color.GRAY, false); 
        bgPanel.add(txtJLives2); bgPanel.setComponentZOrder(txtJLives2, 0);
        
        statusTray2 = new StatusTray(); 
        statusTray2.setBounds(940, 460, 300, 25); 
        bgPanel.add(statusTray2); bgPanel.setComponentZOrder(statusTray2, 0);

        txtFloor = new JLabel("FLOOR 1", SwingConstants.LEFT);
        txtFloor.setBounds(25, 545, 300, 40); 
        txtFloor.setFont(new Font("Monospaced", Font.BOLD, 35));
        txtFloor.setForeground(Color.BLACK);
        bgPanel.add(txtFloor); bgPanel.setComponentZOrder(txtFloor, 0);

        ImageIcon lBox = new ImageIcon(getClass().getResource("/javamon/assets/LOWER_TEXTBOX.png"));
        JLabel bottomBox = new JLabel(lBox); bottomBox.setBounds(-10, 500, lBox.getIconWidth(), lBox.getIconHeight());
        bgPanel.add(bottomBox);

        txtWhat = new JTextArea("What will " + activePlayerMon.getName() + " do?");
        txtWhat.setBounds(190, 620, 750, 90);
        txtWhat.setFont(LOG_FONT); 
        txtWhat.setForeground(Color.BLACK);
        txtWhat.setOpaque(false);
        txtWhat.setEditable(false);
        txtWhat.setLineWrap(true);       
        txtWhat.setWrapStyleWord(true); 
        txtWhat.setHighlighter(null);    
        bgPanel.add(txtWhat); bgPanel.setComponentZOrder(txtWhat, 0);

        // --- BATTLE BUTTONS ---
        ImageIcon fightIcon = new ImageIcon(getClass().getResource("/javamon/assets/FIGHTBTN.png"));
        JButton fightButton = createAnimatedButton(fightIcon);
        fightButton.setBounds(650, 600, fightIcon.getIconWidth(), fightIcon.getIconHeight());
        fightButton.addActionListener(e -> { 
            if (!isTurnInProgress) { 
                playSoundEffect("/javamon/assets/ButtonsFx.wav"); // <--- APPLIED SOUND EFFECT
                updateFightPanel(); showOverlay(fightPanel); 
            } 
        });
        bgPanel.add(fightButton); bgPanel.setComponentZOrder(fightButton, 0);

        ImageIcon jMonIcon = new ImageIcon(getClass().getResource("/javamon/assets/JAVAMONBTN.png"));
        JButton jMonButton = createAnimatedButton(jMonIcon);
        jMonButton.setBounds(920, 600, jMonIcon.getIconWidth(), jMonIcon.getIconHeight());
        jMonButton.addActionListener(e -> { 
            if (!isTurnInProgress) { 
                playSoundEffect("/javamon/assets/ButtonsFx.wav"); // <--- APPLIED SOUND EFFECT
                updateSwitchPanel(); showOverlay(switchPanel); 
            } 
        });
        bgPanel.add(jMonButton); bgPanel.setComponentZOrder(jMonButton, 0);

        ImageIcon helpIcon = new ImageIcon(getClass().getResource("/javamon/assets/HELPBTN.png"));
        JButton helpButton = createAnimatedButton(helpIcon);
        helpButton.setBounds(780, 660, helpIcon.getIconWidth(), helpIcon.getIconHeight());
        helpButton.addActionListener(e -> {
            playSoundEffect("/javamon/assets/ButtonsFx.wav"); // <--- APPLIED SOUND EFFECT
            showOverlay(helpPanel);
        });
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

     // Replace the helpPanel creation in createOverlays method with this:

        helpPanel = new JPanel(new BorderLayout(0, 0));
        helpPanel.setBounds(200, 50, 880, 620);
        helpPanel.setBackground(new Color(20, 20, 30));
        helpPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(NEON_CYAN, 3),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        // Header
        JPanel helpHeader = new JPanel(new BorderLayout());
        helpHeader.setBackground(new Color(15, 15, 25));
        helpHeader.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel helpTitle = new JLabel("⚔ JAVAMON BATTLE GUIDE ⚔");
        helpTitle.setFont(new Font("Impact", Font.BOLD, 32));
        helpTitle.setForeground(NEON_CYAN);
        helpTitle.setHorizontalAlignment(SwingConstants.CENTER);
        helpHeader.add(helpTitle, BorderLayout.CENTER);

        helpPanel.add(helpHeader, BorderLayout.NORTH);

        // Content with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.setBackground(new Color(25, 25, 35));
        tabbedPane.setForeground(Color.WHITE);

        // TAB 1: Type Chart
        JPanel typeChartPanel = createTypeChartPanel();
        tabbedPane.addTab("TYPE CHART", typeChartPanel);

        // TAB 2: Game Mechanics
        JPanel mechanicsPanel = createMechanicsPanel();
        tabbedPane.addTab("MECHANICS", mechanicsPanel);

        // TAB 3: Classes & Tips
        JPanel tipsPanel = createTipsPanel();
        tabbedPane.addTab("CLASSES & TIPS", tipsPanel);

        helpPanel.add(tabbedPane, BorderLayout.CENTER);

        // Footer with close button
        JPanel helpFooter = new JPanel(new FlowLayout(FlowLayout.CENTER));
        helpFooter.setBackground(new Color(15, 15, 25));
        helpFooter.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        JButton closeHelp = new JButton("CLOSE");
        closeHelp.setFont(new Font("Arial", Font.BOLD, 18));
        closeHelp.setPreferredSize(new Dimension(200, 45));
        closeHelp.setBackground(new Color(200, 50, 50));
        closeHelp.setForeground(Color.WHITE);
        closeHelp.setFocusPainted(false);
        closeHelp.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        closeHelp.setCursor(new Cursor(Cursor.HAND_CURSOR));

        closeHelp.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                closeHelp.setBackground(new Color(230, 70, 70));
            }
            public void mouseExited(MouseEvent e) {
                closeHelp.setBackground(new Color(200, 50, 50));
            }
        });

        closeHelp.addActionListener(e -> {
            playSoundEffect("/javamon/assets/ButtonsFx.wav");
            helpPanel.setVisible(false);
        });

        helpFooter.add(closeHelp);
        helpPanel.add(helpFooter, BorderLayout.SOUTH);

        helpPanel.setVisible(false);
        lp.add(helpPanel, JLayeredPane.POPUP_LAYER);
        
        // POKEMON STYLE OVERLAY: Start off-screen
        infoOverlay = new JPanel(new BorderLayout(10, 5));
        infoOverlay.setBounds(-350, 0, 350, 180); 
        infoOverlay.setBackground(INFO_BG);
        infoOverlay.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(NEON_CYAN, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        infoOverlay.setVisible(false);
        
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

    private void animateBattleStart() {
        jMon1Label.setAlpha(0f);
        jMon2Label.setAlpha(0f);
        
        jMon1Label.setLocation(PLAYER_X, 600);
        jMon2Label.setLocation(ENEMY_X, -200);
        
        Timer riseUp = new Timer(20, null);
        final float[] alpha = {0f};
        
        riseUp.addActionListener(e -> {
            boolean done1 = false, done2 = false;
            
            if(jMon1Label.getY() > PLAYER_Y) {
                jMon1Label.setLocation(PLAYER_X, jMon1Label.getY() - 15);
            } else {
                jMon1Label.setLocation(PLAYER_X, PLAYER_Y); done1 = true;
            }
            
            if(jMon2Label.getY() < ENEMY_Y) {
                jMon2Label.setLocation(ENEMY_X, jMon2Label.getY() + 15);
            } else {
                jMon2Label.setLocation(ENEMY_X, ENEMY_Y); done2 = true;
            }
            
            alpha[0] += 0.05f;
            jMon1Label.setAlpha(Math.min(1.0f, alpha[0]));
            jMon2Label.setAlpha(Math.min(1.0f, alpha[0]));
            
            if(done1 && done2) {
                riseUp.stop();
                jMon1Label.setAlpha(1.0f); jMon2Label.setAlpha(1.0f);
            }
            scheduleRepaint();
        });
        riseUp.start();
    }
    
    // NEW: Wild Encounter Flash & Slide Animation
    private void wildEncounterAnimation(Runnable onComplete) {
        // Ensure player is in place
        jMon1Label.setLocation(PLAYER_X, PLAYER_Y);
        jMon1Label.setAlpha(1.0f);
        
        // Flash effect
        final int[] flashes = {0};
        Timer flashTimer = new Timer(150, null); // 150ms = more noticeable
        
        flashTimer.addActionListener(e -> {
            flashPanel.setVisible(!flashPanel.isVisible());
            flashes[0]++;
            scheduleRepaint(flashPanel);
            
            if (flashes[0] >= 6) {
                flashTimer.stop();
                flashPanel.setVisible(false);
                
                // Slide in effect
                jMon2Label.setLocation(1280, ENEMY_Y);
                jMon2Label.setAlpha(1.0f);
                jMon2Label.setVisible(true);
                
                Timer slide = new Timer(15, null);
                slide.addActionListener(ev -> {
                    jMon2Label.setLocation(jMon2Label.getX() - 25, ENEMY_Y);
                    
                    if (jMon2Label.getX() <= ENEMY_X) {
                        slide.stop();
                        jMon2Label.setLocation(ENEMY_X, ENEMY_Y);
                        
                        // Zoom in slightly
                        Timer zoom = new Timer(20, null);
                        final int[] zoomFrame = {0};
                        final int originalY = ENEMY_Y;
                        
                        zoom.addActionListener(zev -> {
                            if (zoomFrame[0] < 5) {
                                jMon2Label.setLocation(ENEMY_X - 10, originalY - 10);
                            } else if (zoomFrame[0] < 10) {
                                jMon2Label.setLocation(ENEMY_X, originalY);
                            } else {
                                zoom.stop();
                                if (onComplete != null) onComplete.run();
                            }
                            zoomFrame[0]++;
                            scheduleRepaint(jMon2Label);
                        });
                        zoom.start();
                    }
                    scheduleRepaint(jMon2Label);
                });
                slide.start();
            }
        });
        flashTimer.start();
    }
    
    private void animateSingleSpriteIn(FadableSprite sprite, int startY, int targetY, Runnable onComplete) {
        sprite.setAlpha(0f);
        sprite.setLocation(sprite.getX(), startY);
        sprite.setVisible(true); 
        
        Timer riseUp = new Timer(20, null);
        final float[] alpha = {0f};
        
        riseUp.addActionListener(e -> {
            boolean done = false;
            
            if(startY < targetY) {
                if(sprite.getY() < targetY) {
                    sprite.setLocation(sprite.getX(), sprite.getY() + 15);
                } else {
                    sprite.setLocation(sprite.getX(), targetY);
                    done = true;
                }
            } else {
                if(sprite.getY() > targetY) {
                    sprite.setLocation(sprite.getX(), sprite.getY() - 15);
                } else {
                    sprite.setLocation(sprite.getX(), targetY);
                    done = true;
                }
            }
            
            alpha[0] += 0.05f;
            sprite.setAlpha(Math.min(1.0f, alpha[0]));
            
            if(done) {
                riseUp.stop();
                sprite.setAlpha(1.0f);
                if(onComplete != null) onComplete.run();
            }
            scheduleRepaint(sprite);
        });
        riseUp.start();
    }

    private void startIdleAnimation(FadableSprite sprite, int baseY, boolean isPlayer) {
        Timer timer = isPlayer ? idleTimer1 : idleTimer2;
        if (timer != null && timer.isRunning()) return;

        final int[] frame = {0};
        
        timer = new Timer(60, e -> { 
            if(!isTurnInProgress) {
                int offset = (int)(Math.sin(frame[0] * 0.1) * 4);
                sprite.setLocation(sprite.getX(), baseY + offset);
                scheduleRepaint(sprite);
                frame[0]++;
            }
        });
        
        if (isPlayer) idleTimer1 = timer; else idleTimer2 = timer;
        timer.start();
    }

    // 1. OPTIMIZED animateChargeUp
    private void animateChargeUp(FadableSprite sprite, Runnable onComplete) {
        final int[] pulses = {0};
        Timer charge = new Timer(120, null); // Was 80, now slower
        charge.addActionListener(e -> {
            if(pulses[0] % 2 == 0) {
                sprite.setAlpha(1.0f);
                // Only spawn burst on FIRST pulse, not every pulse
                if(pulses[0] == 0) {
                    animateStatBurst(sprite);
                }
            } else {
                sprite.setAlpha(0.6f);
            }
            pulses[0]++;
            if(pulses[0] >= 6) { // Was 8, reduced
                charge.stop();
                sprite.setAlpha(1.0f);
                onComplete.run();
            }
            scheduleRepaint(sprite);
        });
        charge.start();
    }

    private void showSpeedLines(boolean leftToRight) {
        for(int i = 0; i < 8; i++) {
            JPanel line = new JPanel();
            line.setBackground(new Color(255, 255, 255, 180));
            int startX = leftToRight ? -50 : 1300;
            line.setBounds(startX, 150 + (i * 60), 100, 4);
            layeredPane.add(line, JLayeredPane.POPUP_LAYER);
            
            Timer zoom = new Timer(15, null);
            final int[] speed = {45};
            
            zoom.addActionListener(e -> {
                line.setLocation(line.getX() + (leftToRight ? speed[0] : -speed[0]), line.getY());
                if(line.getX() > 1350 || line.getX() < -150) {
                    zoom.stop();
                    layeredPane.remove(line);
                    scheduleRepaint(line);
                }
            });
            
            Timer delay = new Timer(i * 30, evt -> zoom.start());
            delay.setRepeats(false); delay.start();
        }
    }

    // START OF OPTIMIZATION FIXES (spawnTypeEffect)
    private void spawnTypeEffect(String type, JComponent target) {
        Point p = target.getLocation();
        for(int i = 0; i < PARTICLE_COUNT; i++) { 
            if (particlePool.isEmpty()) break;
            JLabel particle = particlePool.remove(0);

            Color color;
            String symbol;
            
            switch(type) {
                case "Fire": color = new Color(255, 100, 0); symbol = "●"; break;
                case "Water": color = new Color(50, 150, 255); symbol = "●"; break;
                case "Lightning": color = new Color(255, 255, 0); symbol = "★"; break;
                case "Ice": color = new Color(150, 220, 255); symbol = "❄"; break;
                case "Grass": color = new Color(50, 200, 50); symbol = "●"; break;
                case "Dark": color = new Color(100, 0, 150); symbol = "●"; break;
                case "Bug": color = new Color(150, 200, 50); symbol = "●"; break;
                default: color = Color.WHITE; symbol = "●";
            }
            
            particle.setText(symbol);
            particle.setFont(new Font("Arial", Font.BOLD, 24)); 
            particle.setForeground(color);
            particle.setBounds(p.x + 150, p.y + 150, 30, 30);
            particle.setVisible(true);
            
            final double angle = (Math.PI * 2 * i) / PARTICLE_COUNT;
            final int[] life = {0};
            
            Timer burst = new Timer(80, null);  
            burst.addActionListener(e -> {
                life[0]++;
                int dist = life[0] * 6;  
                particle.setLocation(
                    p.x + 150 + (int)(Math.cos(angle) * dist),
                    p.y + 150 + (int)(Math.sin(angle) * dist)
                );
                
                int alpha = Math.max(0, 255 - (life[0] * 40));  
                particle.setForeground(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
                
                if(life[0] > 6) {  
                    burst.stop();
                    particle.setVisible(false);
                    particlePool.add(particle);
                    scheduleRepaint(particle);
                }
            });
            burst.start();
        }
    }
    // END OF OPTIMIZATION FIXES (spawnTypeEffect)

    // START OF OPTIMIZATION FIXES (animateXPGain)
    private void animateXPGain(int amount) {
        Point p = jMon1Label.getLocation();
        for(int i = 0; i < XP_PARTICLE_COUNT; i++) { 
            if (particlePool.isEmpty()) break;
            JLabel coin = particlePool.remove(0);

            coin.setText("✦");
            coin.setFont(new Font("Arial", Font.BOLD, 18));  
            coin.setForeground(Color.YELLOW);
            coin.setBounds(p.x + 150, p.y + 100, 20, 20);
            coin.setVisible(true);
            
            Timer rise = new Timer(80, null);  
            final int[] life = {0};
            final int xOffset = (int)(Math.random() * 80) - 40;  
            
            rise.addActionListener(e -> {
                life[0]++;
                coin.setLocation(coin.getX() + (xOffset/15), coin.getY() - 5);  
                
                int alpha = Math.max(0, 255 - (life[0] * 15));  
                coin.setForeground(new Color(255, 215, 0, alpha));
                
                if(life[0] > 15) {  
                    rise.stop();
                    coin.setVisible(false);
                    particlePool.add(coin);
                    scheduleRepaint(coin);
                }
            });
            
            Timer delay = new Timer(i * 100, evt -> rise.start());  
            delay.setRepeats(false); delay.start();
        }
    }
    // END OF OPTIMIZATION FIXES (animateXPGain)
    
    private void animateDodge(FadableSprite sprite) {
        Point original = sprite.getLocation();
        Timer dodge = new Timer(20, null);
        final int[] frame = {0};
        
        dodge.addActionListener(e -> {
            if(frame[0] < 5) {
                sprite.setLocation(original.x + 20, original.y - 10);
                sprite.setAlpha(0.5f);
            } else if(frame[0] < 10) {
                sprite.setLocation(original.x - 10, original.y);
                sprite.setAlpha(0.8f);
            } else {
                sprite.setLocation(original);
                sprite.setAlpha(1.0f);
                dodge.stop();
            }
            frame[0]++;
            scheduleRepaint(sprite);
        });
        dodge.start();
        
        showEnhancedFloatingText("MISS!", Color.GRAY, sprite, false);
    }

    private void animateTurnBanner(String text, Color bg) {
        turnBanner.setText(text); turnBanner.setBackground(bg); turnBanner.setForeground(Color.WHITE);
        Timer timer = new Timer(5, null); final int[] x = {-1280}; final int[] pause = {0};
        timer.addActionListener(e -> {
            if (x[0] < 0) { x[0] += 40; turnBanner.setLocation(x[0], 300); } 
            else if (pause[0] < 30) { pause[0]++; } 
            else { x[0] += 40; turnBanner.setLocation(x[0], 300); if (x[0] > 1280) timer.stop(); }
            scheduleRepaint(turnBanner);
        }); timer.start();
    }

    // NEW: POKEMON STYLE FIGHT PANEL (2x2 Grid with Move Cards)
    private void updateFightPanel() {
        fightPanel.removeAll();
        fightPanel.setLayout(new GridLayout(2, 3, 15, 15)); // 2 rows, 3 columns for 5 moves + cancel
        
        // Add the 4 regular abilities
        for (int i = 0; i < 4; i++) {
            Ability a = activePlayerMon.getAbilities().get(i);
            boolean isSupport = a.getType().equals("Buff") || a.getType().equals("Healing");
            
            double effectiveness = isSupport ? 1.0 : BattleMechanics.getTypeMultiplier(a.getType(), activeEnemyMon.getType(), trainerClass);
            
            // Create Pokémon-style move button
            JPanel moveCard = new JPanel();
            moveCard.setLayout(new BorderLayout(5, 5));
            moveCard.setBackground(new Color(45, 45, 55));
            moveCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getTypeColor(a.getType()), 3),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            moveCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Move name
            JLabel moveName = new JLabel(a.getName());
            moveName.setFont(new Font("Arial", Font.BOLD, 16));
            moveName.setForeground(Color.WHITE);
            
            // Type badge (small pill)
            JLabel typeBadge = new JLabel(a.getType().toUpperCase());
            typeBadge.setFont(new Font("Arial", Font.BOLD, 10));
            typeBadge.setForeground(Color.WHITE);
            typeBadge.setOpaque(true);
            typeBadge.setBackground(getTypeColor(a.getType()));
            typeBadge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            
            // Effectiveness indicator
            String effIcon = isSupport ? "+" : (effectiveness > 1.0) ? "▲" : (effectiveness < 1.0) ? "▼" : "●";
            Color effColor = isSupport ? NEON_CYAN : (effectiveness > 1.0) ? EFFECTIVE_COLOR : (effectiveness < 1.0) ? WEAK_COLOR : Color.LIGHT_GRAY;
            JLabel effLabel = new JLabel(effIcon);
            effLabel.setFont(new Font("Arial", Font.BOLD, 20));
            effLabel.setForeground(effColor);
            
            // Layout
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setOpaque(false);
            topPanel.add(moveName, BorderLayout.WEST);
            topPanel.add(typeBadge, BorderLayout.EAST);
            
            moveCard.add(topPanel, BorderLayout.NORTH);
            moveCard.add(effLabel, BorderLayout.EAST);
            
            // Hover effects
            moveCard.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    moveCard.setBackground(new Color(60, 60, 75));
                    showAbilityTooltip(a, moveCard, effectiveness);
                }
                public void mouseExited(MouseEvent e) {
                    moveCard.setBackground(new Color(45, 45, 55));
                    infoOverlay.setVisible(false);
                }
                public void mousePressed(MouseEvent e) {
                    if (!isTurnInProgress) {
                        moveCard.setBackground(new Color(35, 35, 45));
                    }
                }
                public void mouseReleased(MouseEvent e) {
                    moveCard.setBackground(new Color(60, 60, 75));
                }
                public void mouseClicked(MouseEvent e) {
                    if (!isTurnInProgress) {
                        initiateCombatRound(a);
                    }
                }
            });
            
            fightPanel.add(moveCard);
        }
        
        // Add the SECRET SKILL (5th move)
        Ability secret = generateSecretAbility();
        JPanel secretCard = new JPanel();
        secretCard.setLayout(new BorderLayout(5, 5));
        secretCard.setBackground(new Color(60, 50, 0)); // Gold background
        secretCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(NEON_GOLD, 3),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        secretCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Secret move name with stars
        JLabel secretName = new JLabel("★ " + secret.getName().toUpperCase() + " ★");
        secretName.setFont(new Font("Arial", Font.BOLD, 16));
        secretName.setForeground(NEON_GOLD);
        
        // Class ULT badge
        JLabel ultBadge = new JLabel("CLASS ULT");
        ultBadge.setFont(new Font("Arial", Font.BOLD, 10));
        ultBadge.setForeground(Color.BLACK);
        ultBadge.setOpaque(true);
        ultBadge.setBackground(NEON_GOLD);
        ultBadge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        
        // Star icon
        JLabel starIcon = new JLabel("★");
        starIcon.setFont(new Font("Arial", Font.BOLD, 24));
        starIcon.setForeground(NEON_GOLD);
        
        // Layout
        JPanel secretTopPanel = new JPanel(new BorderLayout());
        secretTopPanel.setOpaque(false);
        secretTopPanel.add(secretName, BorderLayout.WEST);
        secretTopPanel.add(ultBadge, BorderLayout.EAST);
        
        secretCard.add(secretTopPanel, BorderLayout.NORTH);
        secretCard.add(starIcon, BorderLayout.EAST);
        
        // Hover effects for secret
        secretCard.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                secretCard.setBackground(new Color(80, 70, 0));
                showAbilityTooltip(secret, secretCard, 1.0);
            }
            public void mouseExited(MouseEvent e) {
                secretCard.setBackground(new Color(60, 50, 0));
                infoOverlay.setVisible(false);
            }
            public void mousePressed(MouseEvent e) {
                if (!isTurnInProgress) {
                    secretCard.setBackground(new Color(40, 30, 0));
                }
            }
            public void mouseReleased(MouseEvent e) {
                secretCard.setBackground(new Color(80, 70, 0));
            }
            public void mouseClicked(MouseEvent e) {
                if (!isTurnInProgress) {
                    initiateCombatRound(secret);
                }
            }
        });
        
        fightPanel.add(secretCard);
        
        // Add CANCEL button (6th slot)
        JPanel cancelCard = new JPanel();
        cancelCard.setLayout(new BorderLayout());
        cancelCard.setBackground(new Color(50, 50, 50));
        cancelCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        cancelCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel cancelLabel = new JLabel("CANCEL", SwingConstants.CENTER);
        cancelLabel.setFont(new Font("Arial", Font.BOLD, 16));
        cancelLabel.setForeground(Color.WHITE);
        cancelCard.add(cancelLabel, BorderLayout.CENTER);
        
        cancelCard.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                cancelCard.setBackground(new Color(70, 70, 70));
            }
            public void mouseExited(MouseEvent e) {
                cancelCard.setBackground(new Color(50, 50, 50));
            }
            public void mouseClicked(MouseEvent e) {
                playSoundEffect("/javamon/assets/ButtonsFx.wav"); // <--- APPLIED SOUND EFFECT
                fightPanel.setVisible(false);
            }
        });
        
        fightPanel.add(cancelCard);
        
        fightPanel.revalidate();
        repaint();
    }

    // Helper method for type colors
    
    private JPanel createTypeChartPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(20, 20, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setBackground(new Color(20, 20, 30));
        
        String html = "<html><body style='font-family:Arial; color:white; font-size:14px; line-height:1.8;'>"
            + "<h2 style='color:#00FFFF; text-align:center;'>TYPE EFFECTIVENESS CHART</h2>"
            + "<div style='background-color:#1a1a25; padding:15px; margin:10px 0; border-left:4px solid #ff6666;'>"
            + "<b style='color:#ff6666; font-size:16px;'>🔥 FIRE</b><br/>"
            + "<span style='color:#00ff00;'>Strong vs:</span> Grass, Bug, Ice<br/>"
            + "<span style='color:#ff5555;'>Weak vs:</span> Water, Ground, Fire"
            + "</div>"
            + "<div style='background-color:#1a1a25; padding:15px; margin:10px 0; border-left:4px solid #6699ff;'>"
            + "<b style='color:#6699ff; font-size:16px;'>💧 WATER</b><br/>"
            + "<span style='color:#00ff00;'>Strong vs:</span> Fire, Ground<br/>"
            + "<span style='color:#ff5555;'>Weak vs:</span> Grass, Lightning, Water"
            + "</div>"
            + "<div style='background-color:#1a1a25; padding:15px; margin:10px 0; border-left:4px solid #66ff66;'>"
            + "<b style='color:#66ff66; font-size:16px;'>🌿 GRASS</b><br/>"
            + "<span style='color:#00ff00;'>Strong vs:</span> Water, Ground<br/>"
            + "<span style='color:#ff5555;'>Weak vs:</span> Fire, Flying, Bug, Grass"
            + "</div>"
            + "<div style='background-color:#1a1a25; padding:15px; margin:10px 0; border-left:4px solid #ffff66;'>"
            + "<b style='color:#ffff66; font-size:16px;'>⚡ LIGHTNING</b><br/>"
            + "<span style='color:#00ff00;'>Strong vs:</span> Water, Flying<br/>"
            + "<span style='color:#ff5555;'>Weak vs:</span> Ground, Lightning"
            + "</div>"
            + "<div style='background-color:#1a1a25; padding:15px; margin:10px 0; border-left:4px solid #cc8844;'>"
            + "<b style='color:#cc8844; font-size:16px;'>🪨 GROUND</b><br/>"
            + "<span style='color:#00ff00;'>Strong vs:</span> Fire, Lightning<br/>"
            + "<span style='color:#ff5555;'>Weak vs:</span> Grass, Ice, Water"
            + "</div>"
            + "<div style='background-color:#1a1a25; padding:15px; margin:10px 0; border-left:4px solid #66ddff;'>"
            + "<b style='color:#66ddff; font-size:16px;'>❄️ ICE</b><br/>"
            + "<span style='color:#00ff00;'>Strong vs:</span> Grass, Ground, Flying<br/>"
            + "<span style='color:#ff5555;'>Weak vs:</span> Fire, Fighting, Ice"
            + "</div>"
            + "</body></html>";
        
        textPane.setText(html);
        
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMechanicsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(20, 20, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setBackground(new Color(20, 20, 30));
        
        String html = "<html><body style='font-family:Arial; color:white; font-size:14px; line-height:1.8;'>"
            + "<h2 style='color:#00FFFF; text-align:center;'>⚙️ BATTLE MECHANICS</h2>"
            + "<div style='background-color:#1a1a25; padding:15px; margin:10px 0;'>"
            + "<h3 style='color:#ffd700;'>🎯 Turn Order</h3>"
            + "• <b>Priority Moves</b> (Buffs/Heals) always go first<br/>"
            + "• <b>Speed</b> determines who attacks first if same priority<br/>"
            + "• If speeds are equal, it's a 50/50 coin flip<br/>"
            + "• When switching, enemy might attack first if faster"
            + "</div>"
            + "<div style='background-color:#1a1a25; padding:15px; margin:10px 0;'>"
            + "<h3 style='color:#ffd700;'>💥 Damage Calculation</h3>"
            + "• <b>Super Effective:</b> 2x damage (green indicator ▲)<br/>"
            + "• <b>Not Very Effective:</b> 0.5x damage (red indicator ▼)<br/>"
            + "• <b>Critical Hits:</b> 1.5x damage + screen flash<br/>"
            + "• Attack and Defense stats affect final damage"
            + "</div>"
            + "<div style='background-color:#1a1a25; padding:15px; margin:10px 0;'>"
            + "<h3 style='color:#ffd700;'>📊 Stats Explained</h3>"
            + "• <b>HP:</b> Health Points - when it reaches 0, you faint<br/>"
            + "• <b>ATK:</b> Attack power - higher = more damage dealt<br/>"
            + "• <b>DEF:</b> Defense - higher = less damage taken<br/>"
            + "• <b>SPD:</b> Speed - determines turn order"
            + "</div>"
            + "<div style='background-color:#1a1a25; padding:15px; margin:10px 0;'>"
            + "<h3 style='color:#ffd700;'>⭐ Experience & Leveling</h3>"
            + "• Defeating enemies grants XP (30 × enemy level)<br/>"
            + "• Level up to increase all stats and restore HP<br/>"
            + "• XP bar fills up at the bottom of your monster's info"
            + "</div>"
            + "<div style='background-color:#1a1a25; padding:15px; margin:10px 0;'>"
            + "<h3 style='color:#ffd700;'>🏢 Floor Progression</h3>"
            + "• Clear all 3 enemies to advance to next floor<br/>"
            + "• Team fully heals between floors<br/>"
            + "• Enemy levels increase with each floor<br/>"
            + "• Game over if all your JavaMons faint"
            + "</div>"
            + "</body></html>";
        
        textPane.setText(html);
        
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTipsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(20, 20, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setBackground(new Color(20, 20, 30));
        
        String html = "<html><body style='font-family:Arial; color:white; font-size:14px; line-height:1.8;'>"
            + "<h2 style='color:#00FFFF; text-align:center;'>👤 TRAINER CLASSES</h2>"
            + "<div style='background-color:#1a1a25; padding:15px; margin:10px 0; border-left:4px solid #ff6666;'>"
            + "<b style='color:#ff6666; font-size:16px;'>🔥 ELEMENTALIST</b><br/>"
            + "Master of type advantages. Deals <b>1.5x more damage</b> with super-effective moves!<br/>"
            + "<span style='color:#ffd700;'>Class ULT:</span> Elemental Blast (varies by monster type)"
            + "</div>"
            + "<div style='background-color:#1a1a25; padding:15px; margin:10px 0; border-left:4px solid #6699ff;'>"
            + "<b style='color:#6699ff; font-size:16px;'>🧠 STRATEGIST</b><br/>"
            + "Tactical genius. Buff moves are <b>50% more effective</b>!<br/>"
            + "<span style='color:#ffd700;'>Class ULT:</span> Master Plan (powerful buff)"
            + "</div>"
            + "<div style='background-color:#1a1a25; padding:15px; margin:10px 0; border-left:4px solid #ff9900;'>"
            + "<b style='color:#ff9900; font-size:16px;'>⚔️ AGGRESSOR</b><br/>"
            + "All-out attacker. <b>Higher critical hit rate</b> for devastating strikes!<br/>"
            + "<span style='color:#ffd700;'>Class ULT:</span> All-In Strike (high crit chance)"
            + "</div>"
            + "<div style='background-color:#1a1a25; padding:15px; margin:10px 0; border-left:4px solid #66ff66;'>"
            + "<b style='color:#66ff66; font-size:16px;'>🐾 BEASTMASTER</b><br/>"
            + "Monster bond specialist. Team gets <b>bonus stats</b>!<br/>"
            + "<span style='color:#ffd700;'>Class ULT:</span> Primal Roar (team buff)"
            + "</div>"
            + "<h2 style='color:#00FFFF; text-align:center; margin-top:30px;'>💡 PRO TIPS</h2>"
            + "<div style='background-color:#1a1a25; padding:15px; margin:10px 0;'>"
            + "✅ <b>Switch strategically!</b> If your monster is weak to enemy's type, switch to gain advantage<br/><br/>"
            + "✅ <b>Use buffs early!</b> Buff moves go first and can turn the tide of battle<br/><br/>"
            + "✅ <b>Watch HP colors!</b> Green = healthy, Yellow = careful, Red = critical<br/><br/>"
            + "✅ <b>Save CLASS ULT!</b> Your 5th golden move is powerful - use it wisely<br/><br/>"
            + "✅ <b>Type advantage matters!</b> 2x damage can make or break a fight<br/><br/>"
            + "✅ <b>Speed ties!</b> Invest in speed to control turn order<br/><br/>"
            + "✅ <b>Hover for info!</b> Hover over monsters and moves to see detailed stats"
            + "</div>"
            + "</body></html>";
        
        textPane.setText(html);
        
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    private Color getTypeColor(String type) {
        switch(type) {
            case "Fire": return new Color(238, 129, 48);
            case "Water": return new Color(99, 144, 240);
            case "Grass": return new Color(122, 199, 76);
            case "Lightning": return new Color(247, 208, 44);
            case "Ice": return new Color(150, 217, 214);
            case "Ground": return new Color(226, 191, 101);
            case "Bug": return new Color(166, 185, 26);
            case "Dark": return new Color(112, 87, 70);
            case "Buff": return new Color(168, 167, 122);
            case "Healing": return new Color(255, 120, 180);
            default: return new Color(168, 167, 122);
        }
    }
    
    private Ability generateSecretAbility() {
        String name = "Secret";
        String type = activePlayerMon.getType();
        String c = trainerClass.toUpperCase();
        if (c.equals("ELEMENTALIST")) {
            if(type.equals("Water")) name = "Tsunami Surfer"; else if(type.equals("Fire")) name = "Spicy Meatball"; else name = "Elemental Blast";
        } else if (c.equals("STRATEGIST")) {
            name = "Master Plan"; type = "Buff";
        } else if (c.equals("AGGRESSOR")) {
            name = "All-In Strike";
        } else if (c.equals("BEASTMASTER")) {
            name = "Primal Roar"; type = "Buff";
        } else { 
            name = "Hex"; type = "Dark";
        }
        return new Ability(999, name, "Class Secret Skill", type);
    }
    
    private void showAbilityTooltip(Ability a, Component anchor, double mult) {
        // IMPORTANT: Clear and rebuild tooltip to prevent monster hover from persisting
        infoOverlay.removeAll();
        infoOverlay.setLayout(new BorderLayout(10, 5));
        infoOverlay.setPreferredSize(new Dimension(350, 180));
        
        // Title
        infoTitle = new JLabel(a.getName() + " (" + a.getType() + ")", SwingConstants.CENTER);
        infoTitle.setFont(new Font("Arial", Font.BOLD, 18));
        infoTitle.setForeground(NEON_CYAN);
        infoOverlay.add(infoTitle, BorderLayout.NORTH);
        
        // Body
        infoBody = new JTextPane();
        infoBody.setEditable(false);
        infoBody.setContentType("text/html");
        infoBody.setOpaque(false);
        
        String desc = "<b>Desc:</b> " + a.getDescription();
        String eff = "";
        if(a.getType().equals("Buff") || a.getType().equals("Healing")) {
            eff = "<br/><span style='color:#00FFFF'>Targets Self</span>";
        } else {
            String colorHex = (mult > 1.0) ? "#00FF00" : (mult < 1.0) ? "#FF5555" : "#FFFFFF";
            eff = "<br/><b>Vs " + activeEnemyMon.getName() + ":</b> <span style='color:" + colorHex + "'>x" + mult + " Dmg</span>";
        }
        
        String html = "<html><body style='font-family:Arial; color:white; font-size:12px; padding:5px;'>" 
                      + desc + "<br/>" + eff + "</body></html>";
        infoBody.setText(html);
        
        infoOverlay.add(infoBody, BorderLayout.CENTER);
        
        // Position
        Point loc = anchor.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(loc, layeredPane);
        int targetY = loc.y - 190;
        if(targetY < 0) targetY = loc.y + 80;
        
        infoOverlay.setBounds(loc.x + 20, targetY, 350, 180);
        infoOverlay.setVisible(true);
        infoOverlay.revalidate();
        infoOverlay.repaint();
    }
    
    private void showTypeTooltip(String type, Component anchor) {
        // Clear and rebuild tooltip
        infoOverlay.removeAll();
        infoOverlay.setLayout(new BorderLayout(10, 5));
        infoOverlay.setPreferredSize(new Dimension(350, 180));
        
        // Title
        infoTitle = new JLabel(type, SwingConstants.CENTER);
        infoTitle.setFont(new Font("Arial", Font.BOLD, 18));
        infoTitle.setForeground(NEON_CYAN);
        infoOverlay.add(infoTitle, BorderLayout.NORTH);
        
        // Body
        infoBody = new JTextPane();
        infoBody.setEditable(false);
        infoBody.setContentType("text/html");
        infoBody.setOpaque(false);
        
        String weakTo = "", strongVs = "";
        if(type.equals("Water")) { strongVs = "Fire, Ground"; weakTo = "Grass, Lightning"; }
        else if(type.equals("Fire")) { strongVs = "Grass, Bug, Ice"; weakTo = "Water, Ground"; }
        else if(type.equals("Grass")) { strongVs = "Water, Ground"; weakTo = "Fire, Flying"; }
        else if(type.equals("Lightning")) { strongVs = "Water, Flying"; weakTo = "Ground"; }
        else if(type.equals("Ground")) { strongVs = "Fire, Lightning"; weakTo = "Grass, Ice"; }
        else { strongVs = "???"; weakTo = "???"; }
        
        String html = "<html><body style='font-family:Arial; color:white; font-size:12px; padding:5px;'>" 
                      + "<span style='color:#00FF00'>Strong Vs:</span> " + strongVs + "<br/>" 
                      + "<span style='color:#FF5555'>Weak To:</span> " + weakTo + "</body></html>";
        infoBody.setText(html);
        
        infoOverlay.add(infoBody, BorderLayout.CENTER);
        
        // Position
        Point loc = anchor.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(loc, layeredPane);
        
        infoOverlay.setBounds(loc.x, loc.y + 30, 350, 180);
        infoOverlay.setVisible(true);
        infoOverlay.revalidate();
        infoOverlay.repaint();
    }
    
 // Replace the addMonsterHover method with this improved version:

    private void addMonsterHover(Component c, boolean isPlayer) {
        c.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                Monster m = isPlayer ? activePlayerMon : activeEnemyMon;
                showEnhancedMonsterTooltip(m, c);
            }
            public void mouseExited(MouseEvent e) { 
                infoOverlay.setVisible(false); 
            }
        });
    }

    private void showEnhancedMonsterTooltip(Monster m, Component anchor) {
        // Clear and rebuild tooltip
        infoOverlay.removeAll();
        infoOverlay.setLayout(new BorderLayout(0, 0));
        infoOverlay.setPreferredSize(new Dimension(380, 280));
        
        // Header Section with gradient background
        JPanel headerPanel = new JPanel(new BorderLayout(10, 5));
        headerPanel.setBackground(new Color(15, 15, 25, 250));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // Left: Monster Icon (small)
        JLabel miniIcon = new JLabel();
        if (m.getImage() != null) {
            ImageIcon icon = new ImageIcon(m.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));
            miniIcon.setIcon(icon);
        }
        headerPanel.add(miniIcon, BorderLayout.WEST);
        
        // Center: Name + Level + Type
        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(m.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 22));
        nameLabel.setForeground(NEON_CYAN);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel levelTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        levelTypePanel.setOpaque(false);
        
        JLabel levelLabel = new JLabel("Lv. " + m.getLevel());
        levelLabel.setFont(new Font("Arial", Font.BOLD, 14));
        levelLabel.setForeground(NEON_GOLD);
        
        JLabel typeBadge = new JLabel(m.getType().toUpperCase());
        typeBadge.setFont(new Font("Arial", Font.BOLD, 11));
        typeBadge.setForeground(Color.WHITE);
        typeBadge.setOpaque(true);
        typeBadge.setBackground(getTypeColorForTooltip(m.getType()));
        typeBadge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        
        levelTypePanel.add(levelLabel);
        levelTypePanel.add(typeBadge);
        
        namePanel.add(nameLabel);
        namePanel.add(Box.createVerticalStrut(5));
        namePanel.add(levelTypePanel);
        
        headerPanel.add(namePanel, BorderLayout.CENTER);
        
        infoOverlay.add(headerPanel, BorderLayout.NORTH);
        
        // Body Section: Stats Grid
        JPanel bodyPanel = new JPanel(new GridLayout(4, 1, 0, 8));
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // HP Stat
        bodyPanel.add(createStatRow("HP", m.getCurrentHP() + " / " + m.getMaxHP(), 
            NEON_GREEN, (double) m.getCurrentHP() / m.getMaxHP()));
        
        // ATK Stat
        bodyPanel.add(createStatRow("ATK", String.valueOf(m.getAttack()), 
            new Color(255, 100, 100), Math.min(1.0, m.getAttack() / 150.0)));
        
        // DEF Stat
        bodyPanel.add(createStatRow("DEF", String.valueOf(m.getDefense()), 
            new Color(100, 150, 255), Math.min(1.0, m.getDefense() / 150.0)));
        
        // SPD Stat
        bodyPanel.add(createStatRow("SPD", String.valueOf(m.getSpeed()), 
            new Color(255, 215, 0), Math.min(1.0, m.getSpeed() / 150.0)));
        
        infoOverlay.add(bodyPanel, BorderLayout.CENTER);
        
        // Footer: Status Effects
        if (!m.getActiveStatuses().isEmpty()) {
            JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            footerPanel.setOpaque(false);
            footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 50, 70)),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
            ));
            
            JLabel statusLabel = new JLabel("Status: ");
            statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
            statusLabel.setForeground(Color.LIGHT_GRAY);
            footerPanel.add(statusLabel);
            
            for (StatusEffect s : m.getActiveStatuses()) {
                JLabel statusIcon = new JLabel(s.name.toUpperCase());
                statusIcon.setFont(new Font("Arial", Font.BOLD, 10));
                statusIcon.setForeground(Color.WHITE);
                statusIcon.setOpaque(true);
                
                if (s.type.equals("DMG")) {
                    statusIcon.setBackground(new Color(255, 100, 0));
                } else if (s.type.equals("STOP")) {
                    statusIcon.setBackground(new Color(100, 100, 255));
                } else {
                    statusIcon.setBackground(new Color(50, 200, 50));
                }
                
                statusIcon.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
                footerPanel.add(statusIcon);
            }
            
            infoOverlay.add(footerPanel, BorderLayout.SOUTH);
        }
        
        // Position tooltip
        Point loc = anchor.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(loc, layeredPane);
        
        // Smart positioning (avoid going off-screen)
        int tooltipX = loc.x + 50;
        int tooltipY = loc.y;
        
        if (tooltipX + 380 > layeredPane.getWidth()) {
            tooltipX = loc.x - 400; // Show on left side
        }
        
        if (tooltipY + 280 > layeredPane.getHeight()) {
            tooltipY = layeredPane.getHeight() - 300;
        }
        
        infoOverlay.setBounds(tooltipX, tooltipY, 380, 280);
        infoOverlay.setVisible(true);
        infoOverlay.revalidate();
        infoOverlay.repaint();
    }

    private JPanel createStatRow(String statName, String statValue, Color barColor, double percentage) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        
        // Stat Label
        JLabel nameLabel = new JLabel(statName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 13));
        nameLabel.setForeground(Color.LIGHT_GRAY);
        nameLabel.setPreferredSize(new Dimension(45, 20));
        row.add(nameLabel, BorderLayout.WEST);
        
        // Stat Bar Container
        JPanel barContainer = new JPanel(new BorderLayout(5, 0));
        barContainer.setOpaque(false);
        
        // Progress Bar
        JPanel progressBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                // Background
                g2d.setColor(new Color(40, 40, 50));
                g2d.fillRoundRect(0, 0, w, h, h, h);
                
                // Fill
                int fillWidth = (int) (w * percentage);
                g2d.setColor(barColor);
                g2d.fillRoundRect(0, 0, fillWidth, h, h, h);
                
                // Highlight
                g2d.setColor(new Color(255, 255, 255, 40));
                g2d.fillRoundRect(0, 0, fillWidth, h / 2, h / 2, h / 2);
                
                // Border
                g2d.setColor(new Color(20, 20, 30));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, w - 1, h - 1, h, h);
            }
        };
        progressBar.setPreferredSize(new Dimension(180, 18));
        progressBar.setOpaque(false);
        
        barContainer.add(progressBar, BorderLayout.CENTER);
        
        // Value Label
        JLabel valueLabel = new JLabel(statValue);
        valueLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setPreferredSize(new Dimension(80, 20));
        barContainer.add(valueLabel, BorderLayout.EAST);
        
        row.add(barContainer, BorderLayout.CENTER);
        
        return row;
    }

    private Color getTypeColorForTooltip(String type) {
        switch(type) {
            case "Fire": return new Color(238, 129, 48);
            case "Water": return new Color(99, 144, 240);
            case "Grass": return new Color(122, 199, 76);
            case "Lightning": return new Color(247, 208, 44);
            case "Ice": return new Color(150, 217, 214);
            case "Ground": return new Color(226, 191, 101);
            case "Bug": return new Color(166, 185, 26);
            case "Dark": return new Color(112, 87, 70);
            default: return Color.GRAY;
        }
    }
    
    private int getMovePriority(Ability move) {
        String type = move.getType();
        
        if (type.equalsIgnoreCase("Healing") || type.equalsIgnoreCase("Buff")) {
            return 3; 
        }
        
        if (type.equalsIgnoreCase("Debuff") || move.getId() == 999) {
            return 2; 
        }
        
        return 1;
    }

    private void initiateCombatRound(Ability playerMove) {
        fightPanel.setVisible(false); 
        infoOverlay.setVisible(false); 
        isTurnInProgress = true;
        
        if(idleTimer1 != null) idleTimer1.stop();
        if(idleTimer2 != null) idleTimer2.stop();
        
        Ability enemyMove = activeEnemyMon.getAbilities().get((int)(Math.random() * 4)); 
        
        int playerPriority = getMovePriority(playerMove);
        int enemyPriority = getMovePriority(enemyMove);
        
        boolean playerFirst;
        
        if (playerPriority > enemyPriority) {
            playerFirst = true;
        } else if (enemyPriority > playerPriority) {
            playerFirst = false;
        } else {
            playerFirst = (activePlayerMon.getSpeed() >= activeEnemyMon.getSpeed());
        }
        
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
        
        if(getMovePriority(firstMove) >= 2) {
             FadableSprite sprite = isFirstPlayer ? jMon1Label : jMon2Label;
             animateChargeUp(sprite, () -> {
                 performAttackSequence(first, second, firstMove, isFirstPlayer, () -> {
                    if (second.isFainted()) {
                        if (isFirstPlayer) handleEnemyFaint(); else handlePlayerFaint();
                    } else {
                        triggerSecondHalf(second, first, secondMove, !isFirstPlayer);
                    }
                });
             });
        } else {
            performAttackSequence(first, second, firstMove, isFirstPlayer, () -> {
                if (second.isFainted()) {
                    if (isFirstPlayer) handleEnemyFaint(); else handlePlayerFaint();
                } else {
                    triggerSecondHalf(second, first, secondMove, !isFirstPlayer);
                }
            });
        }
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
        
        if(getMovePriority(move) >= 2) {
             FadableSprite sprite = isPlayer ? jMon1Label : jMon2Label;
             animateChargeUp(sprite, () -> performAttackSequence(attacker, defender, move, isPlayer, () -> endTurn()));
        } else {
            performAttackSequence(attacker, defender, move, isPlayer, () -> endTurn());
        }
    }

    private void endTurn() {
        if (activePlayerMon.isFainted()) { handlePlayerFaint(); return; }
        if (activeEnemyMon.isFainted()) { handleEnemyFaint(); return; }
        
        StringBuilder log = new StringBuilder();
        int pDmg = activePlayerMon.processEndOfTurn(log);
        int eDmg = activeEnemyMon.processEndOfTurn(log);
        
        if(pDmg > 0 || eDmg > 0) {
            hpBar1.slideHealth(activePlayerMon.getCurrentHP(), activePlayerMon.getMaxHP(), null);
            hpBar2.slideHealth(activeEnemyMon.getCurrentHP(), activeEnemyMon.getMaxHP(), () -> {
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
        
        startIdleAnimation(jMon1Label, PLAYER_Y, true);
        startIdleAnimation(jMon2Label, ENEMY_Y, false);
    }
    
    // 4. UPDATED performAttackSequence (to use optimized effects)
    private void performAttackSequence(Monster attacker, Monster defender, Ability move, boolean isPlayer, Runnable onComplete) {
        FadableSprite atkLabel = isPlayer ? jMon1Label : jMon2Label;
        FadableSprite defLabel = isPlayer ? jMon2Label : jMon1Label;
        SmoothBatteryBar atkBar = isPlayer ? hpBar1 : hpBar2;
        SmoothBatteryBar defBar = isPlayer ? hpBar2 : hpBar1;
        
        boolean isSupport = move.getType().equals("Buff") || move.getType().equals("Healing");
        
        txtWhat.setText(attacker.getName() + " used " + move.getName() + "!");
        
        if (isSupport) {
            Timer t = new Timer(500, e -> {
                int preHp = attacker.getCurrentHP();
                String log = AbilityLogic.execute(move, attacker, defender, null, isPlayer ? trainerClass : "ENEMY");
                
                showEnhancedFloatingText("BUFF!", NEON_CYAN, atkLabel, false);
                
                // Option A: Optimized particle burst (Default choice as per request)
                animateStatBurst(atkLabel);
                
                // Option B: Glow effect (pure glow - LIGHTEST)
                // animateHealGlow(atkLabel);
                
                int healedAmt = attacker.getCurrentHP() - preHp;
                if(healedAmt > 0) showEnhancedFloatingText("+" + healedAmt, NEON_GREEN, atkLabel, true);

                atkBar.slideHealth(attacker.getCurrentHP(), attacker.getMaxHP(), () -> {
                    txtWhat.setText(log);
                    updateBattleState();
                    Timer p = new Timer(1000, evt -> onComplete.run()); p.setRepeats(false); p.start();
                });
            });
            t.setRepeats(false); t.start();
            return;
        }
        
        

        int direction = isPlayer ? 1 : -1;
        int originalX = isPlayer ? PLAYER_X : ENEMY_X;
        int originalY = isPlayer ? PLAYER_Y : ENEMY_Y;
        
        Timer windUp = new Timer(15, null);
        final int[] windUpFrame = {0};
        windUp.addActionListener(e -> {
            if(windUpFrame[0] < 8) {
                atkLabel.setLocation(originalX - (direction * 3 * windUpFrame[0]), originalY);
            }
            if(windUpFrame[0] >= 8) {
                windUp.stop();
                performLunge(atkLabel, defLabel, attacker, defender, move, isPlayer, 
                            originalX, originalY, direction, atkBar, defBar, onComplete);
            }
            windUpFrame[0]++;
        });
        windUp.start();
    }
    
    private void performLunge(FadableSprite atkLabel, FadableSprite defLabel, Monster attacker, 
            Monster defender, Ability move, boolean isPlayer,
            int originalX, int originalY, int direction,
            SmoothBatteryBar atkBar, SmoothBatteryBar defBar, Runnable onComplete) {

        Timer lungeOut = new Timer(10, null);
        final int[] speed = {10};

        lungeOut.addActionListener(e -> {
            atkLabel.setLocation(atkLabel.getX() + (speed[0] * direction), atkLabel.getY() - 1);
            speed[0] = Math.max(3, speed[0] - 1);

            if (Math.abs(atkLabel.getX() - originalX) > 80) {
                lungeOut.stop();

                spawnTypeEffect(move.getType(), defLabel);

                int preDefHp = defender.getCurrentHP();
                int preAtkHp = attacker.getCurrentHP(); 

                String log = AbilityLogic.execute(move, attacker, defender, 
                                                isPlayer ? playerTeam : enemyTeam, 
                                                isPlayer ? trainerClass : "ENEMY");

                int damageTaken = preDefHp - defender.getCurrentHP();
                int selfHeal = attacker.getCurrentHP() - preAtkHp;
                int recoil = preAtkHp - attacker.getCurrentHP();
                
                if(damageTaken == 0 && log.contains("missed")) {
                    animateDodge(defLabel);
                } else {
                    if (log.contains("[CRIT]")) { 
                       flashScreen();
                       
                       // NEW: CRIT ZOOM EFFECT
                       Timer critZoom = new Timer(20, null);
                       final int[] frame = {0};
                       final Point orig = defLabel.getLocation();
                       critZoom.addActionListener(ev -> {
                           if (frame[0] < 3) {
                               defLabel.setLocation(orig.x - 15, orig.y - 15);
                           } else if (frame[0] < 6) {
                               defLabel.setLocation(orig.x + 10, orig.y + 10);
                           } else {
                               critZoom.stop();
                               defLabel.setLocation(orig);
                           }
                           frame[0]++;
                           scheduleRepaint(defLabel);
                       });
                       critZoom.start();

                       multiShake(defLabel, 12, SHAKE_INTENSITY); 
                       animateHitFlash(defLabel, new Color(255, 255, 0, 200));
                    } 
                    else if (damageTaken > 0) { 
                       animateHitFlash(defLabel, new Color(255, 0, 0, 150)); 
                       multiShake(defLabel, 8, SHAKE_INTENSITY); 
                    }

                    if (damageTaken > 0) {
                       showEnhancedDamageText("-"+damageTaken, DAMAGE_RED, defLabel, log.contains("[CRIT]"));
                    } 
                    if (recoil > 0) {
                       showEnhancedDamageText("-"+recoil, Color.ORANGE, atkLabel, false);
                    }
                }
                
                Timer bounceBack = new Timer(8, null);
                final int[] bounceSpeed = {3};

                bounceBack.addActionListener(ev -> {
                   atkLabel.setLocation(atkLabel.getX() - (bounceSpeed[0] * direction), 
                                       atkLabel.getY() + 1);
                   bounceSpeed[0] = Math.min(12, bounceSpeed[0] + 1); 
                   
                   if ((isPlayer && atkLabel.getX() <= originalX) || 
                       (!isPlayer && atkLabel.getX() >= originalX)) {
                       bounceBack.stop(); 
                       atkLabel.setLocation(originalX, originalY);
                       
                       animateSettle(atkLabel, originalX, originalY, () -> {
                           txtJLives1.setText(activePlayerMon.getCurrentHP() + " / " + activePlayerMon.getMaxHP());
                           txtJLives2.setText(activeEnemyMon.getCurrentHP() + " /  " + activeEnemyMon.getMaxHP());

                           Runnable afterHeal = () -> {
                               defBar.slideHealth(defender.getCurrentHP(), defender.getMaxHP(), () -> {
                                   txtWhat.setText(log.replace("[CRIT] ", ""));
                                   updateBattleState(); 
                                   Timer pause = new Timer(1000, evt -> { if (onComplete != null) onComplete.run(); });
                                   pause.setRepeats(false); pause.start();
                               });
                           };
                           
                           if(selfHeal != 0 || recoil > 0) {
                               atkBar.slideHealth(attacker.getCurrentHP(), attacker.getMaxHP(), afterHeal);
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
    
    // START OF OPTIMIZATION FIXES (multiShake)
    private void multiShake(JComponent c, int duration, int intensity) {
        final Point original = c.getLocation();
        final Timer shakeTimer = new Timer(50, null);  
        final int[] count = {0};
        
        shakeTimer.addActionListener(e -> {
            int offsetX = (int)(Math.sin(count[0] * 0.8) * intensity) - (intensity/2);
            int offsetY = (int)(Math.cos(count[0] * 0.6) * intensity/2);
            c.setLocation(original.x + offsetX, original.y + offsetY);
            
            if (++count[0] > duration/2) {  
                shakeTimer.stop(); 
                c.setLocation(original); 
            }
            scheduleRepaint(c);
        }); 
        shakeTimer.start();
    }
    // END OF OPTIMIZATION FIXES (multiShake)

    // 2. ULTRA-OPTIMIZED animateStatBurst (Single Timer)
    private void animateStatBurst(JComponent target) {
        Point p = target.getLocation();
        
        // Prepare 4 particles (was 6)
        List<JLabel> activeParticles = new ArrayList<>();
        List<Double> angles = new ArrayList<>();
        
        for(int i = 0; i < 4; i++) { // Reduced from 6
            if (particlePool.isEmpty()) break;
            
            JLabel particle = particlePool.remove(0);
            particle.setText("★");
            particle.setFont(new Font("Arial", Font.BOLD, 18)); // Smaller font
            particle.setForeground(NEON_CYAN);
            particle.setBounds(p.x + 150, p.y + 150, 25, 25);
            particle.setVisible(true);
            layeredPane.add(particle, JLayeredPane.POPUP_LAYER);
            
            activeParticles.add(particle);
            angles.add((Math.PI * 2 * i) / 4);
        }
        
        // SINGLE TIMER for all particles
        final int[] life = {0};
        Timer burst = new Timer(50, null); // Slower timer (was 40)
        
        burst.addActionListener(e -> {
            life[0]++;
            int distance = life[0] * 7; // Faster movement
            
            // Update ALL particles in one timer tick
            for (int i = 0; i < activeParticles.size(); i++) {
                JLabel particle = activeParticles.get(i);
                double angle = angles.get(i);
                
                int newX = p.x + 150 + (int)(Math.cos(angle) * distance);
                int newY = p.y + 150 + (int)(Math.sin(angle) * distance);
                particle.setLocation(newX, newY);
                
                int alpha = Math.max(0, 255 - (life[0] * 25)); // Faster fade
                particle.setForeground(new Color(0, 255, 255, alpha));
            }
            
            // Clean up when done
            if(life[0] > 10) { // Reduced from 12
                burst.stop();
                for (JLabel particle : activeParticles) {
                    particle.setVisible(false);
                    particlePool.add(particle);
                }
                scheduleRepaint();
            } else {
                scheduleRepaint(); // Repaint on each tick to update positions/alpha
            }
        });
        burst.start();
    }
    // END OF OPTIMIZATION FIXES (animateStatBurst)
    
    // 3. Lightweight Heal Glow Effect (Alternative to particles)
    private void animateHealGlow(JComponent target) {
        final float[] glowAlpha = {0.8f};
        
        JPanel glow = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Point2D center = new Point2D.Float(getWidth()/2f, getHeight()/2f);
                float radius = Math.max(getWidth(), getHeight()) / 2f;
                
                RadialGradientPaint gradient = new RadialGradientPaint(
                    center, radius,
                    new float[]{0f, 0.5f, 1f},
                    new Color[]{
                        new Color(0, 255, 255, (int)(glowAlpha[0] * 180)),
                        new Color(0, 255, 255, (int)(glowAlpha[0] * 100)),
                        new Color(0, 255, 255, 0)
                    }
                );
                
                g2d.setPaint(gradient);
                g2d.fillOval(0, 0, getWidth(), getHeight());
            }
        };
        
        Point p = target.getLocation();
        glow.setOpaque(false);
        glow.setBounds(p.x + 50, p.y + 50, 280, 280);
        layeredPane.add(glow, JLayeredPane.POPUP_LAYER);
        
        Timer pulse = new Timer(60, null);
        final int[] frame = {0};
        
        pulse.addActionListener(e -> {
            frame[0]++;
            glowAlpha[0] = (float)(0.8 - (frame[0] * 0.1));
            glow.repaint();
            
            if (frame[0] > 8) {
                pulse.stop();
                layeredPane.remove(glow);
                scheduleRepaint();
            }
        });
        pulse.start();
    }

    private void animateSettle(FadableSprite sprite, int targetX, int targetY, Runnable onComplete) {
        final int[] bounceFrame = {0};
        final int[] yOffset = {0};
        Timer settle = new Timer(20, null);
        settle.addActionListener(e -> {
            bounceFrame[0]++;
            if(bounceFrame[0] < 5) yOffset[0] = 3; else if(bounceFrame[0] < 8) yOffset[0] = -2; else if(bounceFrame[0] < 10) yOffset[0] = 1; else {
                yOffset[0] = 0; settle.stop(); sprite.setLocation(targetX, targetY);
                if(onComplete != null) onComplete.run(); return;
            }
            sprite.setLocation(targetX, targetY + yOffset[0]);
            scheduleRepaint(sprite);
        }); settle.start();
    }

    private void showEnhancedFloatingText(String text, Color color, JComponent target, boolean isHeal) {
        JLabel floatLbl = new JLabel(text, SwingConstants.CENTER); 
        floatLbl.setFont(new Font("Arial", Font.BOLD, 32)); floatLbl.setForeground(color);
        Point p = target.getLocation(); floatLbl.setBounds(p.x + 80, p.y + 100, 200, 50); 
        layeredPane.add(floatLbl, JLayeredPane.POPUP_LAYER);
        final int[] alpha = {255}; final int[] frames = {0};
        Timer floatTimer = new Timer(30, null);
        floatTimer.addActionListener(e -> {
            frames[0]++;
            floatLbl.setLocation(floatLbl.getX() + (isHeal ? 1 : 0), floatLbl.getY() - 2);
            if(frames[0] > 15) {
                alpha[0] -= 15;
                floatLbl.setForeground(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, alpha[0])));
            }
            if (alpha[0] <= 0) { floatTimer.stop(); layeredPane.remove(floatLbl); scheduleRepaint(); }
            scheduleRepaint(floatLbl);
        }); floatTimer.start();
    }

    private void showEnhancedDamageText(String text, Color color, JComponent target, boolean isCrit) {
        JLabel floatLbl = new JLabel(text, SwingConstants.CENTER); 
        floatLbl.setFont(isCrit ? DMG_FONT.deriveFont(60f) : DMG_FONT); floatLbl.setForeground(color);
        if(isCrit) floatLbl.setBorder(BorderFactory.createLineBorder(NEON_GOLD, 2));
        Point p = target.getLocation(); floatLbl.setBounds(p.x + 50, p.y + 50, 250, 80); 
        layeredPane.add(floatLbl, JLayeredPane.POPUP_LAYER);
        final float[] scale = {0.5f}; final int[] alpha = {255}; final int[] frames = {0};
        Timer floatTimer = new Timer(25, null);
        floatTimer.addActionListener(e -> {
            frames[0]++;
            if(frames[0] < 8) { scale[0] += 0.1f; floatLbl.setFont(floatLbl.getFont().deriveFont(DMG_FONT.getSize() * scale[0])); }
            else if(frames[0] < 20) { floatLbl.setLocation(floatLbl.getX(), floatLbl.getY() - 1); }
            else {
                floatLbl.setLocation(floatLbl.getX(), floatLbl.getY() - 3); alpha[0] -= 12;
                floatLbl.setForeground(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, alpha[0])));
            }
            if (alpha[0] <= 0) { floatTimer.stop(); layeredPane.remove(floatLbl); scheduleRepaint(); }
            scheduleRepaint(floatLbl);
        }); floatTimer.start();
    }
    
    private void animateHitFlash(FadableSprite sprite, Color flashColor) { sprite.flash(flashColor); }
    
    private void flashScreen() {
        flashPanel.setVisible(true);
        final int[] pulses = {0};
        Timer t = new Timer(60, null);
        t.addActionListener(e -> {
            pulses[0]++; flashPanel.setVisible(!flashPanel.isVisible());
            if(pulses[0] >= 4) { flashPanel.setVisible(false); t.stop(); }
            scheduleRepaint(flashPanel);
        }); t.start();
    }

    private void animateSwitchIn(FadableSprite sprite, int targetX, Runnable onComplete) {
        sprite.setAlpha(0f); sprite.setLocation(targetX - 100, sprite.getY()); 
        Timer slideIn = new Timer(15, null);
        final float[] alpha = {0f};
        slideIn.addActionListener(e -> {
            sprite.setLocation(sprite.getX() + 8, sprite.getY());
            alpha[0] += 0.08f; sprite.setAlpha(Math.min(1.0f, alpha[0]));
            if (sprite.getX() >= targetX) { 
                sprite.setLocation(targetX, sprite.getY()); sprite.setAlpha(1.0f);
                slideIn.stop(); animateSettle(sprite, targetX, sprite.getY(), onComplete);
            }
            scheduleRepaint(sprite);
        }); slideIn.start();
    }
    private void animateFaint(FadableSprite sprite, Runnable onComplete) {
        Timer sink = new Timer(20, null); 
        final float[] opacity = {1.0f};
        final int[] rotation = {0};
        final Point original = sprite.getLocation();
        sink.addActionListener(e -> {
            sprite.setLocation(sprite.getX(), sprite.getY() + 4); 
            opacity[0] -= 0.03f; sprite.setAlpha(opacity[0]);
            rotation[0] += 5;
            int wobble = (int)(Math.sin(rotation[0] * 0.1) * 10);
            sprite.setLocation(sprite.getX() + wobble, sprite.getY());
            if (opacity[0] <= 0.05f) { sink.stop(); sprite.setAlpha(0f); sprite.setLocation(original); onComplete.run(); }
            scheduleRepaint(sprite);
        }); sink.start();
    }
    private void animateSwitchOut(FadableSprite sprite, Runnable onComplete) {
        Timer slideOut = new Timer(5, null);
        slideOut.addActionListener(e -> {
            sprite.setLocation(sprite.getX() - 30, sprite.getY());
            if (sprite.getX() < -400) { slideOut.stop(); onComplete.run(); }
            scheduleRepaint(sprite);
        }); slideOut.start();
    }

    private void handleEnemyFaint() {
        animateFaint(jMon2Label, () -> {
            jMon2Label.setVisible(false); // HIDE ENEMY TO PREVENT POP-UP
            
            int xpGained = 30 * activeEnemyMon.getLevel();
            boolean leveledUp = activePlayerMon.gainXP(xpGained);
            
            animateXPGain(xpGained);
            showEnhancedFloatingText("+" + xpGained + " XP", Color.YELLOW, jMon1Label, true);
            
            // Only update player part of UI to avoid resetting enemy state
            float xpFraction = (float)activePlayerMon.getXP() / activePlayerMon.getXpToNextLevel();
            xpBar1.setXP(xpFraction);

            Timer xpPause = new Timer(2000, e -> {
                if(leveledUp) {
                    showEnhancedFloatingText("LEVEL UP!", NEON_GOLD, jMon1Label, false);
                    animateStatBurst(jMon1Label);
                    hpBar1.snapHealth(activePlayerMon.getCurrentHP(), activePlayerMon.getMaxHP()); 
                    
                    lblP1Level.setText("Lvl " + activePlayerMon.getLevel());
                    txtJLives1.setText(activePlayerMon.getCurrentHP() + " / " + activePlayerMon.getMaxHP());
                    
                    Timer lvlPause = new Timer(1500, evt -> continueAfterXP());
                    lvlPause.setRepeats(false); lvlPause.start();
                } else {
                    continueAfterXP();
                }
            });
            xpPause.setRepeats(false);
            xpPause.start();
        });
    }

    private void continueAfterXP() {
        Monster nextEnemy = null;
        for(Monster m : enemyTeam) if(!m.isFainted()) { nextEnemy = m; break; }
        
        if(nextEnemy != null) {
            activeEnemyMon = nextEnemy; 
            
            Timer switchT = new Timer(100, e -> {
                updateBattleState(); // Fully refresh UI for new monster
                
                txtWhat.setText("Enemy sends out " + activeEnemyMon.getName() + "!");
                animateTurnBanner("ENEMY TURN", DAMAGE_RED);
                
                // USE NEW ENCOUNTER ANIMATION HERE
                wildEncounterAnimation(() -> {
                     Timer ready = new Timer(1000, evt -> {
                        isTurnInProgress = false;
                        startIdleAnimation(jMon1Label, PLAYER_Y, true);
                        startIdleAnimation(jMon2Label, ENEMY_Y, false);
                    });
                    ready.setRepeats(false); 
                    ready.start();
                });
            });
            switchT.setRepeats(false); switchT.start();
        } else { 
            Timer floorT = new Timer(1000, e -> handleFloorClear());
            floorT.setRepeats(false); floorT.start();
        }
    }

    private void handleFloorClear() {
        // --- SOUND EFFECT FOR SUCCESS ---
        playSoundEffect("/javamon/assets/floorclear.wav"); 
        
        JOptionPane.showMessageDialog(this, "FLOOR " + currentFloor + " CLEARED!\nTeam Healed.");
        for(Monster m : playerTeam) m.setCurrentHP(m.getMaxHP()); 
        
        currentFloor++; 
        txtFloor.setText("FLOOR " + currentFloor);
        
        // NEW: Show Floor Banner
        animateTurnBanner("FLOOR " + currentFloor, NEON_GOLD);
        
        enemyTeam.clear(); 
        List<Monster> pool = new ArrayList<>(masterMonsterPool); 
        java.util.Collections.shuffle(pool);
        for(int i=0; i<3 && i<pool.size(); i++) {
            Monster enemy = pool.get(i).copy();
         // Progressive enemy scaling: starts at floor level, adds small random variance
            int baseLevel = currentFloor;
            int variance = (currentFloor <= 3) ? 0 : (int)(Math.random() * 2); // No variance early game
            enemy.setLevel(baseLevel + variance);
            enemyTeam.add(enemy);
        }
        
        activeEnemyMon = enemyTeam.get(0); 
        updateBattleState();
        
        // Wait for floor banner to finish before showing wild encounter
        Timer bannerDelay = new Timer(2500, e -> {
            txtWhat.setText("Wild " + activeEnemyMon.getName() + " appeared!");
            animateTurnBanner("PLAYER TURN", NEON_CYAN);
            
            // USE NEW ENCOUNTER ANIMATION HERE INSTEAD OF animateBattleStart
            wildEncounterAnimation(null);
            
            isTurnInProgress = false;
        });
        bannerDelay.setRepeats(false);
        bannerDelay.start();
    }
    
    private void handlePlayerFaint() {
        animateFaint(jMon1Label, () -> {
            txtWhat.setText(activePlayerMon.getName() + " fainted!");
            boolean lost = true;
            for (Monster m : playerTeam) {
                if (!m.isFainted()) { lost = false; break; }
            }

            if (lost) {
                // Stop battle music
                soundManager.stopGameMusic();
                
                SwingUtilities.invokeLater(() -> {
                    GameOver go = new GameOver(currentFloor, () -> {
                        SwingUtilities.invokeLater(() -> {
                            try {
                                new MainMenu().setVisible(true);
                            } catch (Throwable t) { System.out.println("MainMenu class not found."); }
                        });
                    });
                    go.setVisible(true);
                });
                SwingUtilities.invokeLater(() -> GameWindow.this.dispose());
            } else {
                updateSwitchPanel();
                showOverlay(switchPanel);
                isTurnInProgress = false;
            }
        });
    }

    // NEW: POKEMON STYLE SWITCH ANIMATION
    private void pokemonStyleSwitch(Monster newMon, Runnable onComplete) {
        isTurnInProgress = true;
        switchPanel.setVisible(false);
        
        txtWhat.setText("Come back, " + activePlayerMon.getName() + "!");
        
        Timer recall = new Timer(30, null);
        final float[] scale = {1.0f};
        final Point center = new Point(jMon1Label.getX() + 192, jMon1Label.getY() + 172); // Fixed center calculation
        final int originalWidth = jMon1Label.getWidth();
        final int originalHeight = jMon1Label.getHeight();
        
        recall.addActionListener(e -> {
            scale[0] -= 0.08f;
            jMon1Label.setAlpha(scale[0]);
            
            int newWidth = (int)(originalWidth * scale[0]);
            int newHeight = (int)(originalHeight * scale[0]);
            jMon1Label.setSize(newWidth, newHeight);
            jMon1Label.setLocation(center.x - newWidth/2, center.y - newHeight/2);
            
            if (scale[0] <= 0.1f) {
                recall.stop();
                jMon1Label.setAlpha(0f);
                jMon1Label.setSize(originalWidth, originalHeight); // Use saved dimensions
                jMon1Label.setLocation(PLAYER_X, PLAYER_Y);
                
                Timer pause = new Timer(300, evt -> {
                    activePlayerMon = newMon;
                    updateBattleState();
                    txtWhat.setText("Go! " + newMon.getName() + "!");
                    
                    jMon1Label.setLocation(center.x - 5, center.y - 5);
                    jMon1Label.setSize(10, 10);
                    jMon1Label.setAlpha(0.3f);
                    jMon1Label.setVisible(true);
                    
                    Timer sendOut = new Timer(25, null);
                    final float[] growScale = {0.1f};
                    
                    sendOut.addActionListener(ev -> {
                        growScale[0] += 0.1f;
                        jMon1Label.setAlpha(Math.min(1.0f, growScale[0]));
                        
                        int size = (int)(originalWidth * Math.min(1.0f, growScale[0]));
                        int sizeH = (int)(originalHeight * Math.min(1.0f, growScale[0]));
                        jMon1Label.setSize(size, sizeH);
                        jMon1Label.setLocation(
                            PLAYER_X + (originalWidth - size)/2,
                            PLAYER_Y + (originalHeight - sizeH)/2
                        );
                        
                        if (growScale[0] >= 1.0f) {
                            sendOut.stop();
                            jMon1Label.setSize(originalWidth, originalHeight);
                            jMon1Label.setLocation(PLAYER_X, PLAYER_Y);
                            jMon1Label.setAlpha(1.0f);
                            animateSettle(jMon1Label, PLAYER_X, PLAYER_Y, onComplete);
                        }
                        scheduleRepaint(jMon1Label);
                    });
                    sendOut.start();
                });
                pause.setRepeats(false);
                pause.start();
            }
            scheduleRepaint(jMon1Label);
        });
        recall.start();
    }

    private void updateSwitchPanel() {
        switchPanel.removeAll();
        switchPanel.setLayout(new BorderLayout());
        
        // Create main container with padding
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setOpaque(false);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        
        // Title Section
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        
        JLabel title = new JLabel("CHOOSE YOUR JAVAMON");
        title.setFont(new Font("Impact", Font.BOLD, 48));
        title.setForeground(NEON_CYAN);
        titlePanel.add(title);
        
        mainContainer.add(titlePanel);
        mainContainer.add(Box.createVerticalStrut(30));
        
        // Cards Container with proper centering
        JPanel cardsContainer = new JPanel(new GridBagLayout());
        cardsContainer.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 15, 0, 15); // Horizontal spacing between cards
        
        for (Monster m : playerTeam) {
            JPanel card = createMonsterCard(m);
            cardsContainer.add(card, gbc);
            gbc.gridx++;
        }
        
        mainContainer.add(cardsContainer);
        mainContainer.add(Box.createVerticalStrut(30));
        
        // Close Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        
        JButton closeBtn = new JButton("BACK");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 20));
        closeBtn.setPreferredSize(new Dimension(200, 50));
        closeBtn.setBackground(new Color(80, 80, 80));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 2),
            BorderFactory.createEmptyBorder(10, 30, 10, 30)
        ));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                closeBtn.setBackground(new Color(100, 100, 100));
            }
            public void mouseExited(MouseEvent e) {
                closeBtn.setBackground(new Color(80, 80, 80));
            }
        });
        
        closeBtn.addActionListener(e -> {
            playSoundEffect("/javamon/assets/ButtonsFx.wav");
            switchPanel.setVisible(false);
        });
        
        buttonPanel.add(closeBtn);
        mainContainer.add(buttonPanel);
        
        switchPanel.add(mainContainer, BorderLayout.CENTER);
        switchPanel.revalidate();
        switchPanel.repaint();
    }

    private JPanel createMonsterCard(Monster m) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 10));
        card.setPreferredSize(new Dimension(280, 380));
        card.setMinimumSize(new Dimension(280, 380));
        card.setMaximumSize(new Dimension(280, 380));
        
        // Determine card state
        boolean isActive = (m == activePlayerMon);
        boolean isFainted = m.isFainted();
        boolean isAvailable = !isActive && !isFainted;
        
        // Card background and border
        if (isFainted) {
            card.setBackground(new Color(40, 30, 30, 240));
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 50, 50), 3),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
        } else if (isActive) {
            card.setBackground(new Color(30, 50, 60, 240));
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(NEON_CYAN, 4),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
        } else {
            card.setBackground(DARK_CARD_BG);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 3),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
        }
        
        // Top Section: Status Badge
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setOpaque(false);
        
        JLabel statusBadge = new JLabel();
        statusBadge.setFont(new Font("Arial", Font.BOLD, 12));
        statusBadge.setOpaque(true);
        statusBadge.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        
        if (isFainted) {
            statusBadge.setText("FAINTED");
            statusBadge.setBackground(new Color(200, 50, 50));
            statusBadge.setForeground(Color.WHITE);
        } else if (isActive) {
            statusBadge.setText("IN BATTLE");
            statusBadge.setBackground(NEON_CYAN);
            statusBadge.setForeground(Color.BLACK);
        } else {
            statusBadge.setText("READY");
            statusBadge.setBackground(NEON_GREEN);
            statusBadge.setForeground(Color.BLACK);
        }
        
        topPanel.add(statusBadge);
        card.add(topPanel, BorderLayout.NORTH);
        
        // Center Section: Image + Info
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        
        // Monster Image
        JLabel iconLabel = new JLabel();
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (m.getImage() != null) {
            ImageIcon scaledIcon = new ImageIcon(
                m.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH)
            );
            iconLabel.setIcon(scaledIcon);
        }
        centerPanel.add(iconLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        
        // Name
        JLabel nameLabel = new JLabel(m.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        nameLabel.setForeground(isFainted ? Color.GRAY : Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(nameLabel);
        centerPanel.add(Box.createVerticalStrut(5));
        
        // Level
        JLabel levelLabel = new JLabel("Lv. " + m.getLevel());
        levelLabel.setFont(new Font("Arial", Font.BOLD, 18));
        levelLabel.setForeground(NEON_GOLD);
        levelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(levelLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        
        // HP Bar Container
        JPanel hpContainer = new JPanel();
        hpContainer.setLayout(new BoxLayout(hpContainer, BoxLayout.Y_AXIS));
        hpContainer.setOpaque(false);
        hpContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel hpLabel = new JLabel("HP");
        hpLabel.setFont(new Font("Arial", Font.BOLD, 12));
        hpLabel.setForeground(Color.LIGHT_GRAY);
        hpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        hpContainer.add(hpLabel);
        hpContainer.add(Box.createVerticalStrut(3));
        
        // HP Bar
        JPanel hpBarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                
                // Background
                g2d.setColor(new Color(50, 50, 50));
                g2d.fillRoundRect(0, 0, width, height, height, height);
                
                // HP Fill
                double hpPercent = (double) m.getCurrentHP() / m.getMaxHP();
                int fillWidth = (int) (width * hpPercent);
                
                Color hpColor;
                if (hpPercent > 0.5) {
                    hpColor = new Color(80, 220, 80);
                } else if (hpPercent > 0.2) {
                    hpColor = new Color(255, 200, 50);
                } else {
                    hpColor = new Color(255, 80, 80);
                }
                
                g2d.setColor(hpColor);
                g2d.fillRoundRect(0, 0, fillWidth, height, height, height);
                
                // Border
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, width - 1, height - 1, height, height);
            }
        };
        hpBarPanel.setPreferredSize(new Dimension(200, 20));
        hpBarPanel.setMaximumSize(new Dimension(200, 20));
        hpBarPanel.setOpaque(false);
        hpContainer.add(hpBarPanel);
        hpContainer.add(Box.createVerticalStrut(3));
        
        // HP Text
        JLabel hpText = new JLabel(m.getCurrentHP() + " / " + m.getMaxHP());
        hpText.setFont(new Font("Monospaced", Font.BOLD, 16));
        hpText.setForeground(isFainted ? Color.RED : NEON_GREEN);
        hpText.setAlignmentX(Component.CENTER_ALIGNMENT);
        hpContainer.add(hpText);
        
        centerPanel.add(hpContainer);
        card.add(centerPanel, BorderLayout.CENTER);
        
        // Interaction
        if (isAvailable) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            card.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    card.setBackground(new Color(50, 50, 60, 240));
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(NEON_CYAN, 3),
                        BorderFactory.createEmptyBorder(15, 15, 15, 15)
                    ));
                }
                
                public void mouseExited(MouseEvent e) {
                    card.setBackground(DARK_CARD_BG);
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(100, 100, 100), 3),
                        BorderFactory.createEmptyBorder(15, 15, 15, 15)
                    ));
                }
                
                public void mouseClicked(MouseEvent e) {
                    playSoundEffect("/javamon/assets/ButtonsFx.wav");
                    
                    pokemonStyleSwitch(m, () -> {
                        Ability enemyMove = activeEnemyMon.getAbilities().get((int)(Math.random() * 4));
                        
                        boolean enemyFirst;
                        if (activeEnemyMon.getSpeed() > activePlayerMon.getSpeed()) {
                            enemyFirst = true;
                        } else if (activePlayerMon.getSpeed() > activeEnemyMon.getSpeed()) {
                            enemyFirst = false;
                        } else {
                            enemyFirst = Math.random() < 0.5;
                        }

                        if (enemyFirst) {
                            animateTurnBanner("ENEMY TURN", DAMAGE_RED);
                            Timer t = new Timer(1500, evt -> {
                                performAttackSequence(activeEnemyMon, activePlayerMon, enemyMove, false, () -> endTurn());
                            });
                            t.setRepeats(false); 
                            t.start();
                        } else {
                            animateTurnBanner("PLAYER TURN", NEON_CYAN);
                            Timer t = new Timer(800, evt -> {
                                isTurnInProgress = false;
                                startIdleAnimation(jMon1Label, PLAYER_Y, true);
                                startIdleAnimation(jMon2Label, ENEMY_Y, false);
                            });
                            t.setRepeats(false); 
                            t.start();
                        }
                    });
                }
            });
        } else {
            card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
        
        return card;
    }

    private void updateBattleState() {
        lblP1Name.setText(activePlayerMon.getName());
        lblP1Level.setText("Lvl " + activePlayerMon.getLevel());
        typeBadge1.setType(activePlayerMon.getType());
        
        float xpFraction = (float)activePlayerMon.getXP() / activePlayerMon.getXpToNextLevel();
        xpBar1.setXP(xpFraction);
        
        txtJLives1.setText(activePlayerMon.getCurrentHP() + " / " + activePlayerMon.getMaxHP()); 
        hpBar1.snapHealth(activePlayerMon.getCurrentHP(), activePlayerMon.getMaxHP()); 
        hpBar1.checkLowHP();
        
        statusTray1.updateStatuses(activePlayerMon.getActiveStatuses());
        
        String allyPath = "/javamon/assets/ally_" + activePlayerMon.getName().toLowerCase() + ".png";
        if (!scaledAllyIcons.containsKey(activePlayerMon)) { 
             scaledAllyIcons.put(activePlayerMon, loadImage(allyPath, activePlayerMon, true));
        }
        jMon1Label.setIcon(scaledAllyIcons.get(activePlayerMon));
        
        lblP2Name.setText(activeEnemyMon.getName());
        lblP2Level.setText("Lvl " + activeEnemyMon.getLevel());
        typeBadge2.setType(activeEnemyMon.getType());
        
        txtJLives2.setText(activeEnemyMon.getCurrentHP() + " / " + activeEnemyMon.getMaxHP()); 
        hpBar2.snapHealth(activeEnemyMon.getCurrentHP(), activeEnemyMon.getMaxHP()); 
        hpBar2.checkLowHP();
        
        statusTray2.updateStatuses(activeEnemyMon.getActiveStatuses());
        
        String enemyPath = "/javamon/assets/enemy_" + activeEnemyMon.getName().toLowerCase() + ".png";
        if (!scaledEnemyIcons.containsKey(activeEnemyMon)) { 
             scaledEnemyIcons.put(activeEnemyMon, loadImage(enemyPath, activeEnemyMon, false));
        }
        jMon2Label.setIcon(scaledEnemyIcons.get(activeEnemyMon));
        
        jMon1Label.setAlpha(1.0f); jMon2Label.setAlpha(1.0f);
        updateFightPanel();
    }
    
    private ImageIcon loadImage(String path, Monster m, boolean isAlly) {
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        Image img = AssetLoader.loadImagePreferResource(path, fileName);
        if (img == null && m.getImage() != null) img = m.getImage();
        if (img != null) {
            BufferedImage scaled = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaled.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.drawImage(img.getScaledInstance(300, 300, Image.SCALE_SMOOTH), 0, 0, null);
            g2d.dispose();
            return new ImageIcon(scaled);
        }
        return null;
    }
    
    private void scheduleRepaint() {
        if(!repaintScheduled) {
            repaintScheduled = true;
            SwingUtilities.invokeLater(() -> {
                layeredPane.repaint();
                repaintScheduled = false;
            });
        }
    }
    
    private void scheduleRepaint(Component target) {
        if(!repaintScheduled) {
            repaintScheduled = true;
            SwingUtilities.invokeLater(() -> {
                if (target != null) {
                    target.repaint();
                } else {
                    layeredPane.repaint();
                }
                repaintScheduled = false;
            });
        }
    }

    private JLabel createText(int x, int y, int w, int h, Color c, boolean left) {
        JLabel l = new JLabel("", left ? SwingConstants.LEFT : SwingConstants.RIGHT);
        l.setBounds(x, y, w, h); l.setFont(PIXEL_FONT); l.setForeground(c); return l;
    }
    
    private void showOverlay(JPanel p) {
        fightPanel.setVisible(false); switchPanel.setVisible(false); helpPanel.setVisible(false); if(p != null) p.setVisible(true);
    }
    
    // ========================================
    // SOUND EFFECT PLAYBACK METHOD (FOR IN-GAME EFFECTS)
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
            Preferences prefs = Preferences.userNodeForPackage(GameWindow.class);
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


    static class FadableSprite extends JLabel {
        private float alpha = 1.0f;
        private Color flashColor = null;

        public void setAlpha(float value) { this.alpha = Math.max(0.0f, Math.min(1.0f, value)); repaint(); }

        public void flash(Color c) {
            this.flashColor = c; repaint();
            Timer t = new Timer(150, e -> { flashColor = null; repaint(); }); t.setRepeats(false); t.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g; 
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            
            if (flashColor != null && getIcon() instanceof ImageIcon) {
                Image img = ((ImageIcon)getIcon()).getImage();
                BufferedImage bImg = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D gBI = bImg.createGraphics();
                gBI.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                gBI.setComposite(AlphaComposite.SrcAtop); gBI.setColor(flashColor); gBI.fillRect(0, 0, getWidth(), getHeight()); gBI.dispose();
                g.drawImage(bImg, 0, 0, null);
            } else {
                Graphics2D g2dInner = (Graphics2D) g.create();
                g2dInner.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                super.paintComponent(g2dInner); 
                g2dInner.dispose();
            }
        }
    }
    
    static class XPBar extends JPanel {
        private float percentage = 0.0f;
        public XPBar() { setOpaque(false); }
        public void setXP(float p) { this.percentage = Math.max(0f, Math.min(1f, p)); repaint(); }
        
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(50, 50, 50));
            g2.fillRoundRect(0, 0, w, h, h, h);
            
            int fillW = (int)(w * percentage);
            g2.setColor(XP_BLUE);
            g2.fillRoundRect(0, 0, fillW, h, h, h);
            
            g2.setColor(new Color(255, 255, 255, 50));
            g2.fillRoundRect(0, 0, fillW, h/2, h/2, h/2);
            
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w-1, h-1, h, h);
        }
    }

    static class SmoothBatteryBar extends JPanel {
        private int currentHP = 100, maxHP = 100; private double displayHP = 100;
        private final int TOTAL_CELLS = 10; private Timer smoothTimer; private Runnable onComplete;
        
        private Timer pulseTimer; 
        private boolean pulsing = false; private boolean pulseState = false;

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
        
        public void checkLowHP() {
            if(currentHP <= maxHP * 0.25 && !pulsing && currentHP > 0) {
                pulsing = true;
                if(pulseTimer != null) pulseTimer.stop();
                pulseTimer = new Timer(300, e -> {
                    pulseState = !pulseState;
                    repaint();
                    if(currentHP > maxHP * 0.25 || currentHP <= 0) { 
                        ((Timer)e.getSource()).stop(); 
                        pulsing = false; 
                        pulseState = false; 
                        repaint();
                    }
                });
                pulseTimer.start();
            } else if (currentHP > maxHP * 0.25 && pulsing || currentHP <= 0 && pulsing) {
                 if (pulseTimer != null) pulseTimer.stop();
                 pulsing = false;
                 pulseState = false;
                 repaint();
            }
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g); 
            Graphics2D g2d = (Graphics2D) g; 
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            double percent = displayHP / maxHP; int cellsFilled = (int) Math.ceil(percent * TOTAL_CELLS);
            
            Color barColor = (percent > 0.5) ? new Color(0, 230, 0) : (percent > 0.2) ? new Color(255, 215, 0) : new Color(255, 50, 50);
            if(pulsing && pulseState) barColor = new Color(255, 100, 100);

            int w = getWidth(), h = getHeight(), cellGap = 4, cellWidth = (w - (TOTAL_CELLS - 1) * cellGap) / TOTAL_CELLS;
            for (int i = 0; i < TOTAL_CELLS; i++) {
                if (i < cellsFilled) { g2d.setColor(barColor); g2d.fillRect(i * (cellWidth + cellGap), 0, cellWidth, h); g2d.setColor(new Color(255, 255, 255, 70)); g2d.fillRect(i * (cellWidth + cellGap), 0, cellWidth, h / 2); } 
                else { g2d.setColor(new Color(40, 40, 40, 200)); g2d.fillRect(i * (cellWidth + cellGap), 0, cellWidth, h); }
                g2d.setColor(Color.BLACK); g2d.setStroke(new BasicStroke(2)); g2d.drawRect(i * (cellWidth + cellGap), 0, cellWidth, h);
            }
        }
    }
    
    class TypeBadge extends JLabel {
        public TypeBadge() {
            setOpaque(true); setFont(BADGE_FONT); setForeground(Color.WHITE); setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new LineBorder(Color.WHITE, 1, true)); setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { showTypeTooltip(getText(), TypeBadge.this); }
                public void mouseExited(MouseEvent e) { infoOverlay.setVisible(false); }
            });
        }
        public void setType(String type) { setText(type.toUpperCase()); setBackground(getColorForType(type)); }
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
        public StatusTray() { setOpaque(false); setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0)); }
        public void updateStatuses(List<StatusEffect> statuses) {
            removeAll();
            for(StatusEffect s : statuses) {
                JLabel icon = new JLabel(s.name.substring(0, Math.min(3, s.name.length())).toUpperCase());
                icon.setOpaque(true); icon.setFont(new Font("Arial", Font.BOLD, 10)); icon.setForeground(Color.WHITE);
                if(s.type.equals("DMG")) icon.setBackground(new Color(255, 100, 0));
                else if(s.type.equals("STOP")) icon.setBackground(new Color(100, 100, 255));
                else icon.setBackground(new Color(50, 200, 50));
                icon.setBorder(new LineBorder(Color.BLACK, 1)); icon.setPreferredSize(new Dimension(30, 20)); icon.setHorizontalAlignment(SwingConstants.CENTER);
                add(icon);
            } revalidate(); repaint();
        }
    }
}