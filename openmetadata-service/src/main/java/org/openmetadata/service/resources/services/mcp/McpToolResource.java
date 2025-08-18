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
import lombok.extern.slf4j.Slf4j;
import org.openmetadata.schema.api.services.mcp.CreateMcpToolRequest;
import org.openmetadata.schema.entity.services.mcp.McpTool;
import org.openmetadata.schema.type.EntityHistory;
import org.openmetadata.schema.type.Include;
import org.openmetadata.schema.type.MetadataOperation;
import org.openmetadata.service.Entity;
import org.openmetadata.service.jdbi3.ListFilter;
import org.openmetadata.service.jdbi3.McpToolRepository;
import org.openmetadata.service.limits.Limits;
import org.openmetadata.service.resources.Collection;
import org.openmetadata.service.resources.EntityResource;
import org.openmetadata.service.security.Authorizer;
import org.openmetadata.service.util.ResultList;

@Path("/v1/mcpTools")
@Tag(name = "MCP Tools", description = "APIs related to MCP tools exposed by MCP services.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Collection(name = "mcpTools", order = 20)
@Slf4j
public class McpToolResource extends EntityResource<McpTool, McpToolRepository> {
  public static final String COLLECTION_PATH = "v1/mcpTools/";
  static final String FIELDS = "owners,tags,followers,domains,dataProducts";

  @Override
  public McpTool addHref(UriInfo uriInfo, McpTool mcpTool) {
    super.addHref(uriInfo, mcpTool);
    Entity.withHref(uriInfo, mcpTool.getService());
    return mcpTool;
  }

  public McpToolResource(Authorizer authorizer, Limits limits) {
    super(Entity.MCP_TOOL, authorizer, limits);
  }

  @Override
  protected List<MetadataOperation> getEntitySpecificOperations() {
    addViewOperation("inputSchema,outputSchema,examples", MetadataOperation.VIEW_BASIC);
    return null;
  }

  public static class McpToolList extends ResultList<McpTool> {
    /* Required for serde */
  }

  @GET
  @Operation(
      operationId = "listMcpTools",
      summary = "List MCP tools",
      description =
          "Get a list of MCP tools for a service. Use `fields` parameter to get only necessary fields.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of MCP tools",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpToolList.class)))
      })
  public ResultList<McpTool> list(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Filter MCP tools by service name",
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
              description = "Returns list of MCP tools before this cursor",
              schema = @Schema(type = "string"))
          @QueryParam("before")
          String before,
      @Parameter(
              description = "Returns list of MCP tools after this cursor",
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
      operationId = "getMcpToolById",
      summary = "Get an MCP tool by Id",
      description = "Get an MCP tool by `Id`.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The MCP tool",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpTool.class))),
        @ApiResponse(responseCode = "404", description = "MCP tool for instance {id} is not found")
      })
  public McpTool get(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP tool", schema = @Schema(type = "UUID"))
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
      operationId = "getMcpToolByFQN",
      summary = "Get an MCP tool by fully qualified name",
      description = "Get an MCP tool by `fullyQualifiedName`.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The MCP tool",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpTool.class))),
        @ApiResponse(responseCode = "404", description = "MCP tool for instance {fqn} is not found")
      })
  public McpTool getByName(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Fully qualified name of the MCP tool",
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
      operationId = "listAllMcpToolVersion",
      summary = "List MCP tool versions",
      description = "Get a list of all the versions of an MCP tool identified by `Id`",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of MCP tool versions",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = EntityHistory.class)))
      })
  public EntityHistory listVersions(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP tool", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id) {
    return super.listVersionsInternal(securityContext, id);
  }

  @GET
  @Path("/{id}/versions/{version}")
  @Operation(
      operationId = "getSpecificMcpToolVersion",
      summary = "Get a version of the MCP tool",
      description = "Get a version of the MCP tool by given `Id`",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "MCP tool",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpTool.class))),
        @ApiResponse(
            responseCode = "404",
            description = "MCP tool for instance {id} and version {version} is not found")
      })
  public McpTool getVersion(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP tool", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id,
      @Parameter(
              description = "MCP tool version number in the form `major`.`minor`",
              schema = @Schema(type = "string", example = "0.1 or 1.1"))
          @PathParam("version")
          String version) {
    return super.getVersionInternal(securityContext, id, version);
  }

  @POST
  @Operation(
      operationId = "createMcpTool",
      summary = "Create an MCP tool",
      description = "Create a new MCP tool.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The MCP tool",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpTool.class))),
        @ApiResponse(responseCode = "400", description = "Bad request")
      })
  public Response create(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Valid CreateMcpToolRequest create) {
    McpTool mcpTool = getMcpTool(create, securityContext.getUserPrincipal().getName());
    return create(uriInfo, securityContext, mcpTool);
  }

  @PATCH
  @Path("/{id}")
  @Operation(
      operationId = "patchMcpTool",
      summary = "Update an MCP tool",
      description = "Update an existing MCP tool using JsonPatch.",
      externalDocs =
          @ExternalDocumentation(
              description = "JsonPatch RFC",
              url = "https://tools.ietf.org/html/rfc6902"))
  @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
  public Response patch(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP tool", schema = @Schema(type = "UUID"))
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
      operationId = "createOrUpdateMcpTool",
      summary = "Create or update an MCP tool",
      description = "Create a new MCP tool, if it does not exist or update an existing MCP tool.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The MCP tool",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpTool.class))),
        @ApiResponse(responseCode = "400", description = "Bad request")
      })
  public Response createOrUpdate(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Valid CreateMcpToolRequest create) {
    McpTool mcpTool = getMcpTool(create, securityContext.getUserPrincipal().getName());
    return createOrUpdate(uriInfo, securityContext, mcpTool);
  }

  @DELETE
  @Path("/{id}")
  @Operation(
      operationId = "deleteMcpTool",
      summary = "Delete an MCP tool by Id",
      description = "Delete an MCP tool by `Id`.",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "MCP tool for instance {id} is not found")
      })
  public Response delete(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP tool", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id) {
    return delete(uriInfo, securityContext, id, false, true);
  }

  @DELETE
  @Path("/name/{fqn}")
  @Operation(
      operationId = "deleteMcpToolByFQN",
      summary = "Delete an MCP tool by fully qualified name",
      description = "Delete an MCP tool by `fullyQualifiedName`.",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "MCP tool for instance {fqn} is not found")
      })
  public Response delete(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Fully qualified name of the MCP tool",
              schema = @Schema(type = "string"))
          @PathParam("fqn")
          String fqn) {
    return deleteByName(uriInfo, securityContext, fqn, false, true);
  }

  @PUT
  @Path("/{id}/restore")
  @Operation(
      operationId = "restoreMcpTool",
      summary = "Restore a soft deleted MCP tool",
      description = "Restore a soft deleted MCP tool.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully restored the MCP tool.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpTool.class)))
      })
  public Response restoreMcpTool(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP tool", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id) {
    return restoreEntity(uriInfo, securityContext, id);
  }

  private McpTool getMcpTool(CreateMcpToolRequest create, String user) {
    try {
      McpTool mcpTool =
          repository
              .copy(new McpTool(), create, user)
              .withService(getEntityReference(Entity.MCP_SERVICE, create.getService()))
              .withToolType(create.getToolType())
              .withCategory(create.getCategory())
              .withInputSchema(create.getInputSchema())
              .withOutputSchema(create.getOutputSchema());
      // Convert examples from API schema to entity schema if needed
      if (create.getExamples() != null) {
        // TODO: Implement conversion from API Example to Entity Example
        // mcpTool.withExamples(convertExamples(create.getExamples()));
      }
      return mcpTool;
    } catch (Exception e) {
      LOG.error("Failed to create MCP tool from request: {}", create, e);
      throw e;
    }
  }
}
