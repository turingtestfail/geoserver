package org.geoserver.mapml;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.data.test.SystemTestData;
import org.geotools.api.style.Style;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MapMLStyleVisitorTest extends MapMLTestSupport {
    @Override
    protected void onSetUp(SystemTestData testData) throws Exception {
        super.onSetUp(testData);
        Catalog catalog = getCatalog();
        testData.addStyle("pointSymbolizer", "pointSymbolizer.sld", getClass(), catalog);
    }

    @Test
    public void testPointSymbolizer() throws Exception {
        Catalog catalog = getCatalog();
        StyleInfo styleInfo = catalog.getStyleByName("pointSymbolizer");
        Style style = styleInfo.getStyle();
        MapMLStyleVisitor visitor = new MapMLStyleVisitor();
        style.accept(visitor);
        Map<String, MapMLStyle> styleMap = visitor.getStyles();
        MapMLStyle mapMLStyle = styleMap.get("rule-1:symbolizer-1");
        assertEquals("0.5", mapMLStyle.getProperty("opacity"));
        assertEquals("#FF0000", mapMLStyle.getProperty("fill"));
    }
}
