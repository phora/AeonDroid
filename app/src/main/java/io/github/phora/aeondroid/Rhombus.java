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
public class Rhombus extends Drawable {

    private int color = Color.BLACK;
    private Direction direction = Direction.VERTICAL;
    /**
     *
     */
    public Rhombus() {
        super();
    }
    /**
     *
     */
    public Rhombus(int color, Direction direction) {
        super();
        this.color = color;
        this.direction = direction;
    }

    public enum Direction {
        VERTICAL, HORIZONTAL;
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
        Point startPoint = null, p2 = null, p3 = null, p4 = null;
        Rect bounds = getBounds();
        int width = bounds.right - bounds.left;
        switch (direction){
            case VERTICAL:
                startPoint = new Point(bounds.left + (width / 2), bounds.bottom);
                p2 = new Point(startPoint.x - (width / 4), startPoint.y - (width / 2));
                p3 = new Point(startPoint.x, bounds.top);
                p4 = new Point(startPoint.x + (width / 4), startPoint.y - (width / 2));
                break;
            case HORIZONTAL:
                startPoint = new Point(bounds.left, bounds.bottom - (width / 2));
                p2 = new Point(startPoint.x + (width / 2), startPoint.y - (width / 4));
                p3 = new Point(bounds.right, startPoint.y);
                p4 = new Point(startPoint.x + (width / 2), startPoint.y + (width / 4));
                break;
            default:
                startPoint = new Point(bounds.left + (width / 2), bounds.bottom);
                p2 = new Point(startPoint.x - (width / 4), startPoint.y - (width / 2));
                p3 = new Point(startPoint.x, bounds.top);
                p4 = new Point(startPoint.x + (width / 4), startPoint.y - (width / 2));
                break;
        }

        Path path = new Path();
        path.moveTo(startPoint.x, startPoint.y);
        path.lineTo(p2.x, p2.y);
        path.lineTo(p3.x, p3.y);
        path.lineTo(p4.x, p4.y);

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
