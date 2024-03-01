package org.geoserver.mapml;

import org.geotools.api.filter.expression.Literal;
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
import org.geotools.styling.AbstractStyleVisitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MapMLStyleVisitor extends AbstractStyleVisitor {
    public static final String RULE_ID_PREFIX = "rule-";
    public static final String SYMBOLIZER_ID_PREFIX = "symbolizer-";
    public static final String OPACITY = "opacity";
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
        if (ps.getDescription() != null) {
            ps.getDescription().accept(this);
        }
        if (ps.getGraphic() != null) {
            ps.getGraphic().accept(this);
        }
    }

    public void visit(Graphic gr) {
        if (gr.getOpacity() != null) {
            if (gr.getOpacity() instanceof Literal) {
                Literal lo = (Literal) gr.getOpacity();
                double value = lo.evaluate(null, Double.class).doubleValue();
                style.setProperty(OPACITY, String.valueOf(value));
            }
        }

        for (GraphicalSymbol gs : gr.graphicalSymbols()) {
            if (!(gs instanceof Symbol)) {
                throw new RuntimeException("Don't know how to visit " + gs);
            }

            gs.accept(this);
        }
    }

    @Override
    public void visit(Fill fill) {
        if (fill.getColor() != null && fill.getColor() instanceof Literal) {
            Literal color = (Literal) fill.getColor();
            String value = color.evaluate(null, String.class);
            style.setProperty("fill", value);
        }
    }

    @Override
    public void visit(Stroke stroke) {
        if (stroke != null && stroke.getColor() != null && stroke.getColor() instanceof Literal) {
            Literal color = (Literal) stroke.getColor();
            String value = color.evaluate(null, String.class);
            style.setProperty("stroke", value);
        }
    }

    @Override
    public void visit(LineSymbolizer line) {
        createStyle(line);
        if (line.getDescription() != null) {
            line.getDescription().accept(this);
        }
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
                        + ":"
                        + SYMBOLIZER_ID_PREFIX
                        + style.getSymbolizerId(),
                style);
    }

    public Map<String, MapMLStyle> getStyles() {
        return styles;
    }
}
