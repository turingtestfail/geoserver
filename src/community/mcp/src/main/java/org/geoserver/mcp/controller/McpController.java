package org.geoserver.mcp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class McpController  {

    @GetMapping(
            produces = "application/json",
            path = "/mcp",
            name = "MCPTest")
    @ResponseBody
    public ExportMap exportMap(@PathVariable String workspaceName, HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();
        String requestParameters = request.getQueryString();
        String updatedRequestParameters = requestParameters.replaceAll("\\bf=[^&]+", "f=image&format=png");

        return new ExportMap(requestURL + updatedRequestParameters);
    }
}
