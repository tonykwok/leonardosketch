package org.joshy.gfx.css;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.parboiled.Node;
import org.parboiled.Parboiled;
import org.parboiled.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jul 28, 2010
 * Time: 9:37:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainTest {
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void doit() throws IOException {
        InputStream css = MainTest.class.getResourceAsStream("test1.css");
        ParsingResult<?> result = parseCSS(css);
        CSSRuleSet set = new CSSRuleSet();
        condense(result.parseTreeRoot,set);

        //basic matching
        assertTrue(set.findStringValue("button","color").equals("ff00aa"));
        //fallback to *
        assertTrue(set.findStringValue("label","color").equals("ff00aa"));
        //parsing a pixel value
        assertTrue(set.findIntegerValue("button","border")==3);

        //get the url
        CSSMatcher matcher = new CSSMatcher("button");
        assertTrue(set.findURIValue(matcher,"icon").toString().endsWith("png"));
        //get the icon position:
        assertTrue("left".equals(set.findStringValue("button","icon-position")));

        //background-color: transparent
        assertTrue("transparent".equals(set.findStringValue("transptest","background-color")));
        assertTrue(!"transparent".equals(set.findStringValue("transptest","color")));
        //background-color: red turns into #ff0000
        
    }


    
    private static void condense(Node<?> node, CSSRuleSet set) {
        if("CSSRule".equals(node.getLabel())) {
            CSSRule rule = (CSSRule) node.getValue();
            set.rules.add(rule);
        }
        for(Node<?> n : node.getChildren()) {
            condense(n,set);
        }
    }

    private static ParsingResult<?> parseCSS(InputStream css) throws IOException {
        String cssString = toString(css);
        CSSParser parser = Parboiled.createParser(CSSParser.class);
        //System.out.println("string = " + cssString);
        ParsingResult<?> result = ReportingParseRunner.run(parser.RuleSet(), cssString);
        //String parseTreePrintOut = ParseTreeUtils.printNodeTree(result);
        //System.out.println(parseTreePrintOut);
        //u.p("other value = " + result.parseTreeRoot.getLabel());
        return result;
    }

    private static String toString(InputStream css) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buff = new byte[256];
        while(true) {
            int n = css.read(buff);
            if(n < 0) break;
            out.write(buff,0,n);
        }
        css.close();
        out.close();
        return new String(out.toByteArray());
    }

    @After
    public void tearDown() throws Exception {
    }

}
