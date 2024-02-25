package org.geoserver.mapml;

public class MapMLStyle {
    private int ruleId;
    private int symbolizerId;
    private String symbolizerType;

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
}
