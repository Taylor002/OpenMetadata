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
import org.openmetadata.schema.api.services.mcp.CreateMcpResourceRequest;
import org.openmetadata.schema.entity.services.McpService;
import org.openmetadata.schema.entity.services.mcp.McpResource;
import org.openmetadata.service.Entity;
import org.openmetadata.service.resources.EntityResourceTest;
import org.openmetadata.service.resources.services.McpServiceResourceTest;
import org.openmetadata.service.util.ResultList;

@Slf4j
class McpResourceResourceTest extends EntityResourceTest<McpResource, CreateMcpResourceRequest> {

  private McpService mcpService;

  public McpResourceResourceTest() {
    super(
        Entity.MCP_RESOURCE,
        McpResource.class,
        McpResourceResource.McpResourceList.class,
        "mcpResources",
        McpResourceResource.FIELDS);
    this.supportsPatch = false;
  }

  @Override
  public void setup(TestInfo test) throws URISyntaxException, IOException {
    super.setup(test);
    // Create a parent MCP service for the resources
    CreateMcpService createService =
        new McpServiceResourceTest().createRequest(test.getDisplayName());
    mcpService = new McpServiceResourceTest().createEntity(createService, ADMIN_AUTH_HEADERS);
  }

  @Test
  void post_validMcpResource_200_ok(TestInfo test) throws IOException {
    // Create MCP resource with different configurations
    CreateMcpResourceRequest create =
        createRequest(test)
            .withResourceType(CreateMcpResourceRequest.McpResourceType.FILE)
            .withUri("file:///docs/api-guide.md")
            .withMimeType("text/markdown")
            .withSize(1024)
            .withLastModified(System.currentTimeMillis());

    McpResource resource = createAndCheckEntity(create, ADMIN_AUTH_HEADERS);
    assertEquals(CreateMcpResourceRequest.McpResourceType.FILE, resource.getResourceType());
    assertEquals("file:///docs/api-guide.md", resource.getUri());
    assertEquals("text/markdown", resource.getMimeType());
    assertEquals(Integer.valueOf(1024), resource.getSize());
    assertNotNull(resource.getLastModified());
  }

  @Test
  void post_mcpResourceWithoutService_400(TestInfo test) {
    CreateMcpResourceRequest create = createRequest(test).withService(null);
    assertResponse(
        () -> createEntity(create, ADMIN_AUTH_HEADERS), BAD_REQUEST, "[service must not be null]");
  }

  @Test
  void post_mcpResourceWithoutUri_400(TestInfo test) {
    CreateMcpResourceRequest create = createRequest(test).withUri(null);
    assertResponse(
        () -> createEntity(create, ADMIN_AUTH_HEADERS), BAD_REQUEST, "[uri must not be null]");
  }

  @Test
  void put_updateMcpResource_200(TestInfo test) throws IOException {
    CreateMcpResourceRequest create =
        createRequest(test).withUri("file:///initial.txt").withMimeType("text/plain");
    McpResource resource = createAndCheckEntity(create, ADMIN_AUTH_HEADERS);

    // Update resource fields
    create
        .withResourceType(CreateMcpResourceRequest.McpResourceType.CUSTOM)
        .withUri("file:///config.yaml")
        .withMimeType("application/yaml")
        .withSize(2048);

    McpResource updated = updateEntity(create, OK, ADMIN_AUTH_HEADERS);
    assertEquals(CreateMcpResourceRequest.McpResourceType.CUSTOM, updated.getResourceType());
    assertEquals("file:///config.yaml", updated.getUri());
    assertEquals("application/yaml", updated.getMimeType());
    assertEquals(Integer.valueOf(2048), updated.getSize());
  }

  @Test
  void get_mcpResourcesByService_200(TestInfo test) throws IOException {
    // Create multiple resources for the same service
    CreateMcpResourceRequest create1 =
        createRequest(test, 1)
            .withUri("file:///resource1.txt")
            .withResourceType(CreateMcpResourceRequest.McpResourceType.FILE);
    CreateMcpResourceRequest create2 =
        createRequest(test, 2)
            .withUri("file:///resource2.json")
            .withResourceType(CreateMcpResourceRequest.McpResourceType.FILE);
    CreateMcpResourceRequest create3 =
        createRequest(test, 3)
            .withUri("https://api.example.com/data")
            .withResourceType(CreateMcpResourceRequest.McpResourceType.API);

    createEntity(create1, ADMIN_AUTH_HEADERS);
    createEntity(create2, ADMIN_AUTH_HEADERS);
    createEntity(create3, ADMIN_AUTH_HEADERS);

    // List resources by service
    Map<String, String> queryParams = Map.of("service", mcpService.getId().toString());
    ResultList<McpResource> resources = listEntities(queryParams, ADMIN_AUTH_HEADERS);

    assertEquals(3, resources.getData().size());
  }

  @Test
  void test_mcpResourceWithMetadata(TestInfo test) throws IOException {
    // Test resource with metadata
    CreateMcpResourceRequest create =
        createRequest(test)
            .withUri("https://api.example.com/users")
            .withResourceType(CreateMcpResourceRequest.McpResourceType.API)
            .withMimeType("application/json");
    // TODO: Add metadata when type conversion is implemented

    McpResource resource = createAndCheckEntity(create, ADMIN_AUTH_HEADERS);
    assertEquals(CreateMcpResourceRequest.McpResourceType.API, resource.getResourceType());
  }

  @Override
  public CreateMcpResourceRequest createRequest(String name) {
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
    return new CreateMcpResourceRequest()
        .withName(name)
        .withService(mcpService.getId().toString())
        .withDescription("Test MCP resource")
        .withResourceType(CreateMcpResourceRequest.McpResourceType.FILE)
        .withUri("file:///test/" + name);
  }

  @Override
  public void validateCreatedEntity(
      McpResource resource, CreateMcpResourceRequest request, Map<String, String> authHeaders) {
    assertEquals(request.getName(), resource.getName());
    assertEquals(request.getDescription(), resource.getDescription());
    assertEquals(request.getResourceType(), resource.getResourceType());
    assertEquals(request.getUri(), resource.getUri());
    assertEquals(request.getMimeType(), resource.getMimeType());
    assertEquals(request.getSize(), resource.getSize());
    assertNotNull(resource.getService());
    assertEquals(mcpService.getId(), resource.getService().getId());
  }

  @Override
  public void compareEntities(
      McpResource expected, McpResource updated, Map<String, String> authHeaders) {
    assertEquals(expected.getName(), updated.getName());
    assertEquals(expected.getResourceType(), updated.getResourceType());
    assertEquals(expected.getUri(), updated.getUri());
    assertEquals(expected.getMimeType(), updated.getMimeType());
  }

  @Override
  public McpResource validateGetWithDifferentFields(McpResource resource, boolean byName)
      throws HttpResponseException {
    String fields = "";
    resource =
        byName
            ? getEntityByName(resource.getFullyQualifiedName(), fields, ADMIN_AUTH_HEADERS)
            : getEntity(resource.getId(), fields, ADMIN_AUTH_HEADERS);
    assertListNull(resource.getOwners());

    fields = "owners,tags";
    resource =
        byName
            ? getEntityByName(resource.getFullyQualifiedName(), fields, ADMIN_AUTH_HEADERS)
            : getEntity(resource.getId(), fields, ADMIN_AUTH_HEADERS);
    assertListNotNull(resource.getOwners());
    assertListNotNull(resource.getTags());

    return resource;
  }

  @Override
  public void assertFieldChange(String fieldName, Object expected, Object actual) {
    if (fieldName.equals("resourceType")
        || fieldName.equals("uri")
        || fieldName.equals("mimeType")
        || fieldName.equals("size")) {
      assertEquals(expected, actual);
    } else {
      assertCommonFieldChange(fieldName, expected, actual);
    }
  }
}
