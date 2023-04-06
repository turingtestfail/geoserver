package org.geoserver.linking.mangling;

import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.linking.mangling.config.LinkingManglerRule;
import org.geoserver.linking.mangling.config.LinkingManglerRuleDAO;
import org.geoserver.ows.HTTPHeadersCollector;import org.geoserver.ows.ProxifyingURLMangler;import org.geoserver.ows.URLMangler;
import org.geoserver.platform.resource.Resource;
import org.geotools.util.logging.Logging;
import java.net.MalformedURLException;import java.net.URL;import java.util.Arrays;import java.util.HashMap;import java.util.List;import java.util.Map;import java.util.Optional;import java.util.logging.Level;import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static org.geoserver.linking.mangling.config.LinkingManglerRuleDAO.getLinkingManglerRules;

public class WebLinkingUrlMangler implements URLMangler {
    private static final Logger LOGGER = Logging.getLogger(WebLinkingUrlMangler.class);
    private static final Pattern TEMPLATE_LITERAL_PATTERN = Pattern.compile("\\$\\{(.*?)\\}");
    public static final String TEMPLATE_PREFIX = "${";
    public static final String TEMPLATE_POSTFIX = "}";
    public static final String PROTOCOL_SEPARATOR = "://";

    private List<LinkingManglerRule> linkingManglerRules;
    public WebLinkingUrlMangler(GeoServerDataDirectory dataDirectory) {
        Resource resource = dataDirectory.get(LinkingManglerRuleDAO.LINKING_MANGLE_RULES_PATH);
        linkingManglerRules = getLinkingManglerRules(resource);
        resource.addListener(notify -> linkingManglerRules = getLinkingManglerRules(resource));
    }

    /**
     * Constructor for testing purposes
     */
    protected WebLinkingUrlMangler(List<LinkingManglerRule> linkingManglerRules){
        this.linkingManglerRules=linkingManglerRules;
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
                transformURL(baseURL,path, transformer.get());
                return;
            }
            //if there are template literals, collect the headers
            Map<String,String> headers = collectHeaders(templateLiterals);
            //check if headers are missing
            if(headers.size() != templateLiterals.size()) {
                LOGGER.info("Web Linking URL Mangler: Some headers are missing, cannot transform the URL using the transformer: "
                        + transformer.get() );
                return;
            }
            transformURL(baseURL,path, transformer.get(), headers);
        }

    }
    private void transformURL(StringBuilder baseURL, StringBuilder path, String transformer, Map<String,String> headers) {
        String transformerReplacedLiterals = transformer;
        if(headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                transformerReplacedLiterals = transformerReplacedLiterals.replace(TEMPLATE_PREFIX + entry.getKey()
                        + TEMPLATE_POSTFIX, entry.getValue());
            }
        }
        try{
            URL url = new URL(transformerReplacedLiterals);
            baseURL.setLength(0);
            baseURL.append( url.getProtocol() + PROTOCOL_SEPARATOR + url.getHost());
            path.setLength(0);
            path.append(url.getPath());
        }catch (MalformedURLException e) {
            LOGGER.log(Level.ALL,"Web Linking URL Mangler: The transformer, after header template replacement: "
                    + transformerReplacedLiterals + " is not a valid URL", e);
            return;
        }

    }
    private void transformURL(StringBuilder baseURL, StringBuilder path, String transformer) {
        transformURL(baseURL, path, transformer, null);
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

