package io.github.phora.aeondroid;

/**
 * Created by phora on 9/10/15.
 */
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/** This draws an equilateral triangle within the set bounds, i.e. setBounds(). The triangle will point in the
 * specified direction. The default direction is NORTH and the default color is black.
 * https://boltingupandroid.wordpress.com/2013/11/08/a-drawable-for-a-directional-equilateral-triangle/
 */
public class EquilateralTriangle extends Drawable {

    private int color = Color.BLACK;
    private Direction direction = Direction.NORTH;
    /**
     *
     */
    public EquilateralTriangle() {
        super();
    }
    /**
     *
     */
    public EquilateralTriangle(int color, Direction direction) {
        super();
        this.color = color;
        this.direction = direction;
    }

    public enum Direction {
        NORTH, SOUTH, EAST, WEST;
    }
    /* (non-Javadoc)
    * @see android.graphics.drawable.Drawable#draw(android.graphics.Canvas)
    */
    @Override
    public void draw(Canvas canvas) {
        Paint p = new Paint();
        p.setStyle(Style.FILL);
        p.setColor(getColor());
        Path path = getEquilateralTriangle();
        canvas.drawPath(path, p);

    }

    /* no-op
    * @see android.graphics.drawable.Drawable#setAlpha(int)
    */
    @Override
    public void setAlpha(int alpha) {
//
    }

    /* no-op
    * @see android.graphics.drawable.Drawable#setColorFilter(android.graphics.ColorFilter)
    */
    @Override
    public void setColorFilter(ColorFilter cf) {
//
    }

    /* Returns zero
    * @see android.graphics.drawable.Drawable#getOpacity()
    */
    @Override
    public int getOpacity() {
//
        return 0;
    }

    /* see http://tech.chitgoks.com/2012/07/08/android-draw-equilateral-triangle-shapes-in-canvas/
    *
    */
    private Path getEquilateralTriangle() {
        Point startPoint = null, p2 = null, p3 = null;
        Rect bounds = getBounds();
        int width = bounds.right - bounds.left;
        switch (direction){
            case NORTH:
                startPoint = new Point(bounds.left, bounds.bottom);
                p2 = new Point(startPoint.x + width, startPoint.y);
                p3 = new Point(startPoint.x + (width / 2), startPoint.y - width);
                break;
            case SOUTH:
                startPoint = new Point(bounds.left, bounds.top);
                p2 = new Point(startPoint.x + width,startPoint.y);
                p3 = new Point(startPoint.x + (width / 2), startPoint.y + width);
                break;
            case EAST:
                startPoint = new Point(bounds.left, bounds.top);
                p2 = new Point(startPoint.x, startPoint.y + width);
                p3 = new Point(startPoint.x - width, startPoint.y + (width / 2));
                break;
            case WEST:
            default:
                startPoint = new Point(bounds.right, bounds.top);
                p2 = new Point(startPoint.x, startPoint.y + width);
                p3 = new Point(startPoint.x + width, startPoint.y + (width / 2));
                break;
        }

        Path path = new Path();
        path.moveTo(startPoint.x, startPoint.y);
        path.lineTo(p2.x, p2.y);
        path.lineTo(p3.x, p3.y);

        return path;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}
