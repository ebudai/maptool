/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.lib.geom;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;

import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.overlay.snap.GeometrySnapper;
import org.locationtech.jts.operation.polygonize.Polygonizer;

public class Area extends java.awt.geom.Area implements Cloneable {

  private static final GeometryFactory FACTORY = new GeometryFactory();

  private Geometry geometry;
  private Rectangle2D bounds;

  public Area() {
    geometry = FACTORY.createGeometryCollection();
  }

  public Area(Geometry geometry) {
    this.geometry = geometry;
  }

  public Area(Rectangle rectangle) {
    Envelope envelope =
        new Envelope(
            rectangle.x,
            rectangle.x + rectangle.width,
            rectangle.y,
            rectangle.y + rectangle.height);
    this.geometry = FACTORY.toGeometry(envelope);
    this.bounds = new Rectangle(rectangle);
  }

  public Area(Area area) {
    super(area);
    if (area.geometry == null) {
      //this happens when loading campaigns saved before the changeover to JTS
      area.geometry = ShapeReader.read(area.getPathIterator(null, 1), FACTORY);
    }
    this.geometry = area.geometry.copy();
    this.geometry = validate(this.geometry);
    if (area.bounds != null) {
      this.bounds = new Rectangle(area.getBounds());
    }
  }

  public Area(GeneralPath path) {
    super(path);
    GeneralPath copy = new GeneralPath(path);
    copy.closePath();
    this.geometry = ShapeReader.read(copy.getPathIterator(null, 1), FACTORY);
    this.geometry = validate(this.geometry);
  }

  public Area(Shape shape) {
    super(shape);
    this.geometry = ShapeReader.read(shape.getPathIterator(null, 1), FACTORY);
    this.geometry = validate(this.geometry);
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
    if (bounds == null) {
      bounds = envelopeToRectangle(geometry.getEnvelopeInternal());
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
    return contains(p.getX(), p.getY());
  }

  public boolean isEmpty() {
    return geometry.isEmpty();
  }

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

  public boolean isSingular() {
    return geometry.getNumGeometries() < 2;
  }

  public void intersect(Area area) {
    geometry = geometry.intersection(area.geometry);
    bounds = null;
  }

  public void transform(AffineTransform transform) {
    geometry = transform.transform(geometry);
    bounds = null;
  }

  public void transform(java.awt.geom.AffineTransform transform) {
    transform(new AffineTransform(transform));
  }

  public Area add(Area area) {
    geometry = geometry.union(area.geometry);
    bounds = null;
    return this;
  }

  public Area subtract(Area area) {
    geometry = geometry.difference(area.geometry);
    bounds = null;
    return this;
  }

  public void reset() {
    geometry = FACTORY.createGeometryCollection();
    bounds = null;
  }

  private static Rectangle2D envelopeToRectangle(Envelope envelope) {
    return new Rectangle2D.Double(
        envelope.getMinX(), envelope.getMinY(), envelope.getWidth(), envelope.getHeight());
  }

  private static Geometry rectangleToGeometry(Rectangle2D rectangle) {
    return rectangleToGeometry(
            rectangle.getX(), rectangle.getX() + rectangle.getWidth(), rectangle.getY(), rectangle.getY() + rectangle.getHeight());
  }

  private static Geometry rectangleToGeometry(double x1, double x2, double y1, double y2) {
    Envelope envelope = new Envelope(x1, x2, y1, y2);
    return FACTORY.toGeometry(envelope);
  }

  public Area createTransformedArea(AffineTransform transform) {
    return transform.createTransformedShape(this);
  }

  public Area createTransformedArea(java.awt.geom.AffineTransform transform) {
    return new AffineTransform(transform).createTransformedShape(this);
  }

  //taken from https://stackoverflow.com/questions/31473553/is-there-a-way-to-convert-a-self-intersecting-polygon-to-a-multipolygon-in-jts
  /**
   * Get / create a valid version of the geometry given. If the geometry is a polygon or multi polygon, self intersections /
   * inconsistencies are fixed. Otherwise the geometry is returned.
   *
   * @param geom
   * @return a geometry
   */
  private static Geometry validate(Geometry geom){
    GeometrySnapper.snapToSelf(geom, Double.MIN_VALUE, true);
    if(geom instanceof Polygon){
      if(geom.isValid()){
        geom.normalize(); // validate does not pick up rings in the wrong order - this will fix that
        return geom; // If the polygon is valid just return it
      }
      Polygonizer polygonizer = new Polygonizer();
      addPolygon((Polygon)geom, polygonizer);
      return toPolygonGeometry(polygonizer.getPolygons());
    }else if(geom instanceof MultiPolygon){
      if(geom.isValid()){
        geom.normalize(); // validate does not pick up rings in the wrong order - this will fix that
        return geom; // If the multipolygon is valid just return it
      }
      Polygonizer polygonizer = new Polygonizer();
      for(int n = geom.getNumGeometries(); n-- > 0;){
        addPolygon((Polygon)geom.getGeometryN(n), polygonizer);
      }
      return toPolygonGeometry(polygonizer.getPolygons());
    }else{
      return geom; // In my case, I only care about polygon / multipolygon geometries
    }
  }

  /**
   * Add all line strings from the polygon given to the polygonizer given
   *
   * @param polygon polygon from which to extract line strings
   * @param polygonizer polygonizer
   */
  private static void addPolygon(Polygon polygon, Polygonizer polygonizer){
    addLineString(polygon.getExteriorRing(), polygonizer);
    for(int n = polygon.getNumInteriorRing(); n-- > 0;){
      addLineString(polygon.getInteriorRingN(n), polygonizer);
    }
  }

  /**
   * Add the linestring given to the polygonizer
   *
   * @param lineString line string
   * @param polygonizer polygonizer
   */
  private static void addLineString(LineString lineString, Polygonizer polygonizer){

    LineString line = lineString;
    if(line instanceof LinearRing){ // LinearRings are treated differently to line strings : we need a LineString NOT a LinearRing
      line = line.getFactory().createLineString(line.getCoordinateSequence());
    }

    // unioning the linestring with the point makes any self intersections explicit.
    Point point = line.getFactory().createPoint(line.getCoordinateN(0));
    Geometry toAdd = line.union(point);

    //Add result to polygonizer
    polygonizer.add(toAdd);
  }

  /**
   * Get a geometry from a collection of polygons.
   *
   * @param polygons collection
   * @return null if there were no polygons, the polygon if there was only one, or a MultiPolygon containing all polygons otherwise
   */
  private static Geometry toPolygonGeometry(Collection<Polygon> polygons){
    switch(polygons.size()){
      case 0:
        return null; // No valid polygons!
      case 1:
        return polygons.iterator().next(); // single polygon - no need to wrap
      default:
        //polygons may still overlap! Need to sym difference them
        Iterator<Polygon> iter = polygons.iterator();
        Geometry ret = iter.next();
        while(iter.hasNext()){
          ret = ret.symDifference(iter.next());
        }
        return ret;
    }
  }
}
