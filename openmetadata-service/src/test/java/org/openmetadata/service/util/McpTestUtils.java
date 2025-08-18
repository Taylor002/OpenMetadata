/*
 *  Copyright 2021 Collate
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openmetadata.service.util;

import java.util.List;
import java.util.Map;
import org.openmetadata.schema.services.connections.mcp.McpConnection;

public final class McpTestUtils {

  private McpTestUtils() {
    // Private constructor to prevent instantiation
  }

  public static final McpConnection DEFAULT_MCP_CONNECTION = createDefaultMcpConnection();

  public static final McpConnection STDIO_MCP_CONNECTION = createStdioMcpConnection();

  public static final McpConnection SSE_MCP_CONNECTION = createSseMcpConnection();

  private static McpConnection createDefaultMcpConnection() {
    Map<String, Object> config =
        Map.of(
            "command", "npx",
            "args", List.of("-y", "@modelcontextprotocol/server-everything"),
            "env", Map.of("NODE_ENV", "test"));

    return new McpConnection().withConfig(config);
  }

  private static McpConnection createStdioMcpConnection() {
    Map<String, Object> config =
        Map.of(
            "command",
            "node",
            "args",
            List.of("/path/to/mcp-server.js"),
            "env",
            Map.of("MCP_MODE", "stdio", "LOG_LEVEL", "debug"),
            "cwd",
            "/app/mcp-servers");

    return new McpConnection().withConfig(config);
  }

  private static McpConnection createSseMcpConnection() {
    Map<String, Object> config =
        Map.of(
            "command", "python",
            "args", List.of("-m", "mcp_server", "--mode", "sse", "--port", "8080"),
            "env", Map.of("MCP_MODE", "sse", "MCP_PORT", "8080"));

    return new McpConnection().withConfig(config);
  }

  // TODO: Update these methods to work with separate MCP entity types
  // The following methods are commented out as we've moved to separate entities
  /*
  public static McpTool createSampleTool(String name, String description) {
    String inputSchema =
        """
        {
          "type": "object",
          "properties": {
            "input": {
              "type": "string",
              "description": "Input parameter"
            }
          },
          "required": ["input"]
        }
        """;
    return new McpTool()
        .withName(name)
        .withDescription(description)
        .withCategory("sample")
        .withInputSchema(inputSchema);
  }

  public static McpResource createSampleResource(String uri, String name) {
    return new McpResource()
        .withUri(uri)
        .withName(name)
        .withDescription(name + " resource")
        .withMimeType("text/plain");
  }

  public static McpPrompt createSamplePrompt(String name, String description) {
    return new McpPrompt()
        .withName(name)
        .withDescription(description)
        .withArguments(
            List.of(
                new McpPromptArgument()
                    .withName("param1")
                    .withDescription("First parameter")
                    .withType("string")
                    .withRequired(true),
                new McpPromptArgument()
                    .withName("param2")
                    .withDescription("Second parameter")
                    .withType("number")
                    .withRequired(false)));
  }

  public static List<McpTool> createSampleTools() {
    return List.of(
        createSampleTool("read_file", "Read file contents"),
        createSampleTool("write_file", "Write file contents"),
        createSampleTool("list_directory", "List directory contents"));
  }

  public static List<McpResource> createSampleResources() {
    return List.of(
        createSampleResource("file:///docs/api.md", "API Documentation"),
        createSampleResource("file:///data/config.json", "Configuration"),
        createSampleResource("https://api.example.com/schema", "API Schema"));
  }

  public static List<McpPrompt> createSamplePrompts() {
    return List.of(
        createSamplePrompt("code_review", "Review code for best practices"),
        createSamplePrompt("generate_docs", "Generate documentation from code"),
        createSamplePrompt("optimize_query", "Optimize database query"));
  }
  */
}
