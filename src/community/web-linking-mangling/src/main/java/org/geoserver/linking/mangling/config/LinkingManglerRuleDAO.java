package org.geoserver.linking.mangling.config;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.config.util.SecureXStream;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.resource.Resource;
import org.geotools.util.logging.Logging;

public class LinkingManglerRuleDAO {
    private static final Logger LOGGER = Logging.getLogger(LinkingManglerRuleDAO.class);
    private static SecureXStream xStream;
    public static final String LINKING_MANGLE_RULES_PATH =
            "web-linking-mangling/linking-mangler-rules.xml";
    public static final String LINKING_MANGLE_RULES_PATH_TMP =
            "web-linking-mangling/%s-linking-mangler-rules.xml";
    private static final String DATA_DIRECTORY = "dataDirectory";

    static {
        xStream = new SecureXStream();
        xStream.registerConverter(new LinkingManglerRuleConverter());
        xStream.alias("LinkingManglerRule", LinkingManglerRule.class);
        xStream.alias("LinkingManglerRules", LinkingManglerRuleDAO.LinkingManglerRuleList.class);
        xStream.addImplicitCollection(LinkingManglerRuleDAO.LinkingManglerRuleList.class, "rules");
        xStream.allowTypes(
                new Class[] {
                    LinkingManglerRule.class, LinkingManglerRuleDAO.LinkingManglerRuleList.class
                });
    }

    public static List<LinkingManglerRule> getLinkingManglerRules(Resource resource) {
        if (resource.getType() == Resource.Type.RESOURCE) {
            try (InputStream inputStream = resource.in()) {
                if (inputStream.available() == 0) {
                    LOGGER.log(Level.FINE, "Linking Manager Rules file seems to be empty.");
                } else {
                    LinkingManglerRuleList list =
                            (LinkingManglerRuleList) xStream.fromXML(inputStream);
                    return list.rules == null ? new ArrayList<>() : list.rules;
                }
            } catch (Exception exception) {
                throw new LinkingManglerException(
                        exception, "Error parsing linking mangler rule files.");
            }
        } else {
            LOGGER.log(Level.INFO, "Linking mangler rules file does not exist.");
        }
        return new ArrayList<>();
    }

    public static void saveOrUpdateLinkingManglerRule(LinkingManglerRule linkingManglerRule) {
        Resource linkingManglerRules = getDataDirectory().get(LINKING_MANGLE_RULES_PATH);
        Resource tmplinkingManglerRules =
                getDataDirectory().get(getTmpLinkingManglerRulesPathPath());
        saveOrUpdateLinkingManglerRule(
                linkingManglerRule, linkingManglerRules, tmplinkingManglerRules);
        linkingManglerRules.delete();
        tmplinkingManglerRules.renameTo(linkingManglerRules);
    }

    private static GeoServerDataDirectory getDataDirectory() {
        return (GeoServerDataDirectory) GeoServerExtensions.bean(DATA_DIRECTORY);
    }

    public static String getTmpLinkingManglerRulesPathPath() {
        return String.format(LINKING_MANGLE_RULES_PATH_TMP, UUID.randomUUID());
    }

    public static void saveOrUpdateLinkingManglerRule(
            LinkingManglerRule linkingManglerRule, Resource input, Resource output) {
        List<LinkingManglerRule> linkingManglerRules = getLinkingManglerRules(input);
        boolean exists = false;
        for (int i = 0; i < linkingManglerRules.size() && !exists; i++) {
            if (linkingManglerRules.get(i).getId().equals(linkingManglerRule.getId())) {
                linkingManglerRules.set(i, linkingManglerRule);
                exists = true;
            }
        }
        if (!exists) {
            linkingManglerRules.add(linkingManglerRule);
        }

        writeLinkingManglerRules(linkingManglerRules, output);
    }

    public static void deletelinkingManglerRules(String... linkingManglerRulesIds) {
        Resource linkManglingRules = getDataDirectory().get(LINKING_MANGLE_RULES_PATH);
        Resource tmpLinkManglingRules = getDataDirectory().get(getTmpLinkingManglerRulesPathPath());
        deleteLinkingManglerRules(linkManglingRules, tmpLinkManglingRules, linkingManglerRulesIds);
        linkManglingRules.delete();
        tmpLinkManglingRules.renameTo(linkManglingRules);
    }

    public static void deleteLinkingManglerRules(
            Resource inputResource, Resource outputResource, String... forwardParameterIds) {

        List<LinkingManglerRule> collect =
                getLinkingManglerRules(inputResource).stream()
                        .filter(p -> !ArrayUtils.contains(forwardParameterIds, p.getId()))
                        .collect(Collectors.toList());

        writeLinkingManglerRules(collect, outputResource);
    }

    private static void writeLinkingManglerRules(
            List<LinkingManglerRule> linkingManglerRules, Resource output) {
        try (OutputStream outputStream = output.out()) {
            xStream.toXML(new LinkingManglerRuleList(linkingManglerRules), outputStream);
        } catch (Throwable exception) {
            throw new LinkingManglerException(
                    exception, "Something bad happened when writing linking mangler rules.");
        }
    }

    private static final class LinkingManglerException extends RuntimeException {

        public LinkingManglerException(
                Throwable cause, String message, Object... messageArguments) {
            super(String.format(message, messageArguments), cause);
        }
    }
    /** Support class for XStream serialization */
    static final class LinkingManglerRuleList {
        List<LinkingManglerRule> rules;

        public LinkingManglerRuleList(List<LinkingManglerRule> rules) {
            this.rules = rules;
        }
    }
}
