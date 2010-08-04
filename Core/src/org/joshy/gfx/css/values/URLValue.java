package org.joshy.gfx.css.values;

import org.joshy.gfx.util.u;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Aug 2, 2010
 * Time: 10:27:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class URLValue extends BaseValue {
    private URI value;

    public URLValue(String value) throws MalformedURLException, URISyntaxException {
        u.p("creating a url with: " + value);
        String url = value.substring(4,value.length()-1);
        u.p("final = " + url);
        this.value = new URI(url);
        u.p("uri = " + this.value);
    }

    @Override
    public String asString() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public URI getValue() {
        return value;
    }
}
