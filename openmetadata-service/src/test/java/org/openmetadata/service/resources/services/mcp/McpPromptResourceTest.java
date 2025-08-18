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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpResponseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.openmetadata.schema.api.services.CreateMcpService;
import org.openmetadata.schema.api.services.mcp.CreateMcpPromptRequest;
import org.openmetadata.schema.api.services.mcp.PromptArgument;
import org.openmetadata.schema.entity.services.McpService;
import org.openmetadata.schema.entity.services.mcp.McpPrompt;
import org.openmetadata.service.Entity;
import org.openmetadata.service.resources.EntityResourceTest;
import org.openmetadata.service.resources.services.McpServiceResourceTest;
import org.openmetadata.service.util.ResultList;

@Slf4j
class McpPromptResourceTest extends EntityResourceTest<McpPrompt, CreateMcpPromptRequest> {

  private McpService mcpService;

  public McpPromptResourceTest() {
    super(
        Entity.MCP_PROMPT,
        McpPrompt.class,
        McpPromptResource.McpPromptList.class,
        "mcpPrompts",
        McpPromptResource.FIELDS);
    this.supportsPatch = false;
  }

  @Override
  public void setup(TestInfo test) throws URISyntaxException, IOException {
    super.setup(test);
    // Create a parent MCP service for the prompts
    CreateMcpService createService =
        new McpServiceResourceTest().createRequest(test.getDisplayName());
    mcpService = new McpServiceResourceTest().createEntity(createService, ADMIN_AUTH_HEADERS);
  }

  @Test
  void post_validMcpPrompt_200_ok(TestInfo test) throws IOException {
    // Create MCP prompt with arguments
    PromptArgument arg1 =
        new PromptArgument()
            .withName("language")
            .withDescription("Programming language")
            .withType("string")
            .withRequired(true);

    PromptArgument arg2 =
        new PromptArgument()
            .withName("complexity")
            .withDescription("Code complexity level")
            .withType("string")
            .withRequired(false)
            .withDefault("medium");

    CreateMcpPromptRequest create =
        createRequest(test)
            .withPromptType(CreateMcpPromptRequest.McpPromptType.GENERATION)
            .withTemplate("Generate a ${complexity} ${language} function that ${description}")
            .withArguments(List.of(arg1, arg2));

    McpPrompt prompt = createAndCheckEntity(create, ADMIN_AUTH_HEADERS);
    assertEquals(CreateMcpPromptRequest.McpPromptType.GENERATION, prompt.getPromptType());
    assertNotNull(prompt.getTemplate());
    assertEquals(2, prompt.getArguments().size());
    assertEquals("language", prompt.getArguments().get(0).getName());
    assertEquals(true, prompt.getArguments().get(0).getRequired());
  }

  @Test
  void post_mcpPromptWithoutService_400(TestInfo test) {
    CreateMcpPromptRequest create = createRequest(test).withService(null);
    assertResponse(
        () -> createEntity(create, ADMIN_AUTH_HEADERS), BAD_REQUEST, "[service must not be null]");
  }

  @Test
  void post_mcpPromptWithoutTemplate_400(TestInfo test) {
    CreateMcpPromptRequest create = createRequest(test).withTemplate(null);
    assertResponse(
        () -> createEntity(create, ADMIN_AUTH_HEADERS), BAD_REQUEST, "[template must not be null]");
  }

  @Test
  void put_updateMcpPrompt_200(TestInfo test) throws IOException {
    CreateMcpPromptRequest create = createRequest(test).withTemplate("Initial template: ${param1}");
    McpPrompt prompt = createAndCheckEntity(create, ADMIN_AUTH_HEADERS);

    // Update prompt fields
    PromptArgument newArg =
        new PromptArgument().withName("param1").withType("string").withRequired(true);

    create
        .withPromptType(CreateMcpPromptRequest.McpPromptType.ANALYSIS)
        .withTemplate("Updated template: analyze ${param1} for ${aspect}")
        .withArguments(List.of(newArg));

    McpPrompt updated = updateEntity(create, OK, ADMIN_AUTH_HEADERS);
    assertEquals(CreateMcpPromptRequest.McpPromptType.ANALYSIS, updated.getPromptType());
    assertEquals("Updated template: analyze ${param1} for ${aspect}", updated.getTemplate());
    assertEquals(1, updated.getArguments().size());
  }

  @Test
  void get_mcpPromptsByService_200(TestInfo test) throws IOException {
    // Create multiple prompts for the same service
    CreateMcpPromptRequest create1 =
        createRequest(test, 1)
            .withPromptType(CreateMcpPromptRequest.McpPromptType.TRANSFORMATION)
            .withTemplate("Summarize: ${text}");
    CreateMcpPromptRequest create2 =
        createRequest(test, 2)
            .withPromptType(CreateMcpPromptRequest.McpPromptType.TRANSFORMATION)
            .withTemplate("Translate ${text} to ${targetLang}");
    CreateMcpPromptRequest create3 =
        createRequest(test, 3)
            .withPromptType(CreateMcpPromptRequest.McpPromptType.ANALYSIS)
            .withTemplate("Classify ${content} into categories");

    createEntity(create1, ADMIN_AUTH_HEADERS);
    createEntity(create2, ADMIN_AUTH_HEADERS);
    createEntity(create3, ADMIN_AUTH_HEADERS);

    // List prompts by service
    Map<String, String> queryParams = Map.of("service", mcpService.getId().toString());
    ResultList<McpPrompt> prompts = listEntities(queryParams, ADMIN_AUTH_HEADERS);

    assertEquals(3, prompts.getData().size());
  }

  @Test
  void test_mcpPromptWithComplexArguments(TestInfo test) throws IOException {
    // Test prompt with various argument types
    List<PromptArgument> args =
        List.of(
            new PromptArgument().withName("stringArg").withType("string").withRequired(true),
            new PromptArgument()
                .withName("numberArg")
                .withType("number")
                .withRequired(false)
                .withDefault("42"),
            new PromptArgument()
                .withName("booleanArg")
                .withType("boolean")
                .withRequired(false)
                .withDefault("true"),
            new PromptArgument().withName("arrayArg").withType("array").withRequired(false));

    CreateMcpPromptRequest create =
        createRequest(test)
            .withPromptType(CreateMcpPromptRequest.McpPromptType.CUSTOM)
            .withTemplate("Complex prompt with multiple arg types")
            .withArguments(args);

    McpPrompt prompt = createAndCheckEntity(create, ADMIN_AUTH_HEADERS);
    assertEquals(4, prompt.getArguments().size());

    // Verify each argument
    assertEquals("string", prompt.getArguments().get(0).getType());
    assertEquals("number", prompt.getArguments().get(1).getType());
    assertEquals("42", prompt.getArguments().get(1).getDefault());
    assertEquals("boolean", prompt.getArguments().get(2).getType());
    assertEquals("array", prompt.getArguments().get(3).getType());
  }

  @Override
  public CreateMcpPromptRequest createRequest(String name) {
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
    return new CreateMcpPromptRequest()
        .withName(name)
        .withService(mcpService.getId().toString())
        .withDescription("Test MCP prompt")
        .withPromptType(CreateMcpPromptRequest.McpPromptType.CUSTOM)
        .withTemplate("Default template for " + name);
  }

  @Override
  public void validateCreatedEntity(
      McpPrompt prompt, CreateMcpPromptRequest request, Map<String, String> authHeaders) {
    assertEquals(request.getName(), prompt.getName());
    assertEquals(request.getDescription(), prompt.getDescription());
    assertEquals(request.getPromptType(), prompt.getPromptType());
    assertEquals(request.getTemplate(), prompt.getTemplate());

    if (request.getArguments() != null) {
      assertEquals(request.getArguments().size(), prompt.getArguments().size());
      for (int i = 0; i < request.getArguments().size(); i++) {
        PromptArgument expected = request.getArguments().get(i);
        PromptArgument actual = prompt.getArguments().get(i);
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getRequired(), actual.getRequired());
      }
    }

    assertNotNull(prompt.getService());
    assertEquals(mcpService.getId(), prompt.getService().getId());
  }

  @Override
  public void compareEntities(
      McpPrompt expected, McpPrompt updated, Map<String, String> authHeaders) {
    assertEquals(expected.getName(), updated.getName());
    assertEquals(expected.getPromptType(), updated.getPromptType());
    assertEquals(expected.getTemplate(), updated.getTemplate());
    if (expected.getArguments() != null && updated.getArguments() != null) {
      assertEquals(expected.getArguments().size(), updated.getArguments().size());
    }
  }

  @Override
  public McpPrompt validateGetWithDifferentFields(McpPrompt prompt, boolean byName)
      throws HttpResponseException {
    String fields = "";
    prompt =
        byName
            ? getEntityByName(prompt.getFullyQualifiedName(), fields, ADMIN_AUTH_HEADERS)
            : getEntity(prompt.getId(), fields, ADMIN_AUTH_HEADERS);
    assertListNull(prompt.getOwners());

    fields = "owners,tags";
    prompt =
        byName
            ? getEntityByName(prompt.getFullyQualifiedName(), fields, ADMIN_AUTH_HEADERS)
            : getEntity(prompt.getId(), fields, ADMIN_AUTH_HEADERS);
    assertListNotNull(prompt.getOwners());
    assertListNotNull(prompt.getTags());

    return prompt;
  }

  @Override
  public void assertFieldChange(String fieldName, Object expected, Object actual) {
    if (fieldName.equals("promptType") || fieldName.equals("template")) {
      assertEquals(expected, actual);
    } else if (fieldName.equals("arguments")) {
      // Compare argument lists
      List<?> expectedArgs = (List<?>) expected;
      List<?> actualArgs = (List<?>) actual;
      assertEquals(expectedArgs.size(), actualArgs.size());
    } else {
      assertCommonFieldChange(fieldName, expected, actual);
    }
  }
}
