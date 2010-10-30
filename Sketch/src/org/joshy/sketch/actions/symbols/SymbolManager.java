package org.joshy.sketch.actions.symbols;

import com.joshondesign.xml.XMLWriter;
import org.joshy.gfx.event.EventBus;
import org.joshy.gfx.node.control.ListModel;
import org.joshy.gfx.node.control.ListView;
import org.joshy.gfx.node.layout.TabPanel;
import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.ExportProcessor;
import org.joshy.sketch.actions.OpenAction;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.actions.io.NativeExport;
import org.joshy.sketch.model.SNode;
import org.joshy.sketch.modes.DocContext;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A manager for reusable symbols. 
 */
public class SymbolManager {
    private ListModel<SNode> model;
    
    public Map<File,SymbolSet> sets = new HashMap<File,SymbolSet>();
    private SymbolSet currentSet;
    private File basedir;
    private List<SymbolSet> list = new ArrayList<SymbolSet>();

    public SymbolManager(File file) {
        basedir = file;
        try {
            loadSymbols(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model = new ListModel<SNode>() {
            public SNode get(int i) {
                if(i < currentSet.symbols.size()) {
                    return currentSet.symbols.get(i);
                } else {
                    return null;
                }
            }
            public int size() {
                if(currentSet == null) return 0;
                if(currentSet.symbols == null) return 0;
                return currentSet.symbols.size();
            }
        };

    }

    public void setCurrentSet(SymbolSet set) {
        currentSet = set;
    }

    public SymbolSet createNewSet(String name) {
        SymbolSet set = new SymbolSet();
        set.file = new File(basedir,name+".xml");
        sets.put(set.file,set);
        return set;
    }

    public SymbolSet getSet(int i) {
        return list.get(i);
    }

    public static class SymbolSet {
        private List<SNode> symbols = new ArrayList<SNode>();
        public File file;
        public String toString() {
            return file.getName();
        }
    }

    private void loadSymbols(File basedir) throws Exception {
        u.p("Loading symbols from base dir " + basedir);
        if(!basedir.exists()) {
            boolean success = basedir.mkdirs();
            if(!success) {
                if(sets.isEmpty()) {
                    SymbolSet set = new SymbolSet();
                    set.file = new File(basedir,"default.xml");
                    sets.put(set.file,set);
                    list.add(set);
                    currentSet = set;
                }
                throw new Exception("Error making the directory: " + basedir);
            }
        }
        for(File file : basedir.listFiles()) {
            if(file.getName().endsWith(".xml") && file.exists()) {
                u.p("Loading: " + file.getAbsolutePath());
                List<SNode> shapes = OpenAction.loadShapes(file);
                SymbolSet set = new SymbolSet();
                set.file = file;
                set.symbols = shapes;
                currentSet = set;
                sets.put(file,set);
                list.add(set);
                u.p("successfully loaded: " + file.getName() + " symbols count " +set.symbols.size());
            }
        }
        if(sets.isEmpty()) {
            SymbolSet set = new SymbolSet();
            set.file = new File(basedir,"default.xml");
            sets.put(set.file,set);
            list.add(set);
            currentSet = set;
        }
    }

    public void add(SNode dupe) {
        currentSet.symbols.add(dupe);
        EventBus.getSystem().publish(new ListView.ListEvent(ListView.ListEvent.Updated, model));
        try {
            saveSymbols(currentSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            saveSymbols(currentSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ListModel<SNode> getModel() {
        return model;
    }
    
    public void remove(SNode shape) {
        currentSet.symbols.remove(shape);
        save();
    }

    private void saveSymbols(SymbolSet set) throws FileNotFoundException, UnsupportedEncodingException {
        u.p("saving to file: "+ set.file.getAbsolutePath());
        XMLWriter out = new XMLWriter(new PrintWriter(new OutputStreamWriter(new FileOutputStream(set.file), "UTF-8")),
                set.file.toURI());
        out.header();
        out.start("sketchy","version","-1");
        ExportProcessor.processFragment(new NativeExport(), out, set.symbols);
        out.end();
        out.close();
    }

    public static class ShowSymbolPanel extends SAction {
        private DocContext context;

        public ShowSymbolPanel(DocContext context) {
            super();
            this.context = context;
        }

        @Override
        public void execute() {
            TabPanel sideBar = context.getSidebar();
            if(sideBar!=null) {
                sideBar.setSelected(context.symbolPanel);
            }
        }
    }
}
