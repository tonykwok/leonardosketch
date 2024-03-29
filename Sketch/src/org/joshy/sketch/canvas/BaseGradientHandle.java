package org.joshy.sketch.canvas;

import org.joshy.gfx.draw.*;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.ChangedEvent;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.layout.Container;
import org.joshy.gfx.util.GeomUtil;
import org.joshy.sketch.controls.AlphaPicker;
import org.joshy.sketch.controls.FreerangeColorPicker;
import org.joshy.sketch.model.AbstractResizeableNode;
import org.joshy.sketch.model.Handle;
import org.joshy.sketch.model.SShape;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.tools.MouseEventHandle;
import org.joshy.sketch.util.DrawUtils;
import org.joshy.sketch.util.Util;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: 1/15/11
 * Time: 4:19 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseGradientHandle<G extends MultiGradientFill>
        extends Handle
        implements MouseEventHandle, SShape.SShapeListener {
    protected SShape shape;
    protected VectorDocContext context;
    protected Point2D.Double hoverPoint;
    protected boolean hovered;
    protected RadialGradientFill.Stop activeStop;
    protected boolean onStop;
    protected boolean onPoint;
    protected boolean draggingStop;
    protected Point2D.Double startPoint;
    protected Map<MultiGradientFill.Stop, Control> colorControlMap;
    protected Map<MultiGradientFill.Stop,  Control> alphaControlMap;
    protected ArrayList<Control> controls;
    protected boolean showAddIndicator;
    protected boolean couldDelete;
    private MultiGradientFill.Stop hoverStop;
    private double currentScale;
    private double currentPanX;
    private double currentPanY;

    public BaseGradientHandle(SShape shape, VectorDocContext context) {
        this.shape = shape;
        this.context = context;
        this.shape.addListener(this);
        controls = new ArrayList<Control>();
        colorControlMap = new HashMap<MultiGradientFill.Stop,Control>();
        alphaControlMap = new HashMap<MultiGradientFill.Stop,Control>();
        for(final MultiGradientFill.Stop stop : getFill().getStops()) {
            addStopControl(stop);
        }
        updateControlPositions();
    }

    protected G getFill() {
        return (G) shape.getFillPaint();
    }

    protected void addStopControl(final MultiGradientFill.Stop stop) {
        FreerangeColorPicker colorPopup = new FreerangeColorPicker();
        colorPopup.setSelectedColor(stop.getColor());

        colorPopup.setPrefWidth(10);
        colorPopup.setPrefHeight(10);
        controls.add(colorPopup);
        colorControlMap.put(stop,colorPopup);
        colorPopup.onColorSelected(new Callback<ChangedEvent>() {
            public void call(ChangedEvent changedEvent) throws Exception {
                FlatColor val = (FlatColor) changedEvent.getValue();
                if (val == null) {
                    val = FlatColor.WHITE_TRANSPARENT;
                }

                stop.setColor(val.deriveWithAlpha(stop.getColor().getAlpha()));
                refresh();
            }
        });

        AlphaPicker c2 = new AlphaPicker();
        c2.setAlpha(stop.getColor().getAlpha());
        c2.onAlphaSelected(new Callback<ChangedEvent>() {
            public void call(ChangedEvent changedEvent) throws Exception {
                Double d = (Double) changedEvent.getValue();
                stop.setColor(stop.getColor().deriveWithAlpha(d));
                refresh();
            }
        });
        c2.setPrefWidth(10).setPrefHeight(10);
        controls.add(c2);
        alphaControlMap.put(stop,c2);
    }

    protected void removeStopControl(MultiGradientFill.Stop activeStop) {
        Control colorPopup = colorControlMap.get(activeStop);
        controls.remove(colorPopup);
        colorControlMap.remove(activeStop);
        Container popupLayer = context.getSketchCanvas().getParent().getStage().getPopupLayer();
        popupLayer.remove(colorPopup);

        Control alphaPopup = alphaControlMap.get(activeStop);
        controls.remove(alphaPopup);
        alphaControlMap.remove(activeStop);
        popupLayer.remove(alphaPopup);
    }

    protected void refresh() {
        shape.setFillPaint(shape.getFillPaint());
    }

    abstract void updateControlPositions();

    @Override
    public boolean hasControls() {
        return true;
    }

    @Override
    public Iterable<? extends Control> getControls() {
        return controls;
    }

    @Override
    public void detach() {
        if(shape instanceof AbstractResizeableNode) {
            ((AbstractResizeableNode)this.shape).removeListener(this);
        }
    }

    public void mouseMoved(boolean hovered, MouseEvent event, Point2D.Double cursor) {
        this.hovered = hovered;
        hoverPoint = cursor;
        showAddIndicator = true;
        hoverStop = null;
        for(MultiGradientFill.Stop stop: getFill().getStops()) {
            Point2D pt = Util.interpolatePoint(getStart(), getEnd(), stop.getPosition());
            if(pt.distance(cursor) < 10) {
                showAddIndicator = false;
                hoverStop = stop;
            }
        }
    }


    public boolean contains(double x, double y, double scale) {
        double size = this.size/scale;
        if(getEnd().distance(x,y) < 5) {
            return false;
        }
        Line2D.Double line = new Line2D.Double(getStart(),getEnd());
        if(line.ptSegDist(x,y) < size) {
            return true;
        } else {
            return false;
        }
    }

    public void mouseReleased(MouseEvent event, Point2D.Double cursor) {
        if(onStop && !draggingStop) {
            //u.p("did a click");
        } else {
            //u.p("did a drag");
        }
        activeStop = null;
        onStop = false;
        onPoint = false;
        draggingStop = false;
        startPoint = null;
    }

    public void changed() {
        refresh();
        updateControlPositions();
    }


    @Override
    public void draw(GFX g, SketchCanvas canvas) {
        if(!(shape.getFillPaint() instanceof MultiGradientFill)) return;

        //update the position of the handles if the zoom or pan has changed
        if(canvas.getScale() != currentScale || canvas.getPanX() != currentPanX || canvas.getPanY() != currentPanY) {
            currentScale = canvas.getScale();
            currentPanX = canvas.getPanX();
            currentPanY = canvas.getPanY();
            changed();
        }

        Point2D pt = new Point2D.Double(getX(),getY());
        pt = canvas.transformToDrawing(pt);

        double x = pt.getX();
        double y = pt.getY();

        drawConnectingLine(g, canvas);
        /*
        if(hovered) {
            if(hoverPoint.getY()-shape.getBounds().getY()-getY() < 5) {
                DrawUtils.drawStandardHandle(g, x, y, FlatColor.GREEN);
            } else {
                DrawUtils.drawStandardHandle(g, x, y, FlatColor.RED);
            }
        } else {
            DrawUtils.drawStandardHandle(g, x, y, FlatColor.GREEN);
        }
          */
        DrawUtils.drawStandardHandle(g, x, y, FlatColor.GREEN);

        drawStopHandles(g, canvas);
        if(showAddIndicator && hovered) {
            drawNewStopIndicator(g, canvas);
        }
    }

    protected void drawNewStopIndicator(GFX g, SketchCanvas canvas) {
        Point2D closest = closestPoint(getStart(), getEnd(), hoverPoint);
        Point2D hp = canvas.transformToDrawing(closest.getX(), closest.getY());
        g.translate(hp.getX(),hp.getY());

        double angle = GeomUtil.calcAngle(getStart(), getEnd());
        g.rotate(-angle, Transform.Z_AXIS);
        Path2D.Double path = createHandlePath();
        g.setPaint(FlatColor.WHITE);
        g.fillPath(path);
        g.setPaint(FlatColor.BLACK);
        g.drawPath(path);
        g.rotate(angle, Transform.Z_AXIS);
        g.translate(-hp.getX(),-hp.getY());
    }

    private static Point2D closestPoint(Point2D a, Point2D b, Point2D c) {
        double theta1 = GeomUtil.calcAngle(a,c);
        double theta2 = GeomUtil.calcAngle(a,b);
        double theta = theta2-theta1;

        double ac = a.distance(c);
        double ad = Math.cos(Math.toRadians(theta))*ac;
        return GeomUtil.calcPoint(a, GeomUtil.calcAngle(a, b), ad);
    }

    protected void drawStopHandles(GFX g, SketchCanvas canvas) {
        g.setPureStrokes(true);
        //draw the handles
        for(MultiGradientFill.Stop stop: getFill().getStops()) {
            if(getFill().isFirst(stop)) continue;
            if(getFill().isLast(stop)) continue;
            Point2D pt = getDragHandlePosition(stop,canvas);
            g.translate(pt.getX(),pt.getY());

            double angle = GeomUtil.calcAngle(getStart(),getEnd());
            g.rotate(-angle, Transform.Z_AXIS);
            Path2D.Double path = createHandlePath();

            //fill stop
            g.setPaint(stop.getColor());
            if(stop == hoverStop) {
                g.setPaint(FlatColor.WHITE);
            }
            if(stop == activeStop) {
                g.setPaint(FlatColor.WHITE);
                if(couldDelete) {
                    g.setPaint(FlatColor.GRAY);
                }
            }
            g.fillPath(path);

            //stroke stop
            g.setPaint(FlatColor.BLACK);
            if(stop == activeStop && couldDelete) {
                g.setPaint(FlatColor.GRAY);
            }
            g.drawPath(path);


            g.rotate(angle, Transform.Z_AXIS);
            g.translate(-pt.getX(),-pt.getY());
        }
        g.setPureStrokes(false);
    }

    private Path2D.Double createHandlePath() {
        Path2D.Double path = new Path2D.Double();
        double s = 9;
        path.moveTo(0,-s/2);
        path.lineTo(s, 0);
        path.lineTo(0,s/2);
        path.lineTo(-s,0);
        path.closePath();
        return path;
    }

    protected abstract Point2D getDragHandlePosition(MultiGradientFill.Stop stop, SketchCanvas canvas);

    protected void drawConnectingLine(GFX g, SketchCanvas canvas) {
        Point2D s = getConnectingLineStart();
        Point2D e = getConnectingLineEnd();
        //draw the gradient line
        g.setPaint(FlatColor.BLACK);
        g.drawLine(s.getX(), s.getY(), e.getX(), e.getY());
        g.setPaint(FlatColor.WHITE);
        g.drawLine(s.getX()+1, s.getY()+1, e.getX()+1,e.getY()+1);
    }

    abstract Point2D getConnectingLineStart();

    abstract Point2D getConnectingLineEnd();

    protected abstract Point2D getStart();

    protected abstract Point2D getEnd();

    protected void addStop(double dx) {
        //find previous and next stops
        FlatColor prevcolor = FlatColor.GREEN;
        FlatColor nextcolor = FlatColor.GREEN;
        for(int i=0; i<getFill().getStops().size(); i++) {
            MultiGradientFill.Stop stop = getFill().getStops().get(i);
            if(stop.getPosition() < dx) {
                prevcolor = stop.getColor();
            }
            if(stop.getPosition() >= dx) {
                nextcolor = stop.getColor();
                break;
            }
        }
        FlatColor newcolor = interpolateColor(prevcolor,nextcolor,0.5);
        MultiGradientFill.Stop stop = new MultiGradientFill.Stop(dx, newcolor);
        getFill().addStop(stop);
        addStopControl(stop);
        context.getSelection().regenHandleControls(shape);
        updateControlPositions();
    }

    private FlatColor interpolateColor(FlatColor prevcolor, FlatColor nextcolor, double v) {
        return new FlatColor(
                interpolate(prevcolor.getRed(), nextcolor.getRed(), v),
                interpolate(prevcolor.getGreen(), nextcolor.getGreen(), v),
                interpolate(prevcolor.getBlue(), nextcolor.getBlue(), v),
                interpolate(prevcolor.getAlpha(), nextcolor.getAlpha(), v)
                );
    }

    private double interpolate(double a, double b, double t) {
        return (b-a)*t + a;
    }
}
