package org.joshy.sketch.tools;

import java.awt.geom.Point2D;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.event.KeyEvent;
import org.joshy.gfx.event.MouseEvent;
import org.joshy.gfx.node.Bounds;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.model.SResizeableNode;
import org.joshy.sketch.model.STrace;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.vector.VectorDocContext;

public class DrawTraceTool extends CanvasTool {

    private boolean dragging;
    private boolean hovered;
    private STrace trace;
    private STrace.TracePoint point;
    private boolean active;
    private Point2D.Double hoverPoint;
    private STrace existingTrace;
    private boolean showTracePoints;
    private STrace.TracePoint dragPoint;

    public DrawTraceTool(VectorDocContext context) {
        super(context);
    }


    @Override
    public void enable() {
        super.enable();
        context.getCanvas().setShowSelection(false);
        context.getPropPanel().setVisible(false);
    }

    @Override
    public void disable() {
        super.disable();
        context.getCanvas().setShowSelection(true);
        context.getPropPanel().setVisible(true);
        existingTrace = null;
        showTracePoints = false;
    }

    @Override
    public void drawOverlay(GFX g) {
        if(hovered) {
            g.setPaint(FlatColor.BLUE);
            g.drawOval(hoverPoint.getX(),hoverPoint.getY(),10,10);
        }
        for(SNode node : context.getDocument().getCurrentPage().getNodes()) {
            for(Point2D pt : node.getSnapPoints()) {
                Bounds b = node.getTransformedBounds();
                double x = b.getX() + b.getWidth()*pt.getX();
                double y = b.getY() + b.getHeight()*pt.getY();
                g.setPaint(FlatColor.GREEN);
                g.drawOval(x-5,y-5,10,10);
            }
        }
        if(showTracePoints && existingTrace != null) {
            for(STrace.TracePoint pt : existingTrace.getPoints()) {
                g.setPaint(FlatColor.PURPLE);
                g.fillOval(
                        existingTrace.getTranslateX() + pt.getX() - 5,
                        existingTrace.getTranslateY() + pt.getY() - 5,
                        10, 10);
            }
        }
    }

    @Override
    protected void call(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KeyCode.KEY_ESCAPE) {
            context.releaseControl();
        }
    }

    @Override
    protected void mouseMoved(MouseEvent event, Point2D.Double cursor) {
        hovered = false;

        if(active && point != null) {
            point.setLocation(cursor);
            context.redraw();
        }


        STrace.SlaveFunction target = findTraceSpot(cursor,trace);
        if(target != null) {
            hovered = true;
            hoverPoint = cursor;
            context.redraw();
        } else {
            if(hovered) {
                hovered = false;
                hoverPoint = null;
                context.redraw();
            }
            hovered = false;
        }
    }
    
    

    @Override
    protected void mousePressed(MouseEvent event, Point2D.Double cursor) {
        if(existingTrace != null) {
            STrace.TracePoint pt = findPoint(existingTrace, cursor);
            if(pt != null) {
                dragPoint = pt;
            }
            return;
        }

        if(active) {
            point.setLocation(cursor);
            STrace.SlaveFunction func = findTraceSpot(cursor,trace);
            if(func != null) {
                endTrace(point,func);
                active = false;
            } else {
                appendPoint(cursor);
            }
            return;
        }

        if(!active) {
            STrace trace = new STrace();
            STrace.SlaveFunction func = findTraceSpot(cursor,trace);
            if(func != null) {
                STrace.TracePoint pt = trace.addPoint(cursor);
                trace.addSlaveFunction(pt,func);
                SketchDocument doc = context.getDocument();
                doc.getCurrentPage().add(trace);
                this.trace = trace;
                point = trace.addPoint(cursor);
                context.redraw();
                active = true;
            }
        }
    }

    private STrace.TracePoint findPoint(STrace existingTrace, Point2D.Double cursor) {
        for(STrace.TracePoint pt : existingTrace.getPoints()) {
            double x = cursor.getX() - existingTrace.getTranslateX();
            double y = cursor.getY() - existingTrace.getTranslateY();
            if(Math.abs(pt.distance(x,y)) < 10) {
                return pt;
            }
        }
        return null;
    }

    private STrace.SlaveFunction findTraceSpot(final Point2D.Double cursor, final STrace trace) {
        for(final SNode node : context.getDocument().getCurrentPage().getNodes()) {
            if(node == trace) continue;

            //first go through the real snap points
            for(final Point2D pt : node.getSnapPoints()) {
                Bounds b = node.getTransformedBounds();
                final SNode nd = node;
                double x = b.getX() + b.getWidth()*pt.getX();
                double y = b.getY() + b.getHeight()*pt.getY();
                if(within(x,y,cursor,10)){
                    return new STrace.SlaveFunction() {
                        public void apply(STrace.TracePoint point, SNode trace) {
                            Bounds b = node.getTransformedBounds();
                            double x = b.getX() + b.getWidth()*pt.getX();
                            double y = b.getY() + b.getHeight()*pt.getY();
                            point.setLocation(x-trace.getTranslateX(),y-trace.getTranslateY());
                        }
                    };
                }
            }
            
            if(! (node instanceof SResizeableNode)) continue;

            /*
            final SResizeableNode rect = (SResizeableNode) node;
            //the corners
            if(within(rect.getTranslateX() + rect.getX(), rect.getTranslateY() + rect.getY(), cursor, 10)) {
                return new STrace.SlaveFunction(){
                    public void apply() {
                        cursor.setLocation(
                                rect.getTranslateX()+rect.getX()-trace.getTranslateX(),
                                rect.getTranslateY()+rect.getY()-trace.getTranslateY()
                        );
                    }
                };
            }

            // top edge midpoint
            if(within(rect.getTranslateX() + rect.getX() + rect.getWidth()/2.0,
                    rect.getTranslateY() + rect.getY(), cursor, 10)) {
                return new STrace.SlaveFunction() {
                    public void apply() {
                        cursor.setLocation(
                                rect.getTranslateX()+rect.getX()+rect.getWidth()/2.0-trace.getTranslateX(),
                                rect.getTranslateY()+rect.getY()-trace.getTranslateY()
                        );
                    }
                };
            }
            // bottom edge midpoint
            if(within(rect.getTranslateX() + rect.getX() + rect.getWidth()/2.0,
                    rect.getTranslateY() + rect.getY()+rect.getHeight(), cursor, 10)) {
                return new STrace.SlaveFunction() {
                    public void apply() {
                        cursor.setLocation(
                                rect.getTranslateX()+rect.getX()+rect.getWidth()/2.0-trace.getTranslateX(),
                                rect.getTranslateY()+rect.getY()+rect.getHeight()-trace.getTranslateY()
                        );
                    }
                };
            }

            // left edge midpoint
            if(within(rect.getTranslateX() + rect.getX(),
                    rect.getTranslateY() + rect.getY()+rect.getHeight()/2.0, cursor, 10)) {
                return new STrace.SlaveFunction() {
                    public void apply() {
                        cursor.setLocation(
                                rect.getTranslateX()+rect.getX()-trace.getTranslateX(),
                                rect.getTranslateY()+rect.getY()+rect.getHeight()/2.0-trace.getTranslateY()
                        );
                    }
                };
            }

            // right edge midpoint
            if(within(rect.getTranslateX() + rect.getX()+rect.getWidth(),
                    rect.getTranslateY() + rect.getY()+rect.getHeight()/2.0, cursor, 10)) {
                return new STrace.SlaveFunction() {
                    public void apply() {
                        cursor.setLocation(
                                rect.getTranslateX()+rect.getX()+rect.getWidth()-trace.getTranslateX(),
                                rect.getTranslateY()+rect.getY()+rect.getHeight()/2.0-trace.getTranslateY()
                        );
                    }
                };
            }
            */
            /*
            if(within(rect.getTranslateX() + rect.getX()+rect.getWidth(), rect.getTranslateY() + rect.getY(), cursor, 10)) return rect;
            if(within(rect.getTranslateX() + rect.getX(), rect.getTranslateY() + rect.getY()+rect.getHeight(), cursor, 10)) return rect;
            if(within(rect.getTranslateX() + rect.getX()+rect.getWidth(), rect.getTranslateY() + rect.getY()+rect.getHeight(), cursor, 10)) return rect;
            //the centers
            if(within(rect.getTranslateX() + rect.getX() + rect.getWidth()/2.0, rect.getTranslateY() + rect.getY(), cursor, 10)) return rect;
            if(within(rect.getTranslateX() + rect.getX() + rect.getWidth()/2.0, rect.getTranslateY() + rect.getY()+rect.getHeight(), cursor, 10)) return rect;
            if(within(rect.getTranslateX() + rect.getX(), rect.getTranslateY() + rect.getY() + rect.getHeight()/2.0, cursor, 10)) return rect;
            if(within(rect.getTranslateX() + rect.getX() + rect.getWidth(), rect.getTranslateY() + rect.getY() + rect.getHeight()/2.0, cursor, 10)) return rect;
            */
        }
        return null;
    }

    private boolean within(double v, double v1, Point2D.Double cursor, double delta) {
        if(Math.abs(cursor.distance(v,v1)) < delta) {
            return true;
        }
        return false;
    }

    private void endTrace(STrace.TracePoint cursor, STrace.SlaveFunction func) {
        trace.addSlaveFunction(cursor, func);
        trace = null;
        context.releaseControl();
    }

    private void appendPoint(Point2D.Double cursor) {
        point = trace.addPoint(cursor);
        context.redraw();
    }

    @Override
    protected void mouseDragged(MouseEvent event, Point2D.Double cursor) {
        if(dragPoint != null) {
            dragPoint.setLocation(
                    cursor.getX()-existingTrace.getTranslateX(),
                    cursor.getY()-existingTrace.getTranslateY());
        }
    }

    @Override
    protected void mouseReleased(MouseEvent event, Point2D.Double cursor) {
        if(dragPoint != null) {
            //if first
            if(existingTrace.getPoints().get(0) == dragPoint ||
                    existingTrace.getPoints().get(existingTrace.getPoints().size()-1)==dragPoint
                    ) {
                STrace.SlaveFunction spot = findTraceSpot(cursor, existingTrace);
                if(spot != null) {
                    existingTrace.removeSlaveFunction(dragPoint);
                    existingTrace.addSlaveFunction(dragPoint,spot);
                } else {
                    existingTrace.removeSlaveFunction(dragPoint);
                }
            }
        }
        dragPoint = null;
    }

    public void startEditing(STrace trace) {
        existingTrace = trace;
        showTracePoints = true;
        context.redraw();
    }
}
