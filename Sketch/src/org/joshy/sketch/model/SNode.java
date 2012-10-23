package org.joshy.sketch.model;

import java.util.ArrayList;
import java.util.List;
import org.joshy.gfx.node.Bounds;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

/**
 * The base node for all graphics. Contains standard transforms for translate, rotate, scale
 * as well as a function to clone nodes.
 *
 * getBounds returns the visible bounds of the node without any included transforms
 * getTransformedBounds returns the visible bounds of the node including all transforms
 *
 *
 */
public abstract class SNode {
    private double translateX = 0.0;
    private double translateY = 0.0;
    protected Map<String,String> props = new HashMap<String,String>();
    protected List<Point2D> snapPoints = new ArrayList<Point2D>();
    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private double rotate = 0.0;
    private double anchorX = 0;
    private double anchorY = 0;
    private String id = null;
    private String linkTarget;
    private boolean locked;
    private boolean visible = true;
    private List<NodeListener> listeners = new ArrayList<NodeListener>();

    public abstract Bounds getBounds();
    public abstract Bounds getTransformedBounds();

    public abstract boolean contains(Point2D point);

    public double getTranslateX() {
        return translateX;
    }

    public void setTranslateX(double translateX) {
        if(isLocked()) return;
        this.translateX = translateX;
    }

    public double getTranslateY() {
        return translateY;
    }

    public void setTranslateY(double translateY) {
        if(isLocked()) return;
        this.translateY = translateY;
    }

    public double getAnchorX() {
        return anchorX;
    }

    public void setAnchorX(double anchorX) {
        this.anchorX = anchorX;
    }

    public double getAnchorY() {
        return anchorY;
    }

    public void setAnchorY(double anchorY) {
        this.anchorY = anchorY;
    }

    public SNode duplicate(SNode dupe) {
        if(dupe == null) throw new IllegalArgumentException("SShape.duplicate: duplicate shape argument can't be null!");
        dupe.setTranslateX(this.getTranslateX());
        dupe.setTranslateY(this.getTranslateY());
        dupe.setRotate(this.getRotate());
        dupe.setScaleX(this.getScaleX());
        dupe.setScaleY(this.getScaleY());
        dupe.setAnchorX(this.getAnchorX());
        dupe.setAnchorY(this.getAnchorY());
        return dupe;
    }

    public String getStringProperty(String key) {
        return props.get(key);
    }

    public void setStringProperty(String key, String value) {
        props.put(key,value);
    }


    public boolean getBooleanProperty(String key) {
        String str =  props.get(key);
        if( str == null) return false;
        return Boolean.parseBoolean(str);
    }

    public void setBooleanProperty(String key, boolean value) {
        props.put(key,Boolean.toString(value));
    }

    public Map getProperties() {
        return props;
    }

    public double getScaleX() {
        return scaleX;
    }

    public void setScaleX(double scaleX) {
        this.scaleX = scaleX;
    }

    public double getScaleY() {
        return scaleY;
    }

    public void setScaleY(double scaleY) {
        this.scaleY = scaleY;
    }

    public void setRotate(double rotate) {
        this.rotate = rotate;
    }

    public double getRotate() {
        return rotate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLinkTarget(String linkTarget) {
        this.linkTarget = linkTarget;
    }

    public boolean isLink() {
        return linkTarget != null;
    }

    public String getLinkTarget() {
        return linkTarget;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void addListener(NodeListener gradientHandle) {
        listeners.add(gradientHandle);
    }

    protected void fireUpdate() {
        if(listeners != null) {
            for(NodeListener c : listeners) {
                c.changed(this);
            }
        }
    }

    public void removeListener(NodeListener gradientHandle) {
        listeners.remove(gradientHandle);
    }

    public List<Point2D> getSnapPoints() {
        return snapPoints;
    }

    public void addSnapPoint(Point2D.Double pt) {
        this.snapPoints.add(pt);
    }

    public void removeSnapPoint(Point2D currentPoint) {
        this.snapPoints.remove(currentPoint);
    }
}
