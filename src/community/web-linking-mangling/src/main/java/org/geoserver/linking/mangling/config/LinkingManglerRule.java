package org.geoserver.linking.mangling.config;

public class LinkingManglerRule {
    private String id;
    private String matcher;
    private String transformer;
    private boolean activated;

    public LinkingManglerRule(String id, String matcher, String transformer, boolean activated) {
        this.id = id;
        this.matcher = matcher;
        this.transformer = transformer;
        this.activated = activated;
    }

    public String getMatcher() {
        return matcher;
    }

    public void setMatcher(String matcher) {
        this.matcher = matcher;
    }

    public String getTransformer() {
        return transformer;
    }

    public void setTransformer(String transformer) {
        this.transformer = transformer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }
}
