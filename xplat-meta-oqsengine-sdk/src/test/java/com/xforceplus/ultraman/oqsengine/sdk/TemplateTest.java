package com.xforceplus.ultraman.oqsengine.sdk;

import org.junit.Test;
import org.stringtemplate.v4.*;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class TemplateTest {

    @Test
    public void testTemplate(){

        String template = "<a>$name$</a>";
        ST st = new ST(template,'$','$');
        st.add("name", "World");
        assertEquals("<a>World</a>", st.render());
    }

}
