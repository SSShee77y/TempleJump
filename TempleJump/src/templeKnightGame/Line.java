package templeKnightGame;

import java.awt.geom.Line2D;
import javax.swing.JPanel;

public class Line extends JPanel
{
    public int x1;
    public int y1;
    public int x2;
    public int y2;
    public Rot rotation;
    
    public Line(final int x1, final int y1, final int x2, final int y2) {
        this.rotation = Rot.diagonal;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        if (y1 == y2) {
            this.rotation = Rot.horizontal;
        }
        if (x1 == x2) {
            this.rotation = Rot.vertical;
        }
    }
    
    public Line2D Shape() {
        return new Line2D.Double(this.x1, this.y1, this.x2, this.y2);
    }
    
    public int returnPair(final int a) {
        if (a == this.x1) {
            return this.y1;
        }
        if (a == this.x2) {
            return this.y2;
        }
        if (a == this.y1) {
            return this.x1;
        }
        if (a == this.y2) {
            return this.x2;
        }
        return -1;
    }
    
    @Override
    public String toString() {
        if (this.rotation == Rot.horizontal) {
            return "horizontal";
        }
        if (this.rotation == Rot.vertical) {
            return "vertical";
        }
        if (this.rotation == Rot.diagonal) {
            return "diagonal";
        }
        return null;
    }
    
    public enum Rot
    {
        horizontal("horizontal", 0), 
        vertical("vertical", 1), 
        diagonal("diagonal", 2);
        
        private Rot(final String name, final int ordinal) {
        }
    }
}