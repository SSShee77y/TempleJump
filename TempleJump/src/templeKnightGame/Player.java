package templeKnightGame;

import java.net.URL;
import java.util.ArrayList;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Player {
    private final double minJumpSpeed = 5.0;
    private final double maxJumpSpeed = 22.0;
    private final double maxJumpTimer = 30.0;
    private final double jumpSpeedHorizontal = 8.0;
    private final double terminalVelocity = 20.0;
    private final double gravity = 0.6;
    private final double runSpeed = 4.0;
    private final double splatThreshold = 60.0;
    public Vector2 position = new Vector2(0.0, 0.0);
    public Vector2 velocity = new Vector2(0.0, 0.0);
    public boolean jumpHeld = false;
    public boolean leftHeld = false;
    public boolean rightHeld = false;
    private double jumpTimer = 0.0;
    private boolean isOnGround = false;
    private boolean isFalling = false;
    private boolean isRunning = false;
    private boolean isSliding = false;
    private boolean hasBumped = false;
    private boolean facingRight = true;
    private int runningIndex = 2;
    private int runningIndexSign = 1;
    private final int runningIndexReset = 30;
    private int runningIndexCD = 30;
    private double splatTime = 0.0;
    private boolean splatCheck = false;
    private boolean leftCollision = false;
    private boolean rightCollision = false;
    private boolean upCollision = false;
    private boolean downCollision = false;
    public int width = 50;
    public int height = 65;
    public int jumps = 0;
    public int falls = 0;
    private ArrayList<Line> currentLines = new ArrayList();
    private Clip sound;
    private Clip menuMusic;
    private String[] soundStr = new String[]{"Sounds/king_jump.wav", "Sounds/king_land.wav", "Sounds/king_bump.wav", "Sounds/king_splat.wav"};

    public Player() {
    }

    public Player(Vector2 pos) {
        this.position = pos;
    }

    public Player(Vector2 pos, ArrayList<Line> lines) {
        this.position = pos;
        this.currentLines = lines;
    }

    public void UpdateLines(ArrayList<Line> lines) {
        this.currentLines = lines;
    }

    public void Jump() {
        if (!this.isOnGround) {
            return;
        }
        this.playSound(0, true);
        ++this.jumps;
        double verticalJumpSpeed = this.jumpTimer / 30.0 * 17.0 + 5.0;
        if (this.leftHeld && !this.rightHeld) {
            this.facingRight = false;
            this.velocity = new Vector2(-8.0, -verticalJumpSpeed);
        } else if (this.rightHeld && !this.leftHeld) {
            this.facingRight = true;
            this.velocity = new Vector2(8.0, -verticalJumpSpeed);
        } else {
            this.velocity = new Vector2(0.0, -verticalJumpSpeed);
        }
        this.isOnGround = false;
        this.jumpTimer = 0.0;
    }

    public void Update() {
        this.UpdateSplatTime();
        this.ApplyGravity();
        this.UpdateJumpTimer();
        this.CheckCollisions(this.currentLines);
        this.CheckRunning();
        this.position.add(this.velocity);
    }

    public Vector2 GetCharSpriteIndex() {
        Vector2 index = new Vector2(0.0, 0.0);
        boolean didRun = false;
        if (!this.facingRight) {
            index.y = 1.0;
        }
        if (this.isOnGround) {
            if (this.jumpTimer > 0.0) {
                index.x = 4.0;
            } else if (this.splatCheck) {
                index.x = 7.0;
            } else if (!this.isRunning) {
                index.x = 0.0;
            } else {
                index.x = this.runningIndex;
                if (this.runningIndexCD <= 0) {
                    this.runningIndex += this.runningIndexSign;
                    if (this.runningIndex >= 3 || this.runningIndex <= 1) {
                        this.runningIndexSign *= -1;
                    }
                    this.runningIndexCD = 30;
                } else {
                    --this.runningIndexCD;
                }
                didRun = true;
            }
        } else {
            index.x = this.isSliding || this.hasBumped ? 8.0 : (this.isFalling ? 6.0 : 5.0);
        }
        if (!didRun) {
            this.runningIndex = 2;
            this.runningIndexCD = 30;
        }
        return index;
    }

    public double GetJumpTimer() {
        return this.jumpTimer;
    }

    private void UpdateJumpTimer() {
        if (this.isOnGround && this.jumpHeld && this.jumpTimer < 30.0) {
            this.jumpTimer += 1.0;
        }
        if (!this.jumpHeld && this.jumpTimer > 0.0) {
            this.Jump();
        }
    }

    private void ApplyGravity() {
        if (!this.isOnGround) {
            if (!this.isSliding) {
                this.velocity.y = Math.min(this.velocity.y + 0.6, 20.0);
            } else {
                this.velocity.y = Math.min(this.velocity.y + 0.6, 10.0);
                if (this.velocity.y > 0.0) {
                    this.isFalling = true;
                }
            }
        } else {
            this.isFalling = false;
            this.hasBumped = false;
            this.isSliding = false;
            this.velocity.y = 0.0;
        }
    }

    private void UpdateSplatTime() {
        if (this.isOnGround || this.velocity.y < 0.0) {
            if (this.splatTime > 0.0) {
                this.splatTime -= 5.0;
            }
            if (this.splatTime < 0.0) {
                this.splatTime = 0.0;
            }
            return;
        }
        if (this.splatTime < 120.0) {
            this.splatTime += 1.0;
        }
        if (this.splatTime >= 60.0) {
            this.splatCheck = true;
        }
    }

    private void CheckRunning() {
        this.isRunning = false;
        if (!this.isOnGround) {
            return;
        }
        if (this.jumpHeld || this.leftHeld || this.rightHeld) {
            this.splatCheck = false;
        }
        if (this.jumpHeld || !this.leftHeld && !this.rightHeld) {
            this.velocity = new Vector2(0.0, 0.0);
            return;
        }
        this.isRunning = true;
        if (this.leftHeld && !this.rightHeld && !this.leftCollision) {
            this.velocity = new Vector2(-4.0, 0.0);
            this.facingRight = false;
        } else if (this.rightHeld && !this.leftHeld && !this.rightCollision) {
            this.velocity = new Vector2(4.0, 0.0);
            this.facingRight = true;
        }
    }

    private void CheckCollisions(ArrayList<Line> lines) {
        this.leftCollision = false;
        this.rightCollision = false;
        this.upCollision = false;
        this.downCollision = false;
        this.isSliding = false;
        ArrayList<Line> collidedLines = new ArrayList<Line>();
        for (Line line2 : lines) {
            if (!this.CollidesWith(line2)) continue;
            collidedLines.add(line2);
        }
        if (collidedLines.size() == 0) {
            this.isOnGround = false;
            return;
        }
        Line line2 = this.ClosestLine(collidedLines);
        if (line2.rotation == Line.Rot.horizontal) {
            if ((double)line2.y1 >= this.position.y) {
                this.downCollision = true;
            } else {
                this.upCollision = true;
            }
        } else if (line2.rotation == Line.Rot.vertical) {
            if ((double)line2.x1 >= this.position.x) {
                this.rightCollision = true;
            } else {
                this.leftCollision = true;
            }
        } else if (line2.rotation == Line.Rot.diagonal) {
            if (this.isOnGround) {
                this.velocity.y = -Math.abs(this.velocity.x);
                this.isOnGround = false;
            } else if (line2.returnPair(Math.max(line2.x1, line2.x2)) >= line2.returnPair(Math.min(line2.x1, line2.x2))) {
                if (this.position.y > (double)Math.max(line2.y1, line2.y2) - ((double)Math.max(line2.x1, line2.x2) - this.position.x)) {
                    this.velocity.x = 0.0;
                    this.velocity.y = Math.abs(this.velocity.y) / 2.0;
                } else {
                    this.isSliding = true;
                    this.velocity.x = this.velocity.y;
                }
            } else if (this.position.y > (double)Math.max(line2.y1, line2.y2) - (this.position.x - (double)Math.min(line2.x1, line2.x2))) {
                this.velocity.x = 0.0;
                this.velocity.y = Math.abs(this.velocity.y) / 2.0;
            } else {
                this.isSliding = true;
                this.velocity.x = -this.velocity.y;
            }
        }
        if (this.leftCollision || this.rightCollision) {
            if (this.velocity.y != 0.0 && this.velocity.x != 0.0) {
                this.playSound(2, true);
                this.hasBumped = true;
            }
            if (this.leftCollision) {
                this.position.x = Math.max(line2.x1, line2.x2) + this.width / 2;
                if (!this.isOnGround && this.velocity.x < 0.0) {
                    this.velocity.x = Math.abs(this.velocity.x) / 2.0;
                }
            }
            if (this.rightCollision) {
                this.position.x = Math.min(line2.x1, line2.x2) - this.width / 2;
                if (!this.isOnGround && this.velocity.x > 0.0) {
                    this.velocity.x = -Math.abs(this.velocity.x) / 2.0;
                }
            }
        }
        if (this.downCollision) {
            if (this.velocity.y > 0.0 && this.splatCheck) {
                this.playSound(3, true);
                ++this.falls;
            } else if (this.velocity.y > 0.0) {
                this.playSound(1, true);
            }
            if (line2.rotation == Line.Rot.horizontal) {
                this.position.y = line2.y1 - this.height / 2;
            }
            if (this.velocity.y >= 0.0) {
                this.isOnGround = true;
                this.velocity.y = 0.0;
            }
        }
        if (this.upCollision) {
            if (this.velocity.y != 0.0) {
                this.playSound(2, true);
                this.hasBumped = true;
            }
            if (line2.rotation == Line.Rot.horizontal) {
                this.position.y = line2.y1 + this.height / 2;
            }
            if (this.velocity.y < 0.0) {
                this.velocity.y = Math.abs(this.velocity.y) / 2.0;
            }
        }
        if (!(this.leftCollision || this.rightCollision || this.upCollision)) {
            this.hasBumped = false;
        }
    }

    private boolean CollidesWith(Line line) {
        return line.Shape().intersects(this.position.x - (double)(this.width / 2), this.position.y - (double)(this.height / 2), this.width, this.height);
    }

    private Line ClosestLine(ArrayList<Line> lines) {
        if (lines.size() >= 2) {
            Line vert = null;
            Line horiz = null;
            Line diag = null;
            if (lines.size() > 2) {
                ArrayList<Line> verts = new ArrayList<Line>();
                ArrayList<Line> horis = new ArrayList<Line>();
                ArrayList<Line> diags = new ArrayList<Line>();
                for (Line line : lines) {
                    if (line.rotation == Line.Rot.horizontal) {
                        horis.add(line);
                        continue;
                    }
                    if (line.rotation == Line.Rot.vertical) {
                        verts.add(line);
                        continue;
                    }
                    if (line.rotation != Line.Rot.diagonal) continue;
                    diags.add(line);
                }
                if (horis.size() >= 2) {
                    horiz = this.ClosestToPlayer(horis);
                } else if (horis.size() >= 1) {
                    horiz = horis.get(0);
                }
                if (verts.size() >= 2) {
                    vert = this.ClosestToPlayer(verts);
                } else if (verts.size() >= 1) {
                    vert = verts.get(0);
                }
                if (diags.size() >= 2) {
                    diag = this.ClosestToPlayer(diags);
                } else if (diags.size() >= 1) {
                    diag = diags.get(0);
                }
            }
            if (lines.size() == 2) {
                for (Line line : lines) {
                    if (line.rotation == Line.Rot.horizontal) {
                        horiz = line;
                        continue;
                    }
                    if (line.rotation == Line.Rot.vertical) {
                        vert = line;
                        continue;
                    }
                    if (line.rotation != Line.Rot.diagonal) continue;
                    diag = line;
                }
            }
            if (vert != null && horiz != null) {
                if (this.isOnGround && this.velocity.y == 0.0 && this.position.x > (double)vert.x1 && Math.min(vert.y1, vert.y2) < horiz.y1 && Math.min(horiz.x1, horiz.x2) < vert.x1) {
                    this.isOnGround = false;
                    return vert;
                }
                if (this.isOnGround && this.velocity.y == 0.0 && this.position.x < (double)vert.x1 && Math.min(vert.y1, vert.y2) < horiz.y1 && Math.max(horiz.x1, horiz.x2) > vert.x1) {
                    this.isOnGround = false;
                    return vert;
                }
                if (this.isOnGround && Math.min(vert.y1, vert.y2) < horiz.y1) {
                    this.position.y = horiz.y1 - this.height / 2;
                    return vert;
                }
                if (this.isOnGround) {
                    return horiz;
                }
                for (Line line : lines) {
                    if (line.rotation == Line.Rot.horizontal) {
                        if ((double)line.y1 >= this.position.y) {
                            this.downCollision = true;
                            continue;
                        }
                        this.upCollision = true;
                        continue;
                    }
                    if (line.rotation != Line.Rot.vertical) continue;
                    if ((double)line.x1 >= this.position.x) {
                        this.rightCollision = true;
                        continue;
                    }
                    this.leftCollision = true;
                }
                String corner = "";
                if (this.upCollision) {
                    if (this.leftCollision) {
                        corner = "NW";
                    }
                    if (this.rightCollision) {
                        corner = "NE";
                    }
                } else if (this.downCollision) {
                    if (this.leftCollision) {
                        corner = "SW";
                    }
                    if (this.rightCollision) {
                        corner = "SE";
                    }
                }
                this.leftCollision = false;
                this.rightCollision = false;
                this.upCollision = false;
                this.downCollision = false;
                if (corner.equals("NW")) {
                    if (this.velocity.x == 0.0 && horiz.x1 >= vert.x1 && horiz.x2 >= vert.x1) {
                        return horiz;
                    }
                    if (horiz.x1 >= vert.x1 && horiz.x2 >= vert.x1) {
                        return horiz;
                    }
                    if (Math.abs((double)vert.x1 - (this.position.x - (double)(this.width / 2))) > Math.abs((double)horiz.y1 - (this.position.y - (double)(this.height / 2)))) {
                        return horiz;
                    }
                    return vert;
                }
                if (corner.equals("NE")) {
                    if (this.velocity.x == 0.0 && horiz.x1 <= vert.x1 && horiz.x2 <= vert.x1) {
                        return horiz;
                    }
                    if (horiz.x1 <= vert.x1 && horiz.x2 <= vert.x1) {
                        return horiz;
                    }
                    if (Math.abs(this.position.x + (double)(this.width / 2) - (double)vert.x1) > Math.abs((double)horiz.y1 - (this.position.y - (double)(this.height / 2)))) {
                        return horiz;
                    }
                    return vert;
                }
                if (corner.equals("SW")) {
                    if (this.velocity.x == 0.0 && horiz.x1 >= vert.x1 && horiz.x2 >= vert.x1) {
                        return horiz;
                    }
                    if (horiz.x1 >= vert.x1 && horiz.x2 >= vert.x1) {
                        return horiz;
                    }
                    if (Math.min(vert.y1, vert.y1) < horiz.y1) {
                        return vert;
                    }
                    if (Math.abs((double)vert.x1 - (this.position.x - (double)(this.width / 2))) > Math.abs(this.position.y + (double)(this.height / 2) - (double)horiz.y1)) {
                        return horiz;
                    }
                    return vert;
                }
                if (corner.equals("SE")) {
                    if (this.velocity.x == 0.0 && horiz.x1 <= vert.x1 && horiz.x2 <= vert.x1) {
                        return horiz;
                    }
                    if (horiz.x1 <= vert.x1 && horiz.x2 <= vert.x1) {
                        return horiz;
                    }
                    if (Math.min(vert.y1, vert.y1) < horiz.y1) {
                        return vert;
                    }
                    if (Math.abs(this.position.x + (double)(this.width / 2) - (double)vert.x1) > Math.abs(this.position.y + (double)(this.height / 2) - (double)horiz.y1)) {
                        return horiz;
                    }
                    return vert;
                }
            } else {
                if (horiz != null && diag != null) {
                    if (Math.min(diag.y1, diag.y2) < horiz.y1) {
                        return diag;
                    }
                    return horiz;
                }
                if (vert != null && diag != null) {
                    if (this.velocity.x == 0.0) {
                        return diag;
                    }
                    if (Math.min(vert.y1, vert.y2) <= Math.min(diag.y1, diag.y2)) {
                        if (vert.x1 <= Math.max(diag.x1, diag.x2) && this.position.x <= (double)Math.min(diag.x1, diag.x2)) {
                            return vert;
                        }
                        if (vert.x1 >= Math.min(diag.x1, diag.x2) && this.position.x >= (double)Math.max(diag.x1, diag.x2)) {
                            return vert;
                        }
                    } else {
                        return diag;
                    }
                }
            }
        }
        return lines.get(0);
    }

    private Line ClosestToPlayer(ArrayList<Line> lines) {
        int i;
        Line line = lines.get(0);
        if (lines.get((int)0).rotation == Line.Rot.horizontal) {
            for (i = 1; i < lines.size(); ++i) {
                if (!(Math.abs((double)lines.get((int)i).y1 - (this.position.y + (double)(this.height / 2))) < Math.abs((double)line.y1 - (this.position.y + (double)(this.height / 2))))) continue;
                line = lines.get(i);
            }
        }
        if (lines.get((int)0).rotation == Line.Rot.vertical) {
            for (i = 1; i < lines.size(); ++i) {
                if (!(Math.abs((double)lines.get((int)i).x1 - this.position.x) < Math.abs((double)line.x1 - this.position.x))) continue;
                line = lines.get(i);
            }
        }
        return line;
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
}