package org.geoserver.llm.web;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssUrlReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptUrlReferenceHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.response.StringResponse;
import org.geoserver.ows.URLMangler;
import org.geoserver.ows.util.ResponseUtils;

import org.geoserver.template.TemplateUtils;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.GeoServerBasePage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static freemarker.ext.beans.BeansWrapper.EXPOSE_NOTHING;

public class LlmDemo extends WebPage {

    public LlmDemo() {}

    @Override
    public void renderHead(IHeaderResponse header) {
        super.renderHead(header);
        try {
            renderHeaderScript(header);
        } catch (IOException | TemplateException e) {
            throw new WicketRuntimeException(e);
        }
    }

    private void renderHeaderScript(IHeaderResponse header) throws IOException, TemplateException {
        HttpServletRequest req = GeoServerApplication.get().servletRequest();
        String base = ResponseUtils.baseURL(req);
        header.render(CssHeaderItem.forReference(new PackageResourceReference(LlmDemo.class, "css/ol.css")));
        header.render(CssHeaderItem.forReference(new PackageResourceReference(LlmDemo.class, "css/llm-demo.css")));
        header.render(JavaScriptHeaderItem.forScript(
                """
window.Worker = class { constructor() { throw new Error("Workers disabled by CSP"); } };
window.URL.createObjectURL = function() { throw new Error("createObjectURL blocked"); };
window.ol = window.ol || {};
ol.has = { WEB_WORKER: false };
""",
                "disable-workers"));
        header.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(LlmDemo.class, "js/ol.js")));
        header.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(LlmDemo.class, "js/llm-demo.js")));
    }
}
