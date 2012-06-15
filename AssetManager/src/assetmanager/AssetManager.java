/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package assetmanager;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joshy.gfx.Core;
import org.joshy.gfx.SkinManager;
import org.joshy.gfx.css.CSSMatcher;
import org.joshy.gfx.css.CSSSkin;
import org.joshy.gfx.draw.FlatColor;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.draw.Image;
import org.joshy.gfx.event.*;
import org.joshy.gfx.node.Bounds;
import org.joshy.gfx.node.control.*;
import org.joshy.gfx.node.layout.FlexBox;
import org.joshy.gfx.node.layout.HFlexBox;
import org.joshy.gfx.node.layout.VFlexBox;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.ArrayListModel;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.swatches.Palette;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author josh
 */
public class AssetManager {
    private static ListView<Query> sidebarList;
    private static TableView<Asset, String> tableView;
    private static LibraryQuery all;
    private static AssetDB db;
    private static CSSSkin tableSkin;
    private static Font tableFont;
    private static Button tableViewButton;
    private static Textbox searchBox;
    private static Button addButton;
    private static Button deleteButton;

    public static void main(String[] args) throws Exception {
        Core.init();
        Core.getShared().defer(new Runnable() {
            public void run() {
                Stage stage = Stage.createStage();
                stage.centerOnScreen();

                stage.setContent(setupMain());
                stage.setWidth(800);
                stage.setHeight(600);

                EventBus.getSystem().addListener(SystemMenuEvent.All, new Callback<SystemMenuEvent>() {
                    public void call(SystemMenuEvent systemMenuEvent) throws Exception {
                        if (systemMenuEvent.getType() == SystemMenuEvent.Quit) {
                            System.exit(0);
                        }
                    }
                });
            }
        });
    }

    public static Control setupMain() {
        db = AssetDB.getInstance();

        VFlexBox vbox = new VFlexBox();
        vbox.setBoxAlign(FlexBox.Align.Stretch);

        HFlexBox toolbar = new HFlexBox();

        addButton = new Button("Add");
        addButton.onClicked(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                FileDialog fd = new FileDialog((Frame)null);
                fd.setMode(FileDialog.LOAD);
                fd.setTitle("Add File or Director");
                fd.setVisible(true);
                if(fd.getFile() != null) {
                    File file = new File(fd.getDirectory(),fd.getFile());
                    addFile(file);
                    tableView.redraw();
                }
            }
        });
        toolbar.add(addButton);
        toolbar.add(new Button("New List").setEnabled(false));
        
        deleteButton = new Button("Delete");
        deleteButton.onClicked(new DeleteAction());
        toolbar.add(deleteButton);

        //tableViewButton = new Button("Table View");
        //toolbar.add(tableViewButton);
        //toolbar.add(new Button("Thumb View"));

        searchBox = new Textbox("");
        searchBox.setHintText("Search");
        searchBox.onAction(new Callback<ActionEvent>() {
            public void call(ActionEvent actionEvent) throws Exception {
                String query = searchBox.getText();
                tableView.setModel(new AssetTableModel(db.searchByAnyText(query)));
                tableView.redraw();
            }
        });
        toolbar.add(searchBox.setWidth(100),1.0);
        vbox.add(toolbar);


        HFlexBox main = new HFlexBox();
        main.setBoxAlign(FlexBox.Align.Stretch);
        Query library = new Query("LIBRARY","----");
        library.setSelectable(false);
        all = new LibraryQuery("Everything","*",0,1);
        Query fonts = new Query("Fonts",AssetDB.FONT,6,2);
        Query symbols = new Query("Symbols",AssetDB.SYMBOLSET,4,0);
        Query textures = new Query("Textures",AssetDB.PATTERN,10,0);
        Query gradients = new Query("Gradients","gradient",19,1);
        Query images = new Query("Images","image",19,2);
        Query palettes = new Query("Palettes",AssetDB.PALETTE,4,5);
        final Query staticList = new Query("LISTS","----");
        staticList.setSelectable(false);

        sidebarList = new ListView<Query>();
        ArrayListModel<Query> sidebarModel = new ArrayListModel<Query>();
        sidebarModel.add(library);
        sidebarModel.add(all);
        sidebarModel.add(staticList);
        sidebarModel.add(fonts);
        sidebarModel.add(symbols);
        sidebarModel.add(textures);
        sidebarModel.add(gradients);
        sidebarModel.add(images);
        sidebarModel.add(palettes);
        sidebarList.setModel(sidebarModel);

        sidebarList.setTextRenderer(new ListView.TextRenderer<Query>() {
            public String toString(SelectableControl selectableControl, Query query, int i) {
                return query.getName();
            }
        });

        EventBus.getSystem().addListener(sidebarList, SelectionEvent.Changed, new Callback<SelectionEvent>() {
            public void call(SelectionEvent selectionEvent) throws Exception {
                Query query = sidebarList.getModel().get(sidebarList.getSelectedIndex());
                if(query == null || !query.isSelectable()) return;
                tableView.setModel(new AssetTableModel(query.execute(db)));
                tableView.redraw();
            }
        });

        tableView = new TableView<Asset,String>();
        /*
        tableView.setSorter(new TableView.Sorter() {
            public Comparator createComparator(TableView.TableModel tableModel, int col, TableView.SortOrder sortOrder) {
                return Collator.getInstance();
            }
        });
        */
        tableView.setModel(new AssetTableModel(new ArrayList<Asset>()));
        tableSkin = SkinManager.getShared().getCSSSkin();
        tableFont = tableSkin.getStyleInfo(tableView,null).font;
        tableView.setDefaultColumnWidth(200);
        tableView.setRenderer(new TableView.DataRenderer<assetmanager.Asset>() {
            public void draw(GFX gfx, TableView tableView, Asset asset, int row, int column, double x, double y, double w, double h) {

                if(asset == null) return;

                CSSMatcher matcher = new CSSMatcher(tableView);
                Bounds bounds = new Bounds(x,y,w,h);
                matcher.pseudoElement = "item";
                if(tableView.getSelectedIndex() == row) {
                    matcher.pseudoElement = "selected-item";
                }

                tableSkin.drawBackground(gfx, matcher, bounds);
                tableSkin.drawBorder(gfx, matcher, bounds);

                x = x + 3;
                int col = tableSkin.getCSSSet().findColorValue(matcher, "color");
                gfx.setPaint(new FlatColor(col));
                if(column == 0) {
                    Font.drawCenteredVertically(gfx, asset.getName(), tableFont, x, y, w, h, true);
                }
                if(column == 1) {
                    Font.drawCenteredVertically(gfx, asset.getKind(), tableFont, x, y, w, h, true);
                }
                if(column == 2) {
                    Image img = null;
                    if(asset.getKind().equals(AssetDB.PATTERN)) {
                        img = RenderUtil.patternToImage(asset);
                    }
                    if(asset.getKind().equals(AssetDB.PALETTE)) {
                        img = RenderUtil.toImage((Palette)asset);
                    }
                    if(asset.getKind().equals(AssetDB.FONT)) {
                        img = RenderUtil.fontToImage(asset);
                    }
                    if(img != null) {
                        Bounds oldClip = gfx.getClipRect();
                        gfx.setClipRect(new Bounds(x,y,w,h));
                        gfx.drawImage(img,x,y);
                        gfx.setClipRect(oldClip);
                    }
                }
            }
        });

        ScrollPane sidebarScroll = new ScrollPane(sidebarList);
        sidebarScroll.setPrefWidth(200);
        sidebarScroll.setHorizontalVisiblePolicy(ScrollPane.VisiblePolicy.Never);
        sidebarScroll.setVerticalVisiblePolicy(ScrollPane.VisiblePolicy.WhenNeeded);
        main.add(sidebarScroll);
        
        ScrollPane tableScroll = new ScrollPane(tableView);
        tableScroll.setPrefWidth(500);
        tableScroll.setVerticalVisiblePolicy(ScrollPane.VisiblePolicy.WhenNeeded);
        tableScroll.setHorizontalVisiblePolicy(ScrollPane.VisiblePolicy.WhenNeeded);
        main.add(tableScroll,1.0);
        vbox.add(main,1.0);
        return vbox;
    }


    private static void addFile(File file) throws IOException {
        if(file.exists()) {
            if(file.isDirectory()) {
                addFiles(file.listFiles());
            } else {
                processfile(file);
            }
        }
    }

    private static void addFiles(File[] files) throws IOException {
        for(File file : files) {
            addFile(file);
        }
    }

    private static void processfile(File file) throws IOException {
        //assume png is a texture
        if(file.getName().toLowerCase().endsWith(".png")) {
            db.copyAndAddPattern(file);
        }
        if(file.getName().toLowerCase().endsWith(".ttf")) {
            db.copyAndAddFont(file);
        }
    }

    private static class AssetTableModel implements TableView.TableModel<Asset,String> {

        private List<Asset> data;

        public AssetTableModel(List<Asset> data) {
            this.data = data;
        }

        public int getRowCount() {
            return data.size();
        }

        public int getColumnCount() {
            return 3;
        }

        public String getColumnHeader(int col) {
            switch(col) {
                case 0: return "Name";
                case 1: return "Kind";
                case 2: return "Preview";
                default: return "col?";
            }
        }

        public Asset get(int row, int col) {
            if(row >= data.size()) return null;
            return data.get(row);
        }
    }

    private static class DeleteAction implements Callback<ActionEvent> {
        public void call(ActionEvent actionEvent) throws Exception {
            Query currentQuery = sidebarList.getModel().get(sidebarList.getSelectedIndex());
            if(currentQuery == all) {
                int row = tableView.getSelectedRow();
                Asset asset = tableView.getModel().get(row,0);
                db.removeFromLibrary(asset);
                tableView.redraw();
                return;
            }
            if(!(currentQuery instanceof StaticQuery)) return;
            StaticQuery staticQuery = (StaticQuery) currentQuery;
            
            int row = tableView.getSelectedRow();
            Asset asset = tableView.getModel().get(row,0);
            db.removeFromStaticList(staticQuery,asset);
        }
    }
}
