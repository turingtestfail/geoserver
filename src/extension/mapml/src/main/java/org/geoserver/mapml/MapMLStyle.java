package org.geoserver.mapml;

import java.util.HashMap;
import java.util.Map;

public class MapMLStyle {
    private int ruleId;
    private int symbolizerId;
    private String symbolizerType;

    private Map<String, String> properties = new HashMap<>();

    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }

    public int getRuleId() {
        return ruleId;
    }

    public int getSymbolizerId() {
        return symbolizerId;
    }

    public void setSymbolizerId(int symbolizerId) {
        this.symbolizerId = symbolizerId;
    }

    public String getSymbolizerType() {
        return symbolizerType;
    }

    public void setSymbolizerType(String symbolizerType) {
        this.symbolizerType = symbolizerType;
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }
}
