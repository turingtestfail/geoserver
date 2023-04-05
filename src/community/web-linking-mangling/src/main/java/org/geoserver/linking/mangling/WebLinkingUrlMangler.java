package org.geoserver.linking.mangling;

import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.linking.mangling.config.LinkingManglerRule;
import org.geoserver.linking.mangling.config.LinkingManglerRuleDAO;
import org.geoserver.ows.HTTPHeadersCollector;import org.geoserver.ows.ProxifyingURLMangler;import org.geoserver.ows.URLMangler;
import org.geoserver.platform.resource.Resource;
import org.geotools.util.logging.Logging;
import java.util.Arrays;import java.util.HashMap;import java.util.List;import java.util.Map;import java.util.Optional;import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static org.geoserver.linking.mangling.config.LinkingManglerRuleDAO.getLinkingManglerRules;

public class WebLinkingUrlMangler implements URLMangler {
    private static final Logger LOGGER = Logging.getLogger(WebLinkingUrlMangler.class);
    private static Pattern TEMPLATE_LITERAL_PATTERN = Pattern.compile("\\$\\{(.*?)\\}");

    protected List<LinkingManglerRule> linkingManglerRules;
    public WebLinkingUrlMangler(GeoServerDataDirectory dataDirectory) {
        Resource resource = dataDirectory.get(LinkingManglerRuleDAO.LINKING_MANGLE_RULES_PATH);
        linkingManglerRules = getLinkingManglerRules(resource);
        resource.addListener(notify -> linkingManglerRules = getLinkingManglerRules(resource));
    }



    @Override public void mangleURL(StringBuilder baseURL, StringBuilder path, Map<String,String> kvp, URLType type) {
        //does the path match any of the rules?
        Optional<String> transformer = getFirstMatchingTransformer(path.toString());
        if (transformer.isPresent()) {
            //get the template literals from the transformer
            List<String> templateLiterals = TEMPLATE_LITERAL_PATTERN.matcher(transformer.get()).results()
                    .map(matchResult -> matchResult.group(1)).collect(Collectors.toList());
            if(templateLiterals.isEmpty()) {
                //if there are no template literals, just replace the url with the transformer
                transformURL(baseURL,path, transformer);
                return;
            }
            //if there are template literals, collect the headers
            Map<String,String> headers = collectHeaders(templateLiterals);
            //check if headers are missing
            if(headers.size() != templateLiterals.size()) {
                LOGGER.info("Web Linking URL Mangler: Some headers are missing, cannot transform the URL using the transformer: " + transformer.get() );
                return;
            }
            transformURL(baseURL,path, transformer, headers);
        }

    }

    private Optional<String> getFirstMatchingTransformer(String path) {
        return linkingManglerRules.stream()
                .filter(rule -> ruleMatches(path, rule))
                .map(LinkingManglerRule::getTransformer)
                .findFirst();
    }
    private boolean ruleMatches(String path, LinkingManglerRule rule) {
        return path.matches(rule.getMatcher());
    }
    private Map<String,String> collectHeaders(List<String> requiredHeaders) {
        return
                requiredHeaders.stream()
                        .filter(headerName -> HTTPHeadersCollector.getHeader(headerName) != null)
        .collect(Collectors.toMap(item -> item,HTTPHeadersCollector::getHeader ));
    }


}

