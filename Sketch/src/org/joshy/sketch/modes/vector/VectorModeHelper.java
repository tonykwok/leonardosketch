package org.joshy.sketch.modes.vector;

import org.joshy.gfx.node.control.Button;
import org.joshy.sketch.Main;
import org.joshy.sketch.actions.NewAction;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.controls.Menu;
import org.joshy.sketch.controls.ToolbarButton;
import org.joshy.sketch.model.CanvasDocument;
import org.joshy.sketch.model.SketchDocument;
import org.joshy.sketch.modes.DocContext;
import org.joshy.sketch.modes.DocModeHelper;
import org.joshy.sketch.tools.*;
import org.joshy.sketch.util.BiList;

import static org.joshy.gfx.util.localization.Localization.getString;

/**
 * Mode helper for vector docs
 */
public class VectorModeHelper extends DocModeHelper<VectorDocContext> {
    public VectorModeHelper(Main main) {
        super(main);
    }

    @Override
    public void setupToolbar(BiList<Button, CanvasTool> tools, Main main, VectorDocContext context) throws Exception {
        tools.add(new ToolbarButton(main.getClass().getResource("resources/cr22-action-14_rectangle.png")),new DrawRectTool(context));
        tools.add(new ToolbarButton(main.getClass().getResource("resources/cr22-action-14_ellipse.png")),new DrawOvalTool(context));
        tools.add(new ToolbarButton(main.getClass().getResource("resources/cr22-action-14_polyline.png")),new DrawPolyTool(context));
        //tools.add(new ToolbarButton(main.getClass().getResource("resources/cr22-action-14_shear.png")),new DrawArrowTool(context));
        //tools.add(new ToolbarButton(main.getClass().getResource("resources/cr22-action-move.png")),new PanCanvasTool(context));
        tools.add(new ToolbarButton(main.getClass().getResource("resources/cr22-action-14_image.png")),new AddImageTool(context));
        //tools.add(new ToolbarButton(main.getClass().getResource("resources/cr22-action-move.png")), new TransformTool(context));
    }

    @Override
    public boolean isPageListVisible() {
        return true;
    }

    @Override
    public Menu buildPageMenu(VectorDocContext context) {
        return null;
    }

    @Override
    public CharSequence getModeName() {
        return getString("misc.vector");
    }

    @Override
    public VectorDocContext createDocContext(Main main) {
        return new VectorDocContext(main, this);
    }

    @Override
    public CanvasDocument createNewDoc() {
        return new SketchDocument();
    }

    @Override
    public SAction getNewDocAction(Main main) {
        return new NewAction(main);
    }

    @Override
    public void addCustomExportMenus(Menu exportMenu, DocContext context) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


}
