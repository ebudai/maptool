package net.rptools.lib.geom;

import org.locationtech.jts.geom.Envelope;

import java.awt.*;

public class Rectangle extends Envelope {

    public Rectangle() {
        super();
    }

    public Rectangle(Rectangle rectangle) {
        super.init(rectangle);
    }

    public Rectangle(double x, double y, double width, double height) {
        super(x, x + width, y, y + height);
    }

    public Rectangle(double width, double height) {
        this(0, 0, width, height);
    }

    public Rectangle(java.awt.Rectangle rectangle) {
        this(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
    }

    public double getX() { return super.getMinX(); }
    public double getY() { return super.getMinY(); }

    public double getCenterX() { return super.centre().x; }
    public double getCenterY() { return super.centre().y; }

    public void setX(double x) { this.setBounds(x, this.getY(), this.getWidth(), this.getHeight()); }
    public void setY(double y) { this.setBounds(this.getX(), y, this.getWidth(), this.getHeight()); }

    public void addX(double x) { this.setBounds(getX() + x, getY(), getWidth(), getHeight()); }
    public void addY(double y) { this.setBounds(getX(), getY() + y, getWidth(), getHeight()); }

    public void setWidth(double width) { this.setBounds(getX(), getY(), width, getHeight()); }
    public void setHeight(double height) { this.setBounds(getX(), getY(), getWidth(), height); }

    public void setBounds(double x, double y, double width, double height) {
        super.init(x, x + width, y, y + height);
    }

    public void add(Rectangle rectangle) {
        super.expandToInclude(rectangle);
    }

    public void setSize(double width, double height) {
        super.init(getMinX(), getMinX() + width, getMinY(), getMinY() + height);
    }

    public Dimension getSize() {
        return new Dimension((int)getWidth(), (int)getHeight());
    }
}
