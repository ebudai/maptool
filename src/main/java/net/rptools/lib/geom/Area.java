package net.rptools.lib.geom;

import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.*;

import java.awt.*;
import java.awt.geom.Point2D;

public class Area {

    private static final GeometryFactory FACTORY = new GeometryFactory();

    private Geometry geometry;
    private Rectangle bounds;
    private boolean boundsAreDirty = true;

    public Area() {

    }

    public Area(Geometry geometry) {
        this.geometry = geometry;
    }

    public Area(Rectangle rectangle) {
        this.geometry = FACTORY.toGeometry(rectangle);
        this.bounds = new Rectangle(rectangle);
        boundsAreDirty = false;
    }

    public Area(Area area) {
        this.geometry = area.geometry.copy();
        this.bounds = new Rectangle(area.getBounds());
        this.boundsAreDirty = false;
    }

    public Area(Shape shape) {
        this.geometry = ShapeReader.read(shape.getPathIterator(null, 1), FACTORY);
    }

    public Area copy() {
        Area area = new Area(this.geometry);
        area.bounds = new Rectangle(this.bounds);
        area.boundsAreDirty = this.boundsAreDirty;
        return area;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public Rectangle getBounds() {
        if (boundsAreDirty) {
            bounds = (Rectangle) geometry.getEnvelopeInternal();
            boundsAreDirty = false;
        }
        return bounds;
    }

    public Rectangle getBounds2D() {
        return getBounds();
    }

    public boolean isEmpty() {
        return geometry.isEmpty();
    }

    public Area createTransformedArea(AffineTransform transform) {
        return transform.createTransformedShape(this);
    }

    public Shape asShape() {
        ShapeWriter writer = new ShapeWriter();
        return writer.toShape(geometry);
    }

    public boolean contains(Area area) {
        return geometry.contains(area.geometry);
    }

    public boolean contains(Rectangle rectangle) {
        return geometry.contains(FACTORY.toGeometry(rectangle));
    }

    public boolean contains(Point2D point) {
        return contains(point.getX(), point.getY());
    }

    public boolean contains(double x, double y) {
        Geometry point = FACTORY.createPoint(new CoordinateXY(x, y));
        return geometry.contains(point);
    }

    public boolean intersects(Area area) {
        return geometry.intersects(area.geometry);
    }

    public boolean intersects(Rectangle rectangle) {
        return geometry.intersects(FACTORY.toGeometry(rectangle));
    }

    public boolean isSingular() {
        if (geometry instanceof GeometryCollection) {
            GeometryCollection collection = (GeometryCollection)geometry;
            return collection.getNumGeometries() < 2;
        }
        return true;
    }

    public void intersect(Area area) {
        geometry.intersection(area.geometry);
        boundsAreDirty = true;
    }

    public void transform(AffineTransform transformation) {
        geometry = transformation.transform(geometry);
        boundsAreDirty = true;
    }

    public void add(Area area) {
        geometry = geometry.union(area.geometry);
        boundsAreDirty = true;
    }

    public void subtract(Area area) {
        geometry = geometry.difference(area.geometry);
        boundsAreDirty = true;
    }

    public void reset() {
        geometry = FACTORY.createGeometryCollection();
        boundsAreDirty = true;
    }
}
