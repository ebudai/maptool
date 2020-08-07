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
package net.rptools.maptool.client.ui.zone.vbl;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

import net.rptools.maptool.util.GraphicsUtil;
import org.jetbrains.annotations.NotNull;

public class VisibleAreaSegment implements Comparable<VisibleAreaSegment> {
  private final Point2D origin;
  private final List<AreaFace> faceList = new ArrayList<>();
  private final double EPSILON = 0.01;

  private Point2D centerPoint;
  private Area pathArea;

  public VisibleAreaSegment(Point2D origin) {
    this.origin = origin;
  }

  public void addAtEnd(AreaFace face) {
    faceList.add(face);
  }

  public void addAtFront(AreaFace face) {
    faceList.add(0, face);
  }

  public long getDistanceFromOrigin() {
    // return GeometryUtil.getDistance(getCenterPoint(), origin);
    return (long) (getCenterPoint().distance(origin) * 1000);
  }

  public long getDistanceSqFromOrigin() {
    return (long) getCenterPoint().distanceSq(origin);
  }

  public Point2D getCenterPoint() {
    if (centerPoint == null) {
      Area path = getPath();
      Rectangle2D bounds = path.getBounds2D();
      // Jamz: getCenter points now available from class
      // centerPoint = new Point2D.Double(bounds.getX() + bounds.getWidth() / 2.0, bounds.getY() +
      // bounds.getHeight() / 2.0);
      centerPoint = new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
    }
    return centerPoint;
  }

  public Area getPath() {
    if (faceList.isEmpty()) {
      return new Area();
    }
    if (pathArea == null) {
      int size = faceList.size() + 1;
      Point2D[] pathPoints = new Point2D[size];
      pathPoints[0] = faceList.get(0).getP1();
      for (int i = 1; i < size; i++) {
        pathPoints[i] = faceList.get(i - 1).getP2();
      }
      GeneralPath path = new GeneralPath();
      path.moveTo((float) pathPoints[0].getX(), (float) pathPoints[0].getY());
      for (int i = 1; i < pathPoints.length; i++) {
        path.lineTo((float) pathPoints[i].getX(), (float) pathPoints[i].getY());
      }
      Stroke stroke = new BasicStroke(1);
      pathArea = new Area(stroke.createStrokedShape(path));
    }
    return pathArea;
  }

  public Area getArea() {
    if (faceList.isEmpty()) {
      return new Area();
    }
    Deque<Point2D> pathPoints = new ArrayDeque<>(faceList.size() * 2);
    Point2D first = faceList.get(0).getP1();
    pathPoints.offer(GraphicsUtil.getProjectedPoint(origin, first, Integer.MAX_VALUE / 2));
    pathPoints.offer(first);
    for (AreaFace face : faceList) {
      // Initial point
      // Add to the path
      pathPoints.offer(face.getP2());
      pathPoints.offerFirst(GraphicsUtil.getProjectedPoint(origin, face.getP2(), Integer.MAX_VALUE / 2));
    }
    // System.out.println("Skipped: " + skipCount);

    GeneralPath path = new GeneralPath();
    first = pathPoints.pop();
    path.moveTo((float) first.getX(), (float) first.getY());
    for (Point2D p : pathPoints) {
      path.lineTo((float) p.getX(), (float) p.getY());
    }
    return new Area(path);
  }

  ////
  // COMPARABLE
  public int compareTo(@NotNull VisibleAreaSegment o) {
    if (o != this) {
      // Jamz: We're getting the following exception from this compare:
      // java.lang.IllegalArgumentException: Comparison method violates its general contract!
      // So we changed getDistanceFromOrigin() to return a long after multiplying by 1000 for
      // precision
      long odist = o.getDistanceSqFromOrigin();
      long val = getDistanceSqFromOrigin() - odist; // separate variable for debugging
      return (int) val;
      // return val < EPSILON && val > -EPSILON ? 0 : (int) val; // Should we use an EPSILON value?
      // return getDistanceFromOrigin() < odist ? -1 : getDistanceFromOrigin() > odist ? 1 : 0;
    }
    return 0;
  }
}
