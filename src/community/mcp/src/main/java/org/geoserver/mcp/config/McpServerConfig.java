package org.geoserver.mcp.config;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import org.geoserver.mcp.controller.McpController;
import org.springframework.ai.mcp.server.webmvc.transport.WebMvcStreamableServerTransportProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class McpServerConfig {

    @Bean
    public WebMvcStreamableServerTransportProvider mcpServerTransportProvider() {
        return WebMvcStreamableServerTransportProvider.builder().build();
    }

    @Bean
    public McpSyncServer mcpServer(WebMvcStreamableServerTransportProvider transportProvider, McpController mcpController) {
        // 1. Initialize the standard core specification builder
        var serverBuilder = McpServer.sync(transportProvider)
                .serverInfo("GeoServer MCP Server", "1.0.0");

        // 2. Reflectively discover @Tool annotated methods from your controller
        MethodToolCallbackProvider provider = MethodToolCallbackProvider.builder()
                .toolObjects(mcpController)
                .build();

        // 3. Register each callback back down to the core protocol definition
        for (ToolCallback callback : provider.getToolCallbacks()) {
            Tool toolSpec = Tool.builder()
                    .name(callback.getName())
                    .description(callback.getDescription())
                    .inputSchema(callback.getInputSchema())
                    .build();

            serverBuilder.toolCall(toolSpec, (exchange, request) -> {
                // Bridge the incoming JSON map execution payload to the Spring tool layer
                @SuppressWarnings("unchecked")
                Map<String, Object> arguments = (Map<String, Object>) request.arguments();
                String jsonResult = callback.call(arguments);

                return CallToolResult.builder()
                        .text(jsonResult)
                        .build();
            });
        }

        return serverBuilder.build();
    }
}