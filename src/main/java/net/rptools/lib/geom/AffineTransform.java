package net.rptools.lib.geom;

import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.geom.util.NoninvertibleTransformationException;
import java.awt.*;
import java.awt.geom.NoninvertibleTransformException;

public class AffineTransform extends java.awt.geom.AffineTransform {

    private static final GeometryFactory FACTORY = new GeometryFactory();

    private AffineTransformation transform = new AffineTransformation();

    public AffineTransform() {

    }

    public AffineTransform(java.awt.geom.AffineTransform transform) {
        var matrix = new double[6];
        transform.getMatrix(matrix);
        this.transform = new AffineTransformation(matrix);
    }

    public AffineTransform(double m00, double m10,
                           double m01, double m11,
                           double m02, double m12) {
        var matrix = new double[] { m00, m10, m01, m11, m02, m12 };
        this.transform = new AffineTransformation(matrix);
    }

    public void translate(double x, double y) {
        transform = transform.translate(x, y);
    }

    public void scale(double x, double y) {
        transform = transform.scale(x, y);
    }

    public void concatenate(AffineTransform transform) {
        this.transform.compose(transform.transform);
    }

    public Area createTransformedShape(Area area) {
        return new Area(transform(area.getGeometry()));
    }

    public double[] getMatrix() {
        return transform.getMatrixEntries();
    }

    public Area createTransformedShape(Shape path) {
        ShapeReader reader = new ShapeReader(FACTORY);
        Geometry geometry = reader.read(path.getPathIterator(null, 1));
        return new Area(transform(geometry));
    }

    public Geometry transform(Geometry geometry) {
        return transform.transform(geometry);
    }

    @Override
    public void rotate(double radians, double x, double y) {
        transform.rotate(radians, x, y);
    }

    @Override
    public java.awt.geom.AffineTransform createInverse() throws NoninvertibleTransformException {
        var inverse = new java.awt.geom.AffineTransform();
        try {
            transform.getInverse();
        } catch (NoninvertibleTransformationException nte) {
            throw new NoninvertibleTransformException(nte.getMessage());
        }
        return inverse;
    }

    public static AffineTransform getTranslateInstance(double x, double y) {
        AffineTransform translation = new AffineTransform();
        translation.transform = AffineTransformation.translationInstance(x, y);
        return translation;
    }

    public static AffineTransform getRotateInstance(double radians) {
        AffineTransform rotation = new AffineTransform();
        rotation.transform = AffineTransformation.rotationInstance(radians);
        return rotation;
    }

    public static AffineTransform getRotateInstance(double radians, double x, double y) {
        AffineTransform rotation = new AffineTransform();
        rotation.transform = AffineTransformation.rotationInstance(radians, x, y);
        return rotation;
    }

    public static AffineTransform getScaleInstance(double x, double y) {
        AffineTransform translation = new AffineTransform();
        translation.transform = AffineTransformation.scaleInstance(x, y);
        return translation;
    }
}
