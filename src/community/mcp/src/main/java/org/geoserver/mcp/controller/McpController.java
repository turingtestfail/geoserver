package org.geoserver.mcp.controller;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.config.GeoServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class McpController{
    private final GeoServer geoServer;
    private final Catalog catalog;
    @Autowired
    public McpController(GeoServer geoServer) {
        this.geoServer = geoServer;
        catalog = geoServer.getCatalog();
    }


    @Tool(description = "Lists all available workspaces on the GeoServer instance.")
    public List<String> listWorkspaces() {
        return catalog.getWorkspaces().stream()
                .map(WorkspaceInfo::getName)
                .collect(Collectors.toList());
    }

    // TEMPORARY: Just for you to validate the bean is alive right now
    @GetMapping(value = "/mcp/test", produces = "application/json")
    public List<String> testEndpoint() {
        return listWorkspaces();
    }
}
