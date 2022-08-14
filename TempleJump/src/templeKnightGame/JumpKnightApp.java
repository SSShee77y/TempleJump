package templeKnightGame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class JumpKnightApp
extends JPanel {
    private static JFrame frame = new JFrame();
    private GraphicsEnvironment gEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    private GraphicsDevice gDevice = this.gEnvironment.getDefaultScreenDevice();
    private static double scale = 1.0;
    private static final int BASE_WIDTH = 1200;
    private static final int BASE_HEIGHT = 900;
    private boolean isFullscreen = false;
    private int FPS = 61;
    private double timeLast;
    private double timeDif;
    private double deltaTime;
    private Player player;
    private Player lastAttempt = null;
    private ArrayList<Level> levels = new Level().SetupLevels();
    private int currentLevel = 0;
    private int highestLevel = 0;
    private int lastAttemptLevel = 0;
    private boolean altHeld = false;
    public State gameState = State.Menu;
    private boolean pause = false;
    private int selector;
    private int randSelect = 5;
    private boolean debugMode = false;
    private ArrayList<BufferedImage> playerSprites = new ArrayList();
    private ArrayList<BufferedImage> levelSprites = new ArrayList();
    private Font font;
    private Font aFont;
    private Font bFont;
    private Font cFont;
    private Font mFont;
    private Clip sound;
    private Clip menuMusic;
    private String[] soundStr = new String[]{"Sounds/king_jump.wav", "Sounds/king_land.wav", "Sounds/king_bump.wav", "Sounds/king_splat.wav", "Sounds/gettingOver_theme.wav", "Sounds/selectA.wav", "Sounds/menu_open.wav", "Sounds/menu_confirm.wav", "Sounds/menu_fail.wav"};

    public static void main(String[] args) throws Exception {
        scale = 900.0 / (Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 180.0);
        frame.setTitle("Jump Knight");
        frame.setDefaultCloseOperation(3);
        frame.setSize((int)(1200.0 * (1.0 / scale)), (int)(900.0 * (1.0 / scale)));
        Container container = frame.getContentPane();
        container.add(new JumpKnightApp());
        frame.setUndecorated(true);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.isAlwaysOnTop();
        ImageIcon cursorImg = new ImageIcon(ClassLoader.getSystemResource(""));
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg.getImage(), new Point(0, 0), "blank");
        frame.setCursor(blankCursor);
    }

    public JumpKnightApp() throws Exception {
        int i;
        this.setBackground(Color.black);
        URL url = ClassLoader.getSystemResource("Fonts/alkhemikal.ttf");
        Font tempFont = Font.createFont(0, url.openStream());
        this.font = tempFont.deriveFont(0, 28.0f);
        this.aFont = tempFont.deriveFont(0, 44.0f);
        this.bFont = tempFont.deriveFont(0, 60.0f);
        this.cFont = tempFont.deriveFont(0, 100.0f);
        this.mFont = tempFont.deriveFont(0, 140.0f);
        this.gEnvironment.registerFont(this.font);
        BufferedImage charSpiteSheet = ImageIO.read(ClassLoader.getSystemResource("CharacterSprites.png"));
        BufferedImage stageOneSheet = ImageIO.read(ClassLoader.getSystemResource("Stage1.png"));
        BufferedImage stageTwoSheet = ImageIO.read(ClassLoader.getSystemResource("Stage2.png"));
        BufferedImage stageThreeSheet = ImageIO.read(ClassLoader.getSystemResource("Stage3.png"));
        for (i = 0; i < 1; ++i) {
            for (int j = 0; j < charSpiteSheet.getWidth() / 32; ++j) {
                this.playerSprites.add(charSpiteSheet.getSubimage(j * 32, 0, 32, 32));
            }
        }
        for (i = 1; i <= stageOneSheet.getHeight() / 900; ++i) {
            this.levelSprites.add(stageOneSheet.getSubimage(0, stageOneSheet.getHeight() - i * 900, 1200, 900));
        }
        for (i = 1; i <= stageTwoSheet.getHeight() / 900; ++i) {
            this.levelSprites.add(stageTwoSheet.getSubimage(0, stageTwoSheet.getHeight() - i * 900, 1200, 900));
        }
        for (i = 1; i <= stageThreeSheet.getHeight() / 900; ++i) {
            this.levelSprites.add(stageThreeSheet.getSubimage(0, stageThreeSheet.getHeight() - i * 900, 1200, 900));
        }
        this.player = new Player(new Vector2(500.0, 800.0), this.levels.get((int)this.currentLevel).lines);
        this.playMenuMusic(4, true);
        this.menuMusic.loop(-1);
        this.addKeyListener(new KeyInput());
        this.setFocusable(true);
        Timer gametimer = new Timer(13, new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (JumpKnightApp.this.gameState == State.Game && !JumpKnightApp.this.pause) {
                    JumpKnightApp.this.player.Update();
                    JumpKnightApp.this.CheckLevel();
                }
                JumpKnightApp.this.repaint();
            }
        });
        gametimer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        if (this.isFullscreen) {
            g2.translate((Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 1200.0 / scale) / 2.0, (Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 900.0 / scale) / 2.0);
        }
        g2.scale(1.0 / scale, 1.0 / scale);
        g2.setColor(Color.white);
        g2.setFont(this.font);
        g2.setStroke(new BasicStroke(3.0f));
        if (this.gameState == State.Menu) {
            g2.drawString("F11 Toggle Fullscreen", 20, 880);
            g2.drawString("Based on Jump King", 965, 880);
            g2.setFont(this.mFont);
            g2.drawString("Temple Jump", 270, 160);
            g2.setFont(this.font);
            g2.drawString("A Game of Blunders and Misfortunes", 400, 220);
            int playerSize = this.player.height * 3;
            g2.drawImage(this.playerSprites.get(this.randSelect), 600 + playerSize / 2, 385 - playerSize / 2, -playerSize, playerSize, null);
            g2.setFont(this.bFont);
            if (this.selector == 0) {
                g2.drawString("> Begin Journey", 100, 660);
            } else {
                g2.drawString("Begin Journey", 100, 660);
            }
            if (this.selector == 1) {
                g2.drawString("> Quit", 100, 740);
            } else {
                g2.drawString("Quit", 100, 740);
            }
            if (this.lastAttempt != null) {
                g2.setFont(this.aFont);
                g2.drawString("Last Attempt", 900, 610);
                g2.drawString("Jumps: " + this.lastAttempt.jumps, 929, 660);
                g2.drawString("Falls: " + this.lastAttempt.falls, 950, 710);
                g2.drawString("High: " + this.lastAttemptLevel, 959, 760);
            }
            g2.setFont(this.font);
        }
        if (this.gameState == State.Game) {
            g2.drawImage(this.levelSprites.get(this.currentLevel), 0, 0, 1200, 900, null);
            if (this.debugMode) {
                for (int i = 0; i < this.levels.get((int)this.currentLevel).lines.size(); ++i) {
                    Line line = this.levels.get((int)this.currentLevel).lines.get(i);
                    g2.draw(line.Shape());
                    g2.drawString(String.format("%d", i), (line.x1 + line.x2) / 2, (line.y1 + line.y2) / 2);
                }
                g2.fillRect((int)this.player.position.x - this.player.width / 2, (int)this.player.position.y - this.player.height / 2, this.player.width, this.player.height);
                g2.drawString(String.format("%d", this.currentLevel), 25, 25);
            }
            if (this.currentLevel <= 0) {
                g2.drawString("A D or < > to Run / Direction", 460, 540);
                g2.drawString("Space Bar to Jump", 505, 580);
            }
            if (this.player.GetCharSpriteIndex().y == 0.0) {
                g2.drawImage(this.playerSprites.get((int)this.player.GetCharSpriteIndex().x), (int)(this.player.position.x + (double)(this.player.width / 2) - (double)this.player.width * 1.2), (int)(this.player.position.y + (double)(this.player.height / 2) - (double)this.player.height * 1.2), (int)((double)this.player.height * 1.2), (int)((double)this.player.height * 1.2), null);
            } else {
                g2.drawImage(this.playerSprites.get((int)this.player.GetCharSpriteIndex().x), (int)(this.player.position.x - (double)(this.player.width / 2) + (double)this.player.width * 1.2), (int)(this.player.position.y + (double)(this.player.height / 2) - (double)this.player.height * 1.2), (int)((double)(-this.player.height) * 1.2), (int)((double)this.player.height * 1.2), null);
            }
            g2.setColor(new Color(0, 0, 0, 80));
            if (this.currentLevel <= 4) {
                g2.fillRect(0, 0, 1200, 900);
            }
            g2.setColor(new Color(250, 195, 10));
            g2.fillRect(1050, 20, (int)(this.player.GetJumpTimer() / 30.0 * 100.0), 20);
            g2.setColor(new Color(190, 140, 10));
            g2.drawRect(1050, 20, 100, 20);
            if (this.pause) {
                g2.setColor(Color.black);
                g2.fillRect(300, 230, 600, 340);
                g2.setColor(Color.white);
                g2.drawRect(315, 245, 570, 310);
                g2.setFont(this.cFont);
                g2.drawString("Paused", 465, 340);
                g2.setFont(this.aFont);
                if (this.selector == 0) {
                    g2.drawString("> Resume", 400, 420);
                } else {
                    g2.drawString("Resume", 400, 420);
                }
                if (this.selector == 1) {
                    g2.drawString("> Give Up", 400, 470);
                } else {
                    g2.drawString("Give Up", 400, 470);
                }
                g2.setFont(this.font);
                g2.drawString("Jumps: " + this.player.jumps, 677, 415);
                g2.drawString("Falls: " + this.player.falls, 690, 445);
                g2.drawString("High: " + this.highestLevel, 696, 475);
                g2.drawString("I heard there's a smoking hot babe at the top", 355, 535);
            } else if (!this.pause) {
                this.selector = 0;
            }
        }
    }

    private void ToggleFullscreen() {
        frame.dispose();
        frame.setUndecorated(true);
        boolean bl = this.isFullscreen = !this.isFullscreen;
        if (this.isFullscreen) {
            frame.setExtendedState(6);
            scale = 900.0 / Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        } else {
            frame.setExtendedState(0);
            scale = 900.0 / (Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 180.0);
            frame.setSize((int)(1200.0 * (1.0 / scale)), (int)(900.0 * (1.0 / scale)));
            frame.setLocationRelativeTo(null);
        }
        frame.setVisible(true);
    }

    private void CheckLevel() {
        if (this.player.position.y < (double)(-this.player.height / 2) && this.currentLevel < this.levels.size() - 1) {
            ++this.currentLevel;
            this.player.position.y += 900.0;
            this.player.UpdateLines(this.levels.get((int)this.currentLevel).lines);
        } else if (this.player.position.y > (double)(900 - this.player.height / 2)) {
            if (this.currentLevel <= 0) {
                this.currentLevel = 0;
            } else {
                --this.currentLevel;
                this.player.position.y -= 900.0;
                this.player.UpdateLines(this.levels.get((int)this.currentLevel).lines);
            }
        }
        this.highestLevel = Math.max(this.currentLevel, this.highestLevel);
    }

    private void playSound(int i, boolean startPlay) {
        try {
            URL url = ClassLoader.getSystemResource(this.soundStr[i]);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);
            this.sound = AudioSystem.getClip();
            this.sound.open(audioInputStream);
            if (startPlay) {
                this.sound.start();
            }
        }
        catch (Exception e) {
            System.out.println("An error occured - " + e.getMessage());
        }
    }

    private void playMenuMusic(int i, boolean startPlay) {
        try {
            URL url = ClassLoader.getSystemResource(this.soundStr[i]);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);
            this.menuMusic = AudioSystem.getClip();
            this.menuMusic.open(audioInputStream);
            if (startPlay) {
                this.menuMusic.start();
            }
        }
        catch (Exception e) {
            System.out.println("An error occured - " + e.getMessage());
        }
    }

    private class KeyInput
    extends KeyAdapter {
        private KeyInput() {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (JumpKnightApp.this.altHeld && e.getKeyCode() == 10 || e.getKeyCode() == 122) {
                JumpKnightApp.this.ToggleFullscreen();
            }
            if (JumpKnightApp.this.gameState == State.Game && e.getKeyCode() == 27) {
                JumpKnightApp.this.pause = !JumpKnightApp.this.pause;
                JumpKnightApp.this.playSound(6, true);
            }
            if (JumpKnightApp.this.gameState == State.Game && !JumpKnightApp.this.pause) {
                if (e.getKeyCode() == 32) {
                    ((JumpKnightApp)JumpKnightApp.this).player.jumpHeld = true;
                }
                if (e.getKeyCode() == 65 || e.getKeyCode() == 37) {
                    ((JumpKnightApp)JumpKnightApp.this).player.leftHeld = true;
                }
                if (e.getKeyCode() == 68 || e.getKeyCode() == 39) {
                    ((JumpKnightApp)JumpKnightApp.this).player.rightHeld = true;
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (JumpKnightApp.this.gameState == State.Menu) {
                if (e.getKeyCode() == 87 || e.getKeyCode() == 38) {
                    if (JumpKnightApp.this.selector > 0) {
                        JumpKnightApp jumpKnightApp = JumpKnightApp.this;
                        jumpKnightApp.selector = jumpKnightApp.selector - 1;
                    }
                    JumpKnightApp.this.playSound(5, true);
                }
                if (e.getKeyCode() == 83 || e.getKeyCode() == 40) {
                    if (JumpKnightApp.this.selector < 1) {
                        JumpKnightApp jumpKnightApp = JumpKnightApp.this;
                        jumpKnightApp.selector = jumpKnightApp.selector + 1;
                    }
                    JumpKnightApp.this.playSound(5, true);
                }
                if (e.getKeyCode() == 32 || e.getKeyCode() == 10) {
                    if (JumpKnightApp.this.selector == 0) {
                        JumpKnightApp.this.gameState = State.Game;
                        JumpKnightApp.this.playSound(7, true);
                    }
                    if (JumpKnightApp.this.selector == 1) {
                        System.exit(0);
                        JumpKnightApp.this.playSound(8, true);
                    }
                }
            }
            if (JumpKnightApp.this.gameState == State.Game && JumpKnightApp.this.pause) {
                if (e.getKeyCode() == 87 || e.getKeyCode() == 38) {
                    if (JumpKnightApp.this.selector > 0) {
                        JumpKnightApp jumpKnightApp = JumpKnightApp.this;
                        jumpKnightApp.selector = jumpKnightApp.selector - 1;
                    }
                    JumpKnightApp.this.playSound(5, true);
                }
                if (e.getKeyCode() == 83 || e.getKeyCode() == 40) {
                    if (JumpKnightApp.this.selector < 1) {
                        JumpKnightApp jumpKnightApp = JumpKnightApp.this;
                        jumpKnightApp.selector = jumpKnightApp.selector + 1;
                    }
                    JumpKnightApp.this.playSound(5, true);
                }
                if (e.getKeyCode() == 32 || e.getKeyCode() == 10) {
                    if (JumpKnightApp.this.selector == 0) {
                        JumpKnightApp.this.pause = false;
                        JumpKnightApp.this.playSound(6, true);
                    }
                    if (JumpKnightApp.this.selector == 1) {
                        JumpKnightApp.this.playSound(8, true);
                        JumpKnightApp.this.pause = false;
                        JumpKnightApp.this.gameState = State.Menu;
                        JumpKnightApp.this.lastAttemptLevel = JumpKnightApp.this.highestLevel;
                        JumpKnightApp.this.currentLevel = 0;
                        JumpKnightApp.this.highestLevel = 0;
                        JumpKnightApp.this.selector = 0;
                        JumpKnightApp.this.lastAttempt = JumpKnightApp.this.player;
                        int temp = JumpKnightApp.this.randSelect;
                        while (JumpKnightApp.this.randSelect == temp) {
                            JumpKnightApp.this.randSelect = (int)(Math.random() * 8.0);
                        }
                        JumpKnightApp.this.player = new Player(new Vector2(500.0, 800.0), ((Level)((JumpKnightApp)JumpKnightApp.this).levels.get((int)((JumpKnightApp)JumpKnightApp.this).currentLevel)).lines);
                    }
                }
            }
            if (JumpKnightApp.this.gameState == State.Game) {
                if (e.getKeyCode() == 32) {
                    if (!JumpKnightApp.this.pause) {
                        JumpKnightApp.this.player.Jump();
                    }
                    ((JumpKnightApp)JumpKnightApp.this).player.jumpHeld = false;
                }
                if (e.getKeyCode() == 65 || e.getKeyCode() == 37) {
                    ((JumpKnightApp)JumpKnightApp.this).player.leftHeld = false;
                }
                if (e.getKeyCode() == 68 || e.getKeyCode() == 39) {
                    ((JumpKnightApp)JumpKnightApp.this).player.rightHeld = false;
                }
            }
        }
    }

    private static enum State {
        Game,
        Menu;

    }
}