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

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.json.JsonPatch;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.UUID;
import org.openmetadata.schema.api.services.mcp.CreateMcpPromptRequest;
import org.openmetadata.schema.entity.services.mcp.McpPrompt;
import org.openmetadata.schema.type.EntityHistory;
import org.openmetadata.schema.type.Include;
import org.openmetadata.schema.type.MetadataOperation;
import org.openmetadata.service.Entity;
import org.openmetadata.service.jdbi3.ListFilter;
import org.openmetadata.service.jdbi3.McpPromptRepository;
import org.openmetadata.service.limits.Limits;
import org.openmetadata.service.resources.Collection;
import org.openmetadata.service.resources.EntityResource;
import org.openmetadata.service.security.Authorizer;
import org.openmetadata.service.util.ResultList;

@Path("/v1/mcpPrompts")
@Tag(name = "MCP Prompts", description = "APIs related to MCP prompts exposed by MCP services.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Collection(name = "mcpPrompts", order = 20)
public class McpPromptResource extends EntityResource<McpPrompt, McpPromptRepository> {
  public static final String COLLECTION_PATH = "v1/mcpPrompts/";
  static final String FIELDS = "owners,tags,followers,domains,dataProducts";

  @Override
  public McpPrompt addHref(UriInfo uriInfo, McpPrompt mcpPrompt) {
    super.addHref(uriInfo, mcpPrompt);
    Entity.withHref(uriInfo, mcpPrompt.getService());
    return mcpPrompt;
  }

  public McpPromptResource(Authorizer authorizer, Limits limits) {
    super(Entity.MCP_PROMPT, authorizer, limits);
  }

  @Override
  protected List<MetadataOperation> getEntitySpecificOperations() {
    addViewOperation("template,arguments,examples", MetadataOperation.VIEW_BASIC);
    return null;
  }

  public static class McpPromptList extends ResultList<McpPrompt> {
    /* Required for serde */
  }

  @GET
  @Operation(
      operationId = "listMcpPrompts",
      summary = "List MCP prompts",
      description =
          "Get a list of MCP prompts for a service. Use `fields` parameter to get only necessary fields.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of MCP prompts",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpPromptList.class)))
      })
  public ResultList<McpPrompt> list(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Filter MCP prompts by service name",
              schema = @Schema(type = "string", example = "MCP Service"))
          @QueryParam("service")
          String serviceParam,
      @Parameter(
              description = "Fields requested in the returned resource",
              schema = @Schema(type = "string", example = FIELDS))
          @QueryParam("fields")
          String fieldsParam,
      @Parameter(description = "Limit the number of results. (1 to 1000000, default = 10)")
          @DefaultValue("10")
          @QueryParam("limit")
          @Min(0)
          @Max(1000000)
          int limitParam,
      @Parameter(
              description = "Returns list of MCP prompts before this cursor",
              schema = @Schema(type = "string"))
          @QueryParam("before")
          String before,
      @Parameter(
              description = "Returns list of MCP prompts after this cursor",
              schema = @Schema(type = "string"))
          @QueryParam("after")
          String after,
      @Parameter(
              description = "Include all, deleted, or non-deleted entities.",
              schema = @Schema(implementation = Include.class))
          @QueryParam("include")
          @DefaultValue("non-deleted")
          Include include) {
    ListFilter filter = new ListFilter(include);
    filter = serviceParam != null ? filter.addQueryParam("service", serviceParam) : filter;
    return super.listInternal(
        uriInfo, securityContext, fieldsParam, filter, limitParam, before, after);
  }

  @GET
  @Path("/{id}")
  @Operation(
      operationId = "getMcpPromptById",
      summary = "Get an MCP prompt by Id",
      description = "Get an MCP prompt by `Id`.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The MCP prompt",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpPrompt.class))),
        @ApiResponse(
            responseCode = "404",
            description = "MCP prompt for instance {id} is not found")
      })
  public McpPrompt get(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP prompt", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id,
      @Parameter(
              description = "Fields requested in the returned resource",
              schema = @Schema(type = "string", example = FIELDS))
          @QueryParam("fields")
          String fieldsParam,
      @Parameter(
              description = "Include all, deleted, or non-deleted entities.",
              schema = @Schema(implementation = Include.class))
          @QueryParam("include")
          @DefaultValue("non-deleted")
          Include include) {
    return getInternal(uriInfo, securityContext, id, fieldsParam, include);
  }

  @GET
  @Path("/name/{fqn}")
  @Operation(
      operationId = "getMcpPromptByFQN",
      summary = "Get an MCP prompt by fully qualified name",
      description = "Get an MCP prompt by `fullyQualifiedName`.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The MCP prompt",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpPrompt.class))),
        @ApiResponse(
            responseCode = "404",
            description = "MCP prompt for instance {fqn} is not found")
      })
  public McpPrompt getByName(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Fully qualified name of the MCP prompt",
              schema = @Schema(type = "string"))
          @PathParam("fqn")
          String fqn,
      @Parameter(
              description = "Fields requested in the returned resource",
              schema = @Schema(type = "string", example = FIELDS))
          @QueryParam("fields")
          String fieldsParam,
      @Parameter(
              description = "Include all, deleted, or non-deleted entities.",
              schema = @Schema(implementation = Include.class))
          @QueryParam("include")
          @DefaultValue("non-deleted")
          Include include) {
    return getByNameInternal(uriInfo, securityContext, fqn, fieldsParam, include);
  }

  @GET
  @Path("/{id}/versions")
  @Operation(
      operationId = "listAllMcpPromptVersion",
      summary = "List MCP prompt versions",
      description = "Get a list of all the versions of an MCP prompt identified by `Id`",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of MCP prompt versions",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = EntityHistory.class)))
      })
  public EntityHistory listVersions(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP prompt", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id) {
    return super.listVersionsInternal(securityContext, id);
  }

  @GET
  @Path("/{id}/versions/{version}")
  @Operation(
      operationId = "getSpecificMcpPromptVersion",
      summary = "Get a version of the MCP prompt",
      description = "Get a version of the MCP prompt by given `Id`",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "MCP prompt",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpPrompt.class))),
        @ApiResponse(
            responseCode = "404",
            description = "MCP prompt for instance {id} and version {version} is not found")
      })
  public McpPrompt getVersion(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP prompt", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id,
      @Parameter(
              description = "MCP prompt version number in the form `major`.`minor`",
              schema = @Schema(type = "string", example = "0.1 or 1.1"))
          @PathParam("version")
          String version) {
    return super.getVersionInternal(securityContext, id, version);
  }

  @POST
  @Operation(
      operationId = "createMcpPrompt",
      summary = "Create an MCP prompt",
      description = "Create a new MCP prompt.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The MCP prompt",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpPrompt.class))),
        @ApiResponse(responseCode = "400", description = "Bad request")
      })
  public Response create(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Valid CreateMcpPromptRequest create) {
    McpPrompt mcpPrompt = getMcpPrompt(create, securityContext.getUserPrincipal().getName());
    return create(uriInfo, securityContext, mcpPrompt);
  }

  @PATCH
  @Path("/{id}")
  @Operation(
      operationId = "patchMcpPrompt",
      summary = "Update an MCP prompt",
      description = "Update an existing MCP prompt using JsonPatch.",
      externalDocs =
          @ExternalDocumentation(
              description = "JsonPatch RFC",
              url = "https://tools.ietf.org/html/rfc6902"))
  @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
  public Response patch(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP prompt", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id,
      @RequestBody(
              description = "JsonPatch with array of operations",
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_PATCH_JSON,
                      examples = {
                        @ExampleObject("[{op:remove, path:/a},{op:add, path: /b, value: val}]")
                      }))
          JsonPatch patch) {
    return patchInternal(uriInfo, securityContext, id, patch);
  }

  @PUT
  @Operation(
      operationId = "createOrUpdateMcpPrompt",
      summary = "Create or update an MCP prompt",
      description =
          "Create a new MCP prompt, if it does not exist or update an existing MCP prompt.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The MCP prompt",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpPrompt.class))),
        @ApiResponse(responseCode = "400", description = "Bad request")
      })
  public Response createOrUpdate(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Valid CreateMcpPromptRequest create) {
    McpPrompt mcpPrompt = getMcpPrompt(create, securityContext.getUserPrincipal().getName());
    return createOrUpdate(uriInfo, securityContext, mcpPrompt);
  }

  @DELETE
  @Path("/{id}")
  @Operation(
      operationId = "deleteMcpPrompt",
      summary = "Delete an MCP prompt by Id",
      description = "Delete an MCP prompt by `Id`.",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "MCP prompt for instance {id} is not found")
      })
  public Response delete(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP prompt", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id) {
    return delete(uriInfo, securityContext, id, false, true);
  }

  @DELETE
  @Path("/name/{fqn}")
  @Operation(
      operationId = "deleteMcpPromptByFQN",
      summary = "Delete an MCP prompt by fully qualified name",
      description = "Delete an MCP prompt by `fullyQualifiedName`.",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "MCP prompt for instance {fqn} is not found")
      })
  public Response delete(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Fully qualified name of the MCP prompt",
              schema = @Schema(type = "string"))
          @PathParam("fqn")
          String fqn) {
    return deleteByName(uriInfo, securityContext, fqn, false, true);
  }

  @PUT
  @Path("/{id}/restore")
  @Operation(
      operationId = "restoreMcpPrompt",
      summary = "Restore a soft deleted MCP prompt",
      description = "Restore a soft deleted MCP prompt.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully restored the MCP prompt.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpPrompt.class)))
      })
  public Response restoreMcpPrompt(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP prompt", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id) {
    return restoreEntity(uriInfo, securityContext, id);
  }

  private McpPrompt getMcpPrompt(CreateMcpPromptRequest create, String user) {
    McpPrompt mcpPrompt =
        repository
            .copy(new McpPrompt(), create, user)
            .withService(getEntityReference(Entity.MCP_SERVICE, create.getService()))
            .withPromptType(create.getPromptType())
            .withTemplate(create.getTemplate())
            .withArguments(create.getArguments());
    // Convert examples from API schema to entity schema if needed
    if (create.getExamples() != null) {
      // TODO: Implement conversion from API Example to Entity Example
      // mcpPrompt.withExamples(convertExamples(create.getExamples()));
    }
    return mcpPrompt;
  }
}
