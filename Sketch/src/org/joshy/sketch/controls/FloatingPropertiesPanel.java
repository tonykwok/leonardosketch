package org.joshy.sketch.controls;

import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.GradientFill;
import org.joshy.gfx.event.*;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.NodeUtils;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.OSUtil;
import org.joshy.sketch.Main;
import org.joshy.sketch.canvas.Selection;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.vector.VectorDocContext;
import org.joshy.sketch.property.PropertyManager;

import java.awt.geom.Point2D;
import java.io.IOException;

import static org.joshy.gfx.util.localization.Localization.getString;

public class FloatingPropertiesPanel extends HFlexBox {
    private boolean selected = false;
    private Selection selection;
    private SwatchColorPicker colorButton;
    private Slider fillOpacitySlider;
    private SwatchColorPicker strokeColorButton;
    private Slider strokeWidthSlider;
    private Slider fontSizeSlider;
    private Togglebutton fontBoldButton;
    private Togglebutton fontItalicButton;
    private Togglebutton rectPropsButton;
    private Main manager;
    private PopupMenuButton<String> fontPicker;
    private Label fillOpacityLabel;
    private Label strokeWidthLabel;
    private VectorDocContext context;
    private FlatColor gradient1 = new FlatColor(0,0,0,0);
    private static final GradientFill GRADIENT1 =new GradientFill(FlatColor.GRAY, FlatColor.GREEN,0,true, 50,0,50,100);
    private PopupMenuButton<SArrow.HeadEnd> arrowHeadEnd;
    private Label rgbLabel;
    private Label fontSizeLabel;
    public static final boolean TRUE_PALLETTE = OSUtil.isMac();


    public FloatingPropertiesPanel(final Main manager, final VectorDocContext context) throws IOException {
        this.context = context;
        setFill(FlatColor.RED);
        setWidth(200);
        setHeight(200);
        this.manager = manager;
        selected = false;
        EventBus.getSystem().addListener(Selection.SelectionChangeEvent.Changed, new Callback<Selection.SelectionChangeEvent>(){
            public void call(Selection.SelectionChangeEvent event) {
                //only pay attention to events for our own context doc
                if(event.getSelection().getDocument() != context.getDocument()) return;
                if(!manager.propMan.isClassAvailable(SNode.class)) return;
                selected = !event.getSelection().isEmpty();
                selection = event.getSelection();
                updatePanelContents();
                setDrawingDirty();
                setLayoutDirty();
            }
        });


        colorButton = new SwatchColorPicker();
        colorButton.addCustomSwatch(new SwatchColorPicker.CustomSwatch(){
            public void draw(GFX gfx, double x, double y, double w, double h) {
                gfx.setPaint(GRADIENT1.derive(x,y,x,y+h));
                gfx.fillRect(x,y,w,h);
            }

            public FlatColor getColor() {
                return gradient1;
            }
        });
        colorButton.setOutsideColorCallback(new SwatchColorPicker.ColorCallback(){
            public FlatColor call(MouseEvent event) {
                SketchDocument doc = context.getDocument();
                Stage stage = context.getSketchCanvas().getParent().getStage();
                Point2D point = event.getPointInScreenCoords();
                point = new Point2D.Double(point.getX()-stage.getX(),point.getY()-stage.getY());
                point = NodeUtils.convertFromScene(context.getCanvas(),point);
                if(rgbLabel == null) {
                    rgbLabel = new Label("RGB");
                    rgbLabel.setId("rgblabel");
                    stage.getPopupLayer().add(rgbLabel);
                    rgbLabel.setTranslateX(100);
                    rgbLabel.setTranslateY(100);
                }
                rgbLabel.setVisible(true);
                Point2D dx = event.getPointInScreenCoords();

                rgbLabel.setTranslateX(dx.getX()-stage.getX()+70);
                rgbLabel.setTranslateY(dx.getY()-stage.getY());
                for(SNode node : doc.getCurrentPage().model) {
                    if(node.contains(point) && node instanceof SShape) {
                        FlatColor color =  (FlatColor) ((SShape)node).getFillPaint();
                        int val = color.getRGBA();
                        val = val & 0x00FFFFFF;
                        rgbLabel.setText("RGB: " + Integer.toHexString(val).toUpperCase());
                        return color;
                    }
                }
                rgbLabel.setText("RGB: ------");
                return FlatColor.BLUE;
            }
        });
        EventBus.getSystem().addListener(colorButton,ChangedEvent.FinalChange, new Callback<ChangedEvent>() {
            public void call(ChangedEvent changedEvent) throws Exception {
                if(rgbLabel != null) {
                    rgbLabel.setVisible(false);
                }
            }
        });
        strokeColorButton = new SwatchColorPicker();
        EventBus.getSystem().addListener(ChangedEvent.ColorChanged, new Callback<ChangedEvent>() {
            public void call(ChangedEvent event) {
                if(event.getSource() == colorButton) {
                    FlatColor color = (FlatColor) event.getValue();
                    if(manager.propMan.isClassAvailable(SShape.class)) {
                        if(color == gradient1) {
                            if(manager.propMan.isClassAvailable(SRect.class)) {
                                PropertyManager.Property prop = manager.propMan.getProperty("fillPaint");
                                if(prop.hasSingleValue() && prop.getValue() instanceof GradientFill) {
                                    //do nothing
                                } else {
                                    prop.setValue(GRADIENT1);
                                }
                            }
                            //don't set the color if its a gradient but this isn't a rect
                        } else {
                            manager.propMan.getProperty("fillPaint").setValue(color);
                        }
                        Selection sel = context.getSelection();
                        if(sel.size() == 1){
                            sel.regenHandles(sel.firstItem());
                        }
                    }
                    context.redraw();
                }
                if(event.getSource() == strokeColorButton) {
                    if(manager.propMan.isClassAvailable(SShape.class) ||
                            manager.propMan.isClassAvailable(SImage.class)) {
                        manager.propMan.getProperty("strokePaint").setValue(event.getValue());
                        context.redraw();
                    }
                }
            }
        });
        add(colorButton);
        add(strokeColorButton);

        fillOpacitySlider = new Slider(false);
        fillOpacitySlider.setMin(0);
        fillOpacitySlider.setMax(100);
        fillOpacitySlider.setValue(100);
        EventBus.getSystem().addListener(fillOpacitySlider, ChangedEvent.DoubleChanged, new Callback<ChangedEvent>() {
            public void call(ChangedEvent event) {
                if(selection != null) {
                    if(manager.propMan.isClassAvailable(SShape.class)) {
                        double v = (Double)event.getValue();
                        manager.propMan.getProperty("fillOpacity").setValue(v/100.0);
                        context.redraw();
                    }
                }
            }
        });
        fillOpacityLabel = new Label(getString("toolbar.opacity")+":");
        add(fillOpacityLabel);
        add(fillOpacitySlider);

        fontSizeSlider = new Slider(false);
        fontSizeSlider.setMin(8);
        fontSizeSlider.setMax(200);
        fontSizeSlider.setValue(24);
        EventBus.getSystem().addListener(fontSizeSlider, ChangedEvent.DoubleChanged, new Callback<ChangedEvent>() {
            public void call(ChangedEvent event) {
                if(selection != null) {
                    for(SNode node: selection.items()) {
                        if(node instanceof SText) {
                            SText text = (SText) node;
                            text.setFontSize(((Double)event.getValue()));
                            if(fontBoldButton.isSelected()) {
                                text.setWeight(Font.Weight.Bold);
                            } else {
                                text.setWeight(Font.Weight.Regular);
                            }
                        }
                    }
                    context.redraw();
                }
                
            }
        });
        fontSizeLabel = new Label(getString("toolbar.fontSize")+":");
        add(fontSizeLabel);
        add(fontSizeSlider);

        final Font[] fonts = new Font[]{Main.HANDDRAWN_FONT,Main.SERIF_FONT,Main.SANSSERIF_FONT};
        fontPicker = new PopupMenuButton<String>();
        fontPicker.setModel(new ListModel<String>() {
            public String get(int i) {
                return fonts[i].getName();
            }

            public int size() {
                return 3;
            }
        });
        EventBus.getSystem().addListener(fontPicker, SelectionEvent.Changed, new Callback<SelectionEvent>(){
            public void call(SelectionEvent event) {
                if(manager.propMan.isClassAvailable(SText.class)) {
                    int index = event.getView().getSelectedIndex();
                    manager.propMan.getProperty("fontName").setValue(fonts[index].getName());
                    context.redraw();
                }
            }
        });
        add(fontPicker);
        fontPicker.setVisible(false);

        strokeWidthSlider = new Slider(false);
        strokeWidthSlider.setMin(0);
        strokeWidthSlider.setMax(20);
        strokeWidthSlider.setValue(3);
        EventBus.getSystem().addListener(strokeWidthSlider, ChangedEvent.DoubleChanged, new Callback<ChangedEvent>() {
            public void call(ChangedEvent event) {
                if(manager.propMan.isClassAvailable(SShape.class) ||
                        manager.propMan.isClassAvailable(SImage.class)) {
                    int sw = (int)((Double)event.getValue()).doubleValue();
                    manager.propMan.getProperty("strokeWidth").setValue(sw);
                    context.redraw();
                }
            }
        });
        strokeWidthLabel = new Label(getString("toolbar.stroke")+":");
        add(strokeWidthLabel);
        add(strokeWidthSlider);

        fontBoldButton = new Togglebutton("B");
        add(fontBoldButton);
        fontBoldButton.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent event) {
                if(selection != null) {
                    for(SNode node: selection.items()) {
                        if(node instanceof SText) {
                            SText text = (SText) node;
                            if(fontBoldButton.isSelected()) {
                                text.setWeight(Font.Weight.Bold);
                            } else {
                                text.setWeight(Font.Weight.Regular);
                            }
                        }
                    }
                    context.redraw();
                }

            }
        });

        fontItalicButton = new Togglebutton("I");
        add(fontItalicButton);
        fontItalicButton.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent event) {
                if(selection != null) {
                    for(SNode node: selection.items()) {
                        if(node instanceof SText) {
                            SText text = (SText) node;
                            if(fontItalicButton.isSelected()) {
                                text.setStyle(Font.Style.Italic);
                            } else {
                                text.setStyle(Font.Style.Regular);
                            }
                        }
                    }
                    context.redraw();
                }

            }
        });

        rectPropsButton = new Togglebutton("^");
        add(rectPropsButton);
        rectPropsButton.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent event) {
                context.propsPalette.setVisible(!context.propsPalette.isVisible());
            }
        });


        arrowHeadEnd = new PopupMenuButton<SArrow.HeadEnd>();
        arrowHeadEnd.setModel(ListView.createModel(SArrow.HeadEnd.values()));
        add(arrowHeadEnd);
        arrowHeadEnd.setVisible(false);
        EventBus.getSystem().addListener(arrowHeadEnd, SelectionEvent.Changed, new Callback<SelectionEvent>() {
            public void call(SelectionEvent event) {
                if(manager.propMan.isClassAvailable(SArrow.class)) {
                    int index = event.getView().getSelectedIndex();
                    manager.propMan.getProperty("headEnd").setValue(SArrow.HeadEnd.values()[index]);
                }
            }
        });

        this.setBoxAlign(Align.Baseline);
        setVisible(false);
        //setPadding(new Insets(5,5,5,5));
    }

    private void updatePanelContents() {
        SNode lastNode = null;
        for(SNode node : selection.items()) {
            lastNode = node;
        }

        if(manager.propMan.isClassAvailable(SText.class)) {
            fontSizeSlider.setVisible(true);
            fontSizeLabel.setVisible(true);
            double dval = manager.propMan.getProperty("fontSize").getDoubleValue();
            if(selection.size() == 1) {
                fontSizeSlider.setValue(dval);
            }
            fontBoldButton.setVisible(true);
            //fontBoldButton.setSelected(firstText.getWeight() == Font.Weight.Bold);
            fontItalicButton.setVisible(true);
            //fontItalicButton.setSelected(firstText.getStyle() == Font.Style.Italic);
            fontPicker.setVisible(true);
        } else {
            fontSizeSlider.setVisible(false);
            fontSizeLabel.setVisible(false);
            fontBoldButton.setVisible(false);
            fontItalicButton.setVisible(false);
            fontPicker.setVisible(false);
        }
        if(manager.propMan.isClassAvailable(SArrow.class)) {
            arrowHeadEnd.setVisible(true);
        } else {
            arrowHeadEnd.setVisible(false);
        }

        if(manager.propMan.isClassAvailable(SShape.class)) {
            colorButton.setVisible(true);
            fillOpacitySlider.setVisible(true);
            fillOpacityLabel.setVisible(true);
            if(lastNode != null) {
                PropertyManager.Property fillColorProp = manager.propMan.getProperty("fillPaint");
                if(fillColorProp.hasSingleValue()) {
                    Object val = fillColorProp.getValue();
                    if(val instanceof FlatColor) {
                        colorButton.setSelectedColor((FlatColor)val);
                    } else {
                        colorButton.setSelectedColor(gradient1);
                    }
                }

                PropertyManager.Property fillOpacityProp = manager.propMan.getProperty("fillOpacity");
                if(fillOpacityProp.hasSingleValue()) {
                    fillOpacitySlider.setValue(fillOpacityProp.getDoubleValue()*100.0);
                }
            }
        } else {
            colorButton.setVisible(false);
            fillOpacitySlider.setVisible(false);
            fillOpacityLabel.setVisible(false);
            strokeColorButton.setVisible(false);
            strokeWidthSlider.setVisible(false);
            strokeWidthLabel.setVisible(false);
        }
        if(manager.propMan.isClassAvailable(SShape.class) ||
            manager.propMan.isClassAvailable(SImage.class)) {
            strokeColorButton.setVisible(true);
            strokeWidthSlider.setVisible(true);
            strokeWidthLabel.setVisible(true);
            if(lastNode != null) {
                PropertyManager.Property strokeProp = manager.propMan.getProperty("strokeWidth");
                if(strokeProp.hasSingleValue()) {
                    strokeWidthSlider.setValue(strokeProp.getDoubleValue());
                }
                PropertyManager.Property strokeColorProp = manager.propMan.getProperty("strokePaint");
                if(strokeColorProp.hasSingleValue()) {
                    strokeColorButton.setSelectedColor((FlatColor)strokeColorProp.getValue());
                }
            }
        }

        //show the rect props button only if a single rect is selected
        rectPropsButton.setVisible(manager.propMan.isClassAvailable(SRect.class));

        setLayoutDirty();
    }


    @Override
    protected void drawSelf(GFX g) {
        double h = 50;
        g.setPaint(new FlatColor(0.3,0.3,0.3,0.4));
        g.fillRoundRect(0,0,getWidth()-1,h-1,10,10);
        g.setPaint(new FlatColor(0,0,0,0.8));
        g.drawRoundRect(0,0,getWidth()-1,h-1,10,10);
    }
    @Override
    public void doLayout() {
        if(selected) {
            Bounds bounds = selection.calculateBounds();
            bounds = context.getSketchCanvas().transformToDrawing(bounds);
            Point2D pt = NodeUtils.convertToScene(context.getSketchCanvas(), bounds.getX(), bounds.getY());
            if(TRUE_PALLETTE) {
                Stage s = getParent().getStage();
                Stage cs = context.getStage();
                s.setWidth(400);
                s.setHeight(230);
                pt = new Point2D.Double(pt.getX()+cs.getX(),pt.getY()+cs.getY());
                s.setX(pt.getX());
                s.setY(pt.getY()+bounds.getHeight()+40);
            } else {
                setTranslateX(pt.getX());
                setTranslateY(pt.getY() + bounds.getHeight()+20);
            }
            super.doLayout();
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        setLayoutDirty();
    }

}
