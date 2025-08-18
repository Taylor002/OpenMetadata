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

package org.openmetadata.service.resources.services.mcp;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.openmetadata.service.util.TestUtils.ADMIN_AUTH_HEADERS;
import static org.openmetadata.service.util.TestUtils.assertListNotNull;
import static org.openmetadata.service.util.TestUtils.assertListNull;
import static org.openmetadata.service.util.TestUtils.assertResponse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpResponseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.openmetadata.schema.api.services.CreateMcpService;
import org.openmetadata.schema.api.services.mcp.CreateMcpToolRequest;
import org.openmetadata.schema.entity.services.McpService;
import org.openmetadata.schema.entity.services.mcp.McpTool;
import org.openmetadata.schema.utils.JsonUtils;
import org.openmetadata.service.Entity;
import org.openmetadata.service.resources.EntityResourceTest;
import org.openmetadata.service.resources.services.McpServiceResourceTest;
import org.openmetadata.service.util.ResultList;

@Slf4j
class McpToolResourceTest extends EntityResourceTest<McpTool, CreateMcpToolRequest> {

  private McpService mcpService;

  public McpToolResourceTest() {
    super(
        Entity.MCP_TOOL,
        McpTool.class,
        McpToolResource.McpToolList.class,
        "mcpTools",
        McpToolResource.FIELDS);
    this.supportsPatch = false;
  }

  @Override
  public void setup(TestInfo test) throws URISyntaxException, IOException {
    super.setup(test);
    // Create a parent MCP service for the tools
    CreateMcpService createService =
        new McpServiceResourceTest().createRequest(test.getDisplayName());
    mcpService = new McpServiceResourceTest().createEntity(createService, ADMIN_AUTH_HEADERS);
  }

  @Test
  void post_validMcpTool_200_ok(TestInfo test) throws IOException {
    // Create MCP tool with different configurations
    CreateMcpToolRequest create =
        createRequest(test)
            .withToolType(CreateMcpToolRequest.McpToolType.FILE_OPERATION)
            .withCategory("file-operations")
            .withInputSchema(
                "{\"type\": \"object\", \"properties\": {\"path\": {\"type\": \"string\"}}}")
            .withOutputSchema(
                "{\"type\": \"object\", \"properties\": {\"content\": {\"type\": \"string\"}}}");

    McpTool tool = createAndCheckEntity(create, ADMIN_AUTH_HEADERS);
    assertEquals(CreateMcpToolRequest.McpToolType.FILE_OPERATION, tool.getToolType());
    assertEquals("file-operations", tool.getCategory());
    assertNotNull(tool.getInputSchema());
    assertNotNull(tool.getOutputSchema());
  }

  @Test
  void post_mcpToolWithoutService_400(TestInfo test) {
    CreateMcpToolRequest create = createRequest(test).withService(null);
    assertResponse(
        () -> createEntity(create, ADMIN_AUTH_HEADERS), BAD_REQUEST, "[service must not be null]");
  }

  @Test
  void post_mcpToolWithInvalidService_404(TestInfo test) {
    CreateMcpToolRequest create = createRequest(test).withService(UUID.randomUUID().toString());
    assertResponse(
        () -> createEntity(create, ADMIN_AUTH_HEADERS),
        NOT_FOUND,
        "mcpService instance for " + create.getService() + " not found");
  }

  @Test
  void put_updateMcpTool_200(TestInfo test) throws IOException {
    CreateMcpToolRequest create = createRequest(test);
    McpTool tool = createAndCheckEntity(create, ADMIN_AUTH_HEADERS);

    // Update tool fields
    create
        .withCategory("updated-category")
        .withToolType(CreateMcpToolRequest.McpToolType.DATA_OPERATION)
        .withInputSchema(
            "{\"type\": \"object\", \"properties\": {\"query\": {\"type\": \"string\"}}}");

    McpTool updated = updateEntity(create, OK, ADMIN_AUTH_HEADERS);
    assertEquals("updated-category", updated.getCategory());
    assertEquals(CreateMcpToolRequest.McpToolType.DATA_OPERATION, updated.getToolType());
  }

  @Test
  void get_mcpToolsByService_200(TestInfo test) throws IOException {
    // Create multiple tools for the same service
    CreateMcpToolRequest create1 = createRequest(test, 1).withCategory("category1");
    CreateMcpToolRequest create2 = createRequest(test, 2).withCategory("category2");
    CreateMcpToolRequest create3 = createRequest(test, 3).withCategory("category3");

    createEntity(create1, ADMIN_AUTH_HEADERS);
    createEntity(create2, ADMIN_AUTH_HEADERS);
    createEntity(create3, ADMIN_AUTH_HEADERS);

    // List tools by service
    Map<String, String> queryParams = Map.of("service", mcpService.getId().toString());
    ResultList<McpTool> tools = listEntities(queryParams, ADMIN_AUTH_HEADERS);

    assertEquals(3, tools.getData().size());
  }

  @Override
  public CreateMcpToolRequest createRequest(String name) {
    // Ensure MCP service is created if not already done
    if (mcpService == null) {
      try {
        CreateMcpService createService =
            new McpServiceResourceTest().createRequest("test-mcp-service-" + UUID.randomUUID());
        mcpService = new McpServiceResourceTest().createEntity(createService, ADMIN_AUTH_HEADERS);
      } catch (IOException e) {
        throw new RuntimeException("Failed to create MCP service", e);
      }
    }
    return new CreateMcpToolRequest()
        .withName(name)
        .withService(mcpService.getId().toString())
        .withDescription("Test MCP tool")
        .withToolType(CreateMcpToolRequest.McpToolType.CUSTOM)
        .withCategory("general");
  }

  @Override
  public void validateCreatedEntity(
      McpTool tool, CreateMcpToolRequest request, Map<String, String> authHeaders) {
    assertEquals(request.getName(), tool.getName());
    assertEquals(request.getDescription(), tool.getDescription());
    assertEquals(request.getToolType(), tool.getToolType());
    assertEquals(request.getCategory(), tool.getCategory());
    assertNotNull(tool.getService());
    assertEquals(mcpService.getId(), tool.getService().getId());
  }

  @Override
  public void compareEntities(McpTool expected, McpTool updated, Map<String, String> authHeaders) {
    assertEquals(expected.getName(), updated.getName());
    assertEquals(expected.getToolType(), updated.getToolType());
    assertEquals(expected.getCategory(), updated.getCategory());
  }

  @Override
  public McpTool validateGetWithDifferentFields(McpTool tool, boolean byName)
      throws HttpResponseException {
    String fields = "";
    tool =
        byName
            ? getEntityByName(tool.getFullyQualifiedName(), fields, ADMIN_AUTH_HEADERS)
            : getEntity(tool.getId(), fields, ADMIN_AUTH_HEADERS);
    assertListNull(tool.getOwners());

    fields = "owners,tags";
    tool =
        byName
            ? getEntityByName(tool.getFullyQualifiedName(), fields, ADMIN_AUTH_HEADERS)
            : getEntity(tool.getId(), fields, ADMIN_AUTH_HEADERS);
    assertListNotNull(tool.getOwners());
    assertListNotNull(tool.getTags());

    return tool;
  }

  @Override
  public void assertFieldChange(String fieldName, Object expected, Object actual) {
    if (fieldName.equals("toolType") || fieldName.equals("category")) {
      assertEquals(expected, actual);
    } else if (fieldName.equals("inputSchema") || fieldName.equals("outputSchema")) {
      // Schema comparison - parse JSON to compare
      assertEquals(JsonUtils.readTree(expected.toString()), JsonUtils.readTree(actual.toString()));
    } else {
      assertCommonFieldChange(fieldName, expected, actual);
    }
  }
}
