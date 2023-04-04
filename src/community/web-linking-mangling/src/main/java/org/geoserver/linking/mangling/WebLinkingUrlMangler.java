package org.geoserver.linking.mangling;

import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.linking.mangling.config.LinkingManglerRule;
import org.geoserver.linking.mangling.config.LinkingManglerRuleDAO;
import org.geoserver.ows.URLMangler;
import org.geoserver.platform.resource.Resource;
import org.geotools.util.logging.Logging;
import java.util.List;import java.util.Map;import java.util.logging.Logger;
import static org.geoserver.linking.mangling.config.LinkingManglerRuleDAO.getLinkingManglerRules;

public class WebLinkingUrlMangler implements URLMangler {
    private static final Logger LOGGER = Logging.getLogger(WebLinkingUrlMangler.class);
    private List<LinkingManglerRule> linkingManglerRules;
    public WebLinkingUrlMangler(GeoServerDataDirectory dataDirectory) {
        Resource resource = dataDirectory.get(LinkingManglerRuleDAO.LINKING_MANGLE_RULES_PATH);
        linkingManglerRules = getLinkingManglerRules(resource);
        resource.addListener(notify -> linkingManglerRules = getLinkingManglerRules(resource));
    }



    @Override public void mangleURL(StringBuilder baseURL, StringBuilder path, Map<String,String> kvp, URLType type) {

    }
}
