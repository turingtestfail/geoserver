package org.geoserver.mapml;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.data.test.SystemTestData;
import org.geoserver.logging.TestAppender;
import org.geotools.api.style.Style;
import org.geotools.util.logging.Logging;
import org.junit.Test;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

public class MapMLStyleVisitorTest extends MapMLTestSupport {
    @Override
    protected void onSetUp(SystemTestData testData) throws Exception {
        super.onSetUp(testData);
        Catalog catalog = getCatalog();
        testData.addStyle("pointSymbolizer", "pointSymbolizer.sld", getClass(), catalog);
        testData.addStyle("lineSymbolizer", "lineSymbolizer.sld", getClass(), catalog);
        testData.addStyle("polygonSymbolizer", "polygonSymbolizer.sld", getClass(), catalog);
    }

    @Test
    public void testPointSymbolizer() throws Exception {
        Logger logger = Logging.getLogger(MapMLStyleVisitor.class);
        Level previousLevel = logger.getLevel();
        try (TestAppender appender = TestAppender.createAppender("pointAppender", null)) {
            appender.startRecording("org.geoserver.mapml");
            logger.setLevel(Level.FINE);
            Catalog catalog = getCatalog();
            StyleInfo styleInfo = catalog.getStyleByName("pointSymbolizer");
            Style style = styleInfo.getStyle();
            MapMLStyleVisitor visitor = new MapMLStyleVisitor();
            style.accept(visitor);
            Map<String, MapMLStyle> styleMap = visitor.getStyles();
            MapMLStyle mapMLStyle = styleMap.get("rule-1:symbolizer-1");
            assertEquals("0.5", mapMLStyle.getProperty("opacity"));
            assertEquals("#FF0000", mapMLStyle.getProperty("fill"));
            assertEquals("circle", mapMLStyle.getProperty("well-known-name"));
            appender.assertTrue(
                    "Graphic Rotation Warning",
                    "MapML feature styling does not currently support Graphic Rotation");
            appender.assertTrue(
                    "Displacement Warning",
                    "MapML feature styling does not currently support Graphic Displacement");
            appender.assertTrue(
                    "Anchor Point Warning",
                    "MapML feature styling does not currently support Graphic Anchor Point");
            appender.assertTrue(
                    "Graphic Gap Warning",
                    "MapML feature styling does not currently support Graphic Gap");
            appender.stopRecording("org.geoserver.mapml.MapMLStyleVisitor");
        } finally {
            logger.setLevel(previousLevel);
        }
    }

    @Test
    public void testLineSymbolizer() throws Exception {
        Logger logger = Logging.getLogger(MapMLStyleVisitor.class);
        Level previousLevel = logger.getLevel();
        try (TestAppender appender = TestAppender.createAppender("lineAppender", null)) {
            appender.startRecording("org.geoserver.mapml");
            logger.setLevel(Level.FINE);
            Catalog catalog = getCatalog();
            StyleInfo styleInfo = catalog.getStyleByName("lineSymbolizer");
            Style style = styleInfo.getStyle();
            MapMLStyleVisitor visitor = new MapMLStyleVisitor();
            style.accept(visitor);
            Map<String, MapMLStyle> styleMap = visitor.getStyles();
            MapMLStyle mapMLStyle = styleMap.get("rule-1:symbolizer-1");
            assertEquals("0.5", mapMLStyle.getProperty("stroke-opacity"));
            assertEquals("#333333", mapMLStyle.getProperty("stroke"));
            assertEquals("3.0", mapMLStyle.getProperty("stroke-width"));
            assertEquals("round", mapMLStyle.getProperty("stroke-linecap"));
            assertEquals("5.0 2.0", mapMLStyle.getProperty("stroke-dasharray"));
            appender.assertTrue(
                    "Graphic Stroke Warning",
                    "MapML feature styling does not currently support Graphic Strokes");
            appender.stopRecording("org.geoserver.mapml.MapMLStyleVisitor");
        } finally {
            logger.setLevel(previousLevel);
        }
    }

    @Test
    public void testPolygonSymbolizer() throws Exception {
        Logger logger = Logging.getLogger(MapMLStyleVisitor.class);
        Level previousLevel = logger.getLevel();
        try (TestAppender appender = TestAppender.createAppender("polygonAppender", null)) {
            appender.startRecording("org.geoserver.mapml");
            logger.setLevel(Level.FINE);
            Catalog catalog = getCatalog();
            StyleInfo styleInfo = catalog.getStyleByName("polygonSymbolizer");
            Style style = styleInfo.getStyle();
            MapMLStyleVisitor visitor = new MapMLStyleVisitor();
            style.accept(visitor);
            Map<String, MapMLStyle> styleMap = visitor.getStyles();
            MapMLStyle mapMLStyle = styleMap.get("rule-1:symbolizer-1");
            assertEquals("#000080", mapMLStyle.getProperty("fill"));
            assertEquals("2.0", mapMLStyle.getProperty("stroke-width"));
            assertEquals("0.5", mapMLStyle.getProperty("fill-opacity"));
            MapMLStyle mapMLStyle2 = styleMap.get("rule-2:symbolizer-1");
            appender.assertTrue(
                    "Graphic Fill Warning",
                    "MapML feature styling does not currently support Graphic Fills");
            appender.stopRecording("org.geoserver.mapml.MapMLStyleVisitor");
        } finally {
            logger.setLevel(previousLevel);
        }
    }
}
