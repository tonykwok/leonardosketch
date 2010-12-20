package org.joshy.sketch.modes.vector;

import org.joshy.gfx.event.ActionEvent;
import org.joshy.gfx.event.Callback;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.node.control.Button;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.layout.Panel;
import org.joshy.gfx.node.layout.TabPanel;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.*;
import org.joshy.sketch.actions.flickr.FlickrPanel;
import org.joshy.sketch.actions.flickr.ViewSidebar;
import org.joshy.sketch.actions.symbols.CreateSymbol;
import org.joshy.sketch.actions.symbols.SymbolManager;
import org.joshy.sketch.actions.symbols.SymbolPanel;
import org.joshy.sketch.canvas.Selection;
import org.joshy.sketch.canvas.SketchCanvas;
import org.joshy.sketch.controls.*;
import org.joshy.sketch.model.*;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.tools.*;
import org.joshy.sketch.util.BiList;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.joshy.gfx.util.localization.Localization.getString;

/**
 * A doc context for all vector based doc types, including drawing and presentation
 */
public class VectorDocContext extends DocContext<SketchCanvas, SketchDocument> {
    private DrawTextTool drawTextTool;
    private DrawPathTool drawPathTool;
    private VFlexBox toolbar;
    private TabPanel sideBar;
    private Panel flickrPanel;
    private SymbolPanel symbolPanel;
    private SketchCanvas canvas;
    protected ToggleGroup group;
    protected BiList<Button, CanvasTool> tools = new BiList<Button, CanvasTool>();
    protected EditResizableShapeTool editResizableShapeTool;

    public RectPropsPalette propsPalette;
    public CanvasTool selectedTool;
    public SelectMoveTool moveRectTool;
    public FloatingPropertiesPanel propPanel;
    public DeleteSelectedNodeAction deleteSelectedNodeAction;
    public NodeActions.DuplicateNodesAction duplicateNodeAction;
    private DrawNgonTool drawNGonTool;
    private List<SAction> viewActions = new ArrayList<SAction>();

    public VectorDocContext(Main main, VectorModeHelper helper) {
        super(main,helper);
        canvas =  new SketchCanvas(this);
    }

    public Selection getSelection() {
        return canvas.selection;
    }

    public SAction getDeleteSelectedNodeAction() {
        return deleteSelectedNodeAction;
    }

    public FloatingPropertiesPanel getPropPanel() {
        return propPanel;
    }

    public SAction getDuplicateNodeAction() {
        return duplicateNodeAction;
    }

    public SymbolManager getSymbolManager() {
        return getMain().symbolManager;
    }

    public CanvasTool getEditResizableShapeTool() {
        return this.editResizableShapeTool;
    }

    public void switchToTextEdit(SNode text) {
        drawTextTool.startEditing(text);
        selectButtonForTool(drawTextTool);
    }

    public void switchToPathEdit(SPath path) {
        selectButtonForTool(drawPathTool);
        drawPathTool.startEditing(path);
    }

    public void switchToNGonEdit(NGon ngon) {
        selectButtonForTool(drawNGonTool);
        drawNGonTool.startEditing(ngon);
    }

    public void setSelectedTool(CanvasTool tool) {
        selectedTool.disable();
        selectedTool = tool;
        selectedTool.enable();
    }

    public void selectButtonForTool(CanvasTool tool) {
        group.setSelectedButton(tools.getKey(tool));
    }

    public void releaseControl() {
        selectButtonForTool(moveRectTool);
    }


    @Override
    public SketchDocument getDocument() {
        return canvas.getDocument();
    }

    @Override
    public void redraw() {
        canvas.redraw();
    }

    @Override
    public SketchCanvas getCanvas() {
        return canvas;
    }

    public SketchCanvas getSketchCanvas() {
        return canvas;
    }

    public void setupTools() throws Exception {
        toolbar = new VFlexBox();
        moveRectTool = new SelectMoveTool(this);
        drawTextTool = new DrawTextTool(this);
        drawPathTool = new DrawPathTool(this);
        drawNGonTool = new DrawNgonTool(this);
        editResizableShapeTool = new EditResizableShapeTool(this);
        tools.add(new ToolbarButton(Main.getIcon("cr22-action-14_select.png")),moveRectTool);
        tools.add(new ToolbarButton(Main.getIcon("cr22-action-14_text.png")),drawTextTool);
        tools.add(new ToolbarButton(Main.getIcon("cr22-action-14_insertknots.png")),drawPathTool);
        tools.add(new ToolbarButton(Main.getIcon("cr22-action-14_polygon.png")),drawNGonTool);


        modeHelper.setupToolbar(tools, getMain(),this);

        group = new ToggleGroup();
        for(Button button : tools.keys()) {
            group.add(button);
            toolbar.add(button);
        }
        EventBus.getSystem().addListener(ActionEvent.Action, new Callback<ActionEvent>(){
            public void call(ActionEvent event) {
                if(event.getSource() == group) {
                    Button btn = group.getSelectedButton();
                    CanvasTool tool = tools.getValue(btn);
                    setSelectedTool(tool);
                }
            }
        });
        selectedTool = moveRectTool;
        moveRectTool.enable();
        selectButtonForTool(moveRectTool);

    }

    public void setupSidebar() {
        this.sideBar = new TabPanel();
        this.symbolPanel = new SymbolPanel(getMain().symbolManager, this);
        this.sideBar.add(getString("sidebar.symbols"),this.symbolPanel);
        this.flickrPanel = new FlickrPanel(this);
        this.sideBar.add(getString("sidebar.flickr"), this.flickrPanel);
        this.flickrPanel.setVisible(false);
        this.viewActions.add(new ViewSidebar("menus.viewSymbolSidebar",this,symbolPanel));
        this.viewActions.add(new ViewSidebar("menus.viewFlickrSidebar",this,flickrPanel));
    }

    @Override
    public void setupActions() {
        duplicateNodeAction = new NodeActions.DuplicateNodesAction(this,false);
        deleteSelectedNodeAction = new DeleteSelectedNodeAction(this);
    }

    @Override
    public void setupPalettes() throws IOException {
        propPanel = new FloatingPropertiesPanel(getMain(),this);
        propsPalette = new RectPropsPalette(getMain(),canvas);
        propsPalette.setVisible(false);
        propsPalette.setTranslateX(400);
        propsPalette.setTranslateY(50);
    }

    @Override
    public void setupPopupLayer() {
        if(FloatingPropertiesPanel.TRUE_PALLETTE) {
            Stage s = Stage.createStage();
            s.setContent(propPanel);
            s.centerOnScreen();
            s.setUndecorated(false);
            s.setOpacity(0);
            s.setAlwaysOnTop(true);
        } else {
            getStage().getPopupLayer().add(propPanel);
        }
        getStage().getPopupLayer().add(propsPalette);
    }

    @Override
    public void createAfterEditMenu(Menubar menubar) {
        //node menu
        VectorDocContext context = this;
        menubar.add(new Menu().setTitle(getString("menus.node"))
                .addItem(getString("menus.raiseNodeTop"),      "shift CLOSE_BRACKET", new NodeActions.RaiseTopSelectedNodeAction(this))
                .addItem(getString("menus.raiseNode"),        "CLOSE_BRACKET",       new NodeActions.RaiseSelectedNodeAction(this))
                .addItem(getString("menus.lowerNode"),        "OPEN_BRACKET",        new NodeActions.LowerSelectedNodeAction(this))
                .addItem(getString("menus.lowerNodeBottom"),   "shift OPEN_BRACKET",  new NodeActions.LowerBottomSelectedNodeAction(this))
                .separator()
                .addItem(getString("menus.alignNodeTop"),               new NodeActions.AlignTop(context))
                .addItem(getString("menus.alignNodeBottom"),            new NodeActions.AlignBottom(context))
                .addItem(getString("menus.alignNodeLeft"),              new NodeActions.AlignLeft(context))
                .addItem(getString("menus.alignNodeRight"),             new NodeActions.AlignRight(context))
                .addItem(getString("menus.alignNodeCenterHorizontal"), new NodeActions.AlignCenterH(context))
                .addItem(getString("menus.alignNodeCenterVertical"),   new NodeActions.AlignCenterV(context))
                .separator()
                .addItem(getString("menus.matchNodeWidth"),              new NodeActions.SameWidth(context,true))
                .addItem(getString("menus.matchNodeHeight"),             new NodeActions.SameWidth(context,false))
                .separator()
                .addItem(getString("menus.groupSelection"),   "G",       new NodeActions.GroupSelection(context))
                .addItem(getString("menus.ungroupSelection"), "shift G", new NodeActions.UngroupSelection(context))
                .separator()
                .addItem(getString("menus.createSymbol"), new CreateSymbol(context))
                .addItem(getString("menus.createResizableShape"), new CreateResizableShape(context))
                .addItem(getString("menus.editResizableShape"), new CreateResizableShape.Edit(context))
                .addItem(getString("menus.duplicateNode"), new NodeActions.DuplicateNodesAction(context,true))
                .separator()
                .addItem(getString("menus.resetTransforms"), new NodeActions.ResetTransforms(context))
                );
        menubar.add(new Menu().setTitle(getString("menus.path"))
                .addItem(getString("menus.flipNodeHorizontal"), new PathActions.Flip(context,true))
                .addItem(getString("menus.flipNodeVertical"),   new PathActions.Flip(context,false))
                .addItem(getString("menus.rotateNode90Right"), new PathActions.RotateClockwise(context,Math.PI/2))
                .addItem(getString("menus.rotateNode90Left"), new PathActions.RotateClockwise(context,-Math.PI/2))
                //.addItem(getString("menus.rotateNodeFree"), new PathActions.Rotate(context))
                //.addItem(getString("menus.scaleNode"), new PathActions.Scale(context))
                .separator()
                .addItem(getString("menus.add"), new BooleanGeometry.Union(context))
                .addItem(getString("menus.subtract"), new BooleanGeometry.Subtract(context))
                .addItem(getString("menus.intersection"), new BooleanGeometry.Intersection(context))
                );
        menubar.add(new Menu().setTitle(getString("menus.document"))
                .addItem(getString("menus.setDocumentSize"), new DocumentActions.SetDocumentSize(this))
                );

        rebuildSymbolMenu(menubar);

    }

    @Override
    public Control getToolbar() {
        return toolbar;
    }

    @Override
    public TabPanel getSidebar() {
        return this.sideBar;
    }

    @Override
    public void setDocument(SketchDocument doc) {
        this.canvas.setDocument(doc);
        final JFrame frame = (JFrame) getStage().getNativeWindow();
        EventBus.getSystem().addListener(getDocument(), CanvasDocument.DocumentEvent.Dirty, new Callback<CanvasDocument.DocumentEvent>() {
            public void call(CanvasDocument.DocumentEvent event) {
                frame.getRootPane().putClientProperty("Window.documentModified", event.getDocument().isDirty());
            }
        });
        frame.getRootPane().putClientProperty("Window.documentModified", getDocument().isDirty());
    }


    private void rebuildSymbolMenu(final Menubar menubar) {
        VectorDocContext context = this;
        if(context.symbolMenu != null) {
            menubar.remove(context.symbolMenu);
        }
        context.symbolMenu = new Menu().setTitle(getString("menus.symbolSets"));
        context.symbolMenu.addItem(getString("menus.create.new.set"), new SAction(){
            @Override
            public void execute() {
                String value = StandardDialog.showEditText(getString("symbolSetDialog.newSetName").toString(),"untitled");
                if(value != null) {
                    SymbolManager.SymbolSet set = getMain().symbolManager.createNewSet(value);
                    getMain().symbolManager.setCurrentSet(set);
                    redraw();
                    rebuildSymbolMenu(menubar);
                }
            }
        });
        context.symbolMenu.separator();
        for(final SymbolManager.SymbolSet set : getMain().symbolManager.sets.values()) {
            context.symbolMenu.addItem(set.file.getName(), new SAction(){
                @Override
                public void execute() {
                    getMain().symbolManager.setCurrentSet(set);
                    redraw();
                }
            });
        }
        menubar.add(context.symbolMenu);
    }


    public List<SAction> getSidebarPanelViewActions() {
        return viewActions;
    }
}
