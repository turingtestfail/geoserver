package org.geoserver.mapml;

import org.apache.commons.lang3.StringUtils;
import org.geotools.api.filter.expression.Expression;
import org.geotools.api.style.FeatureTypeStyle;
import org.geotools.api.style.Fill;
import org.geotools.api.style.Graphic;
import org.geotools.api.style.GraphicalSymbol;
import org.geotools.api.style.LineSymbolizer;
import org.geotools.api.style.PointSymbolizer;
import org.geotools.api.style.PolygonSymbolizer;
import org.geotools.api.style.Rule;
import org.geotools.api.style.Stroke;
import org.geotools.api.style.Symbol;
import org.geotools.api.style.Symbolizer;
import org.geotools.filter.visitor.IsStaticExpressionVisitor;
import org.geotools.styling.AbstractStyleVisitor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class MapMLStyleVisitor extends AbstractStyleVisitor {
    public static final String RULE_ID_PREFIX = "rule-";
    public static final String SYMBOLIZER_ID_PREFIX = "symbolizer-";
    public static final String OPACITY = "opacity";

    /** Default radius for point symbolizers, see @link{Graphic#getSize()} */
    private static final Double DEFAULT_RADIUS = 8.0;

    public static final String STROKE_DASHARRAY = "stroke-dasharray";
    public static final String STROKE_LINECAP = "stroke-linecap";
    public static final String STROKE_WIDTH = "stroke-width";
    public static final String STROKE = "stroke";
    public static final String FILL = "fill";
    public static final String RADIUS = "r";
    public static final String RULE_SYMBOLIZER_DELIMITER = ":";
    public static final String STROKE_DASHOFFSET = "stroke-dashoffset";

    Map<String, MapMLStyle> styles = new HashMap<>();
    AtomicInteger ruleCounter = new AtomicInteger(0);
    AtomicInteger symbolizerCounter = new AtomicInteger(0);

    private MapMLStyle style;

    @Override
    public void visit(FeatureTypeStyle fts) {
        for (Rule r : fts.rules()) {
            ruleCounter.incrementAndGet();
            r.accept(this);
        }
    }

    @Override
    public void visit(PointSymbolizer ps) {
        createStyle(ps);
        if (ps.getGraphic() != null) {
            ps.getGraphic().accept(this);
        }
    }

    public void visit(Graphic gr) {
        if (isNotNullAndIsStatic(gr.getOpacity())) {
            double value = gr.getOpacity().evaluate(null, Double.class);
            style.setProperty(OPACITY, String.valueOf(value));
        }
        Double radius = DEFAULT_RADIUS;
        if (isNotNullAndIsStatic(gr.getSize())) {
            double value = gr.getSize().evaluate(null, Double.class);
            radius = value * DEFAULT_RADIUS;
        }
        style.setProperty(RADIUS, String.valueOf(radius));

        for (GraphicalSymbol gs : gr.graphicalSymbols()) {
            if (!(gs instanceof Symbol)) {
                throw new RuntimeException("Don't know how to visit " + gs);
            }

            gs.accept(this);
        }
    }

    @Override
    public void visit(Fill fill) {
        if (isNotNullAndIsStatic(fill.getColor())) {
            String value = fill.getColor().evaluate(null, String.class);
            style.setProperty(FILL, value);
        }
    }

    @Override
    public void visit(Stroke stroke) {
        if (isNotNullAndIsStatic(stroke.getColor())) {
            String value = stroke.getColor().evaluate(null, String.class);
            style.setProperty(STROKE, value);
        }
        if (isNotNullAndIsStatic(stroke.getOpacity())) {
            Double value = stroke.getOpacity().evaluate(null, Double.class);
            style.setProperty(OPACITY, String.valueOf(value));
        }
        if (isNotNullAndIsStatic(stroke.getWidth())) {
            Double value = stroke.getWidth().evaluate(null, Double.class);
            style.setProperty(STROKE_WIDTH, String.valueOf(value));
        }
        if (isNotNullAndIsStatic(stroke.getLineCap())) {
            String value = stroke.getLineCap().evaluate(null, String.class);
            style.setProperty(STROKE_LINECAP, value);
        }
        if (stroke.getDashArray() != null && stroke.getDashArray().length > 0) {
            String value =
                    IntStream.range(0, stroke.getDashArray().length)
                            .mapToObj(String::valueOf)
                            .collect(Collectors.joining(" "));
            style.setProperty(STROKE_DASHARRAY, value);
        }
        if (isNotNullAndIsStatic(stroke.getDashOffset())) {
            Integer value = stroke.getDashOffset().evaluate(null, Integer.class);
            style.setProperty(STROKE_DASHOFFSET, String.valueOf(value));
        }
    }

    @Override
    public void visit(LineSymbolizer line) {
        createStyle(line);
        if (line.getStroke() != null) {
            line.getStroke().accept(this);
        }
    }

    @Override
    public void visit(PolygonSymbolizer poly) {
        createStyle(poly);
        if (poly.getDescription() != null) {
            poly.getDescription().accept(this);
        }
        if (poly.getDisplacement() != null) {
            poly.getDisplacement().accept(this);
        }
        if (poly.getFill() != null) {
            poly.getFill().accept(this);
        }
        if (poly.getStroke() != null) {
            poly.getStroke().accept(this);
        }
    }

    private boolean isNotNullAndIsStatic(Expression ex) {
        return ex != null && (Boolean) ex.accept(IsStaticExpressionVisitor.VISITOR, null);
    }

    /**
     * Create a style for a symbolizer
     *
     * @param sym the symbolizer
     */
    private void createStyle(Symbolizer sym) {
        style = new MapMLStyle();
        style.setRuleId(ruleCounter.get());
        style.setSymbolizerId(symbolizerCounter.incrementAndGet());
        style.setSymbolizerType(sym.getClass().getSimpleName());
        styles.put(
                RULE_ID_PREFIX
                        + style.getRuleId()
                        + RULE_SYMBOLIZER_DELIMITER
                        + SYMBOLIZER_ID_PREFIX
                        + style.getSymbolizerId(),
                style);
    }

    public Map<String, MapMLStyle> getStyles() {
        return styles;
    }
}
