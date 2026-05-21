package org.geoserver.mcp.controller;

import org.geoserver.config.GeoServer;
import org.geoserver.rest.RestBaseController; // Core GeoServer rest base class
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class McpController extends RestBaseController {
    @Autowired
    public McpController(GeoServer geoServer) {
        super(); // <--- PUT A BREAKPOINT HERE
    }

    @GetMapping(produces = "application/json", path = RestBaseController.ROOT_PATH + "/mcp", name = "MCPTest")
    public String exportMap() {
        return "hello world";
    }
}
