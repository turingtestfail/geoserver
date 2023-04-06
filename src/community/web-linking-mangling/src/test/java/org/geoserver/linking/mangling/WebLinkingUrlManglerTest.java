package org.geoserver.linking.mangling;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.geoserver.linking.mangling.config.LinkingManglerRule;
import org.geoserver.ows.URLMangler;
import org.junit.BeforeClass;
import org.junit.Test;

public class WebLinkingUrlManglerTest {
    private static WebLinkingUrlMangler mangler;

    @BeforeClass
    public static void setup() {
        List<LinkingManglerRule> manglerRules = new ArrayList<>();
        manglerRules.add(
                new LinkingManglerRule(
                        "1",
                        "geoserver/ogc/stac/v1/collections/(.*)",
                        "https://stac.example.com/v1/collections/",
                        true));
        mangler = new WebLinkingUrlMangler(manglerRules);
    }

    @Test
    public void testMangleWildCard() throws Exception {
        StringBuilder baseURL = new StringBuilder("http://localhost:8080/");
        StringBuilder path = new StringBuilder("geoserver/ogc/stac/v1/collections/");
        mangler.mangleURL(baseURL, path, Collections.emptyMap(), URLMangler.URLType.SERVICE);
        assertEquals("https://stac.example.com", baseURL.toString());
        assertEquals("/v1/collections/", path.toString());
    }
}
