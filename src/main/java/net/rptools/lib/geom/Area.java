package net.rptools.lib.geom;

import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.*;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Area implements Shape, Cloneable {

    private static final GeometryFactory FACTORY = new GeometryFactory();

    private Geometry geometry;
    private Rectangle2D bounds;
    private boolean boundsAreDirty = true;

    public Area() {

    }

    public Area(Geometry geometry) {
        this.geometry = geometry;
    }

    public Area(Rectangle rectangle) {
        Envelope envelope = new Envelope(rectangle.x, rectangle.x + rectangle.width, rectangle.y, rectangle.y + rectangle.height);
        this.geometry = FACTORY.toGeometry(envelope);
        this.bounds = new Rectangle(rectangle);
        boundsAreDirty = false;
    }

    public Area(Area area) {
        this.geometry = area.geometry.copy();
        this.bounds = new Rectangle(area.getBounds());
        this.boundsAreDirty = area.boundsAreDirty;
    }

    public Area(Shape shape) {
        this.geometry = ShapeReader.read(shape.getPathIterator(null, 1), FACTORY);
    }

    @Override
    public Object clone() {
        return new Area(this);
    }

    public Geometry getGeometry() {
        return geometry;
    }

    @Override
    public Rectangle2D getBounds2D() {
        if (boundsAreDirty) {
            bounds = envelopeToRectangle(geometry.getEnvelopeInternal());
            boundsAreDirty = false;
        }
        return bounds;
    }

    @Override
    public Rectangle getBounds() {
        return getBounds2D().getBounds();
    }

    @Override
    public boolean contains(double x, double y) {
        Geometry point = FACTORY.createPoint(new CoordinateXY(x, y));
        return geometry.contains(point);
    }

    @Override
    public boolean contains(Point2D p) {
        return false;
    }

    public boolean isEmpty() {
        return geometry.isEmpty();
    }
    /*

    public boolean contains(Area area) {
        return geometry.contains(area.geometry);
    }

    public boolean contains(Point2D point) {
        return contains(point.getX(), point.getY());
    }
*/
    @Override
    public boolean intersects(double x, double y, double w, double h) {
        return geometry.intersects(rectangleToGeometry(x, x + w, y, y + h));
    }

    @Override
    public boolean intersects(Rectangle2D rectangle) {
        return geometry.intersects(rectangleToGeometry(rectangle));
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return geometry.contains(rectangleToGeometry(x, x + w, y, y + h));
    }

    @Override
    public boolean contains(Rectangle2D rectangle) {
        return geometry.contains(rectangleToGeometry(rectangle));
    }

    @Override
    public PathIterator getPathIterator(java.awt.geom.AffineTransform at) {
        ShapeWriter writer = new ShapeWriter();
        Shape shape = writer.toShape(geometry);
        return shape.getPathIterator(at);
    }

    @Override
    public PathIterator getPathIterator(java.awt.geom.AffineTransform at, double flatness) {
        ShapeWriter writer = new ShapeWriter();
        Shape shape = writer.toShape(geometry);
        return shape.getPathIterator(at, flatness);
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

    public void transform(AffineTransform transform) {
        geometry = transform.transform(geometry);
        boundsAreDirty = true;
    }

    public void transform(java.awt.geom.AffineTransform transform) {
        transform(new AffineTransform(transform));
    }

    public Area add(Area area) {
        geometry = geometry.union(area.geometry);
        boundsAreDirty = true;
        return this;
    }

    public Area subtract(Area area) {
        geometry = geometry.difference(area.geometry);
        boundsAreDirty = true;
        return this;
    }

    public Area reset() {
        geometry = FACTORY.createGeometryCollection();
        boundsAreDirty = true;
        return this;
    }

    private static Rectangle2D envelopeToRectangle(Envelope envelope) {
        return new Rectangle2D.Double(envelope.getMinX(), envelope.getMinY(), envelope.getWidth(), envelope.getHeight());
    }

    private static Geometry rectangleToGeometry(Rectangle2D rectangle) {
        return rectangleToGeometry(rectangle.getBounds());
    }

    private static Geometry rectangleToGeometry(Rectangle rectangle) {
        return rectangleToGeometry(rectangle.x, rectangle.x + rectangle.width, rectangle.y, rectangle.y + rectangle.height);
    }

    private static Geometry rectangleToGeometry(double x1, double x2, double y1, double y2) {
        Envelope envelope = new Envelope(x1, x2, y1, y1);
        return FACTORY.toGeometry(envelope);
    }

    public Area createTransformedArea(AffineTransform transform) {
        return transform.createTransformedShape(this);
    }

    public Area createTransformedArea(java.awt.geom.AffineTransform transform) {
        return new AffineTransform(transform).createTransformedShape(this);
    }
}
