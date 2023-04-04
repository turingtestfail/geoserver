package org.geoserver.linking.mangling.config;

import com.thoughtworks.xstream.converters.Converter;import com.thoughtworks.xstream.converters.MarshallingContext;import com.thoughtworks.xstream.converters.UnmarshallingContext;import com.thoughtworks.xstream.io.HierarchicalStreamReader;import com.thoughtworks.xstream.io.HierarchicalStreamWriter;import java.util.Optional;import java.util.UUID;

public class LinkingManglerRuleConverter implements Converter {
    @Override
    public void marshal(
            Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        LinkingManglerRule rule = (LinkingManglerRule) source;
        writer.addAttribute("id", rule.getId());
        writer.addAttribute("transformer", rule.getTransformer());
        writer.addAttribute("matcher", rule.getMatcher());
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return new LinkingManglerRule(
                Optional.ofNullable(reader.getAttribute("id"))
                        .orElse(UUID.randomUUID().toString()),
                reader.getAttribute("matcher"),
                reader.getAttribute("transformer"),
                Boolean.valueOf(reader.getAttribute("activated")));
    }

    @Override
    public boolean canConvert(Class type) {
        return LinkingManglerRule.class.equals(type);
    }
}
