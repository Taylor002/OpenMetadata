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
import org.openmetadata.schema.api.services.mcp.CreateMcpResourceRequest;
import org.openmetadata.schema.entity.services.mcp.McpResource;
import org.openmetadata.schema.type.EntityHistory;
import org.openmetadata.schema.type.Include;
import org.openmetadata.schema.type.MetadataOperation;
import org.openmetadata.service.Entity;
import org.openmetadata.service.jdbi3.ListFilter;
import org.openmetadata.service.jdbi3.McpResourceRepository;
import org.openmetadata.service.limits.Limits;
import org.openmetadata.service.resources.Collection;
import org.openmetadata.service.resources.EntityResource;
import org.openmetadata.service.security.Authorizer;
import org.openmetadata.service.util.ResultList;

@Path("/v1/mcpResources")
@Tag(name = "MCP Resources", description = "APIs related to MCP resources exposed by MCP services.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Collection(name = "mcpResources", order = 20)
public class McpResourceResource extends EntityResource<McpResource, McpResourceRepository> {
  public static final String COLLECTION_PATH = "v1/mcpResources/";
  static final String FIELDS = "owners,tags,followers,domains,dataProducts";

  @Override
  public McpResource addHref(UriInfo uriInfo, McpResource mcpResource) {
    super.addHref(uriInfo, mcpResource);
    Entity.withHref(uriInfo, mcpResource.getService());
    return mcpResource;
  }

  public McpResourceResource(Authorizer authorizer, Limits limits) {
    super(Entity.MCP_RESOURCE, authorizer, limits);
  }

  @Override
  protected List<MetadataOperation> getEntitySpecificOperations() {
    addViewOperation("uri,mimeType,examples", MetadataOperation.VIEW_BASIC);
    return null;
  }

  public static class McpResourceList extends ResultList<McpResource> {
    /* Required for serde */
  }

  @GET
  @Operation(
      operationId = "listMcpResources",
      summary = "List MCP resources",
      description =
          "Get a list of MCP resources for a service. Use `fields` parameter to get only necessary fields.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of MCP resources",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpResourceList.class)))
      })
  public ResultList<McpResource> list(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Filter MCP resources by service name",
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
              description = "Returns list of MCP resources before this cursor",
              schema = @Schema(type = "string"))
          @QueryParam("before")
          String before,
      @Parameter(
              description = "Returns list of MCP resources after this cursor",
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
      operationId = "getMcpResourceById",
      summary = "Get an MCP resource by Id",
      description = "Get an MCP resource by `Id`.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The MCP resource",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpResource.class))),
        @ApiResponse(
            responseCode = "404",
            description = "MCP resource for instance {id} is not found")
      })
  public McpResource get(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP resource", schema = @Schema(type = "UUID"))
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
      operationId = "getMcpResourceByFQN",
      summary = "Get an MCP resource by fully qualified name",
      description = "Get an MCP resource by `fullyQualifiedName`.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The MCP resource",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpResource.class))),
        @ApiResponse(
            responseCode = "404",
            description = "MCP resource for instance {fqn} is not found")
      })
  public McpResource getByName(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Fully qualified name of the MCP resource",
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
      operationId = "listAllMcpResourceVersion",
      summary = "List MCP resource versions",
      description = "Get a list of all the versions of an MCP resource identified by `Id`",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of MCP resource versions",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = EntityHistory.class)))
      })
  public EntityHistory listVersions(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP resource", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id) {
    return super.listVersionsInternal(securityContext, id);
  }

  @GET
  @Path("/{id}/versions/{version}")
  @Operation(
      operationId = "getSpecificMcpResourceVersion",
      summary = "Get a version of the MCP resource",
      description = "Get a version of the MCP resource by given `Id`",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "MCP resource",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpResource.class))),
        @ApiResponse(
            responseCode = "404",
            description = "MCP resource for instance {id} and version {version} is not found")
      })
  public McpResource getVersion(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP resource", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id,
      @Parameter(
              description = "MCP resource version number in the form `major`.`minor`",
              schema = @Schema(type = "string", example = "0.1 or 1.1"))
          @PathParam("version")
          String version) {
    return super.getVersionInternal(securityContext, id, version);
  }

  @POST
  @Operation(
      operationId = "createMcpResource",
      summary = "Create an MCP resource",
      description = "Create a new MCP resource.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The MCP resource",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpResource.class))),
        @ApiResponse(responseCode = "400", description = "Bad request")
      })
  public Response create(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Valid CreateMcpResourceRequest create) {
    McpResource mcpResource = getMcpResource(create, securityContext.getUserPrincipal().getName());
    return create(uriInfo, securityContext, mcpResource);
  }

  @PATCH
  @Path("/{id}")
  @Operation(
      operationId = "patchMcpResource",
      summary = "Update an MCP resource",
      description = "Update an existing MCP resource using JsonPatch.",
      externalDocs =
          @ExternalDocumentation(
              description = "JsonPatch RFC",
              url = "https://tools.ietf.org/html/rfc6902"))
  @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
  public Response patch(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP resource", schema = @Schema(type = "UUID"))
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
      operationId = "createOrUpdateMcpResource",
      summary = "Create or update an MCP resource",
      description =
          "Create a new MCP resource, if it does not exist or update an existing MCP resource.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The MCP resource",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpResource.class))),
        @ApiResponse(responseCode = "400", description = "Bad request")
      })
  public Response createOrUpdate(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Valid CreateMcpResourceRequest create) {
    McpResource mcpResource = getMcpResource(create, securityContext.getUserPrincipal().getName());
    return createOrUpdate(uriInfo, securityContext, mcpResource);
  }

  @DELETE
  @Path("/{id}")
  @Operation(
      operationId = "deleteMcpResource",
      summary = "Delete an MCP resource by Id",
      description = "Delete an MCP resource by `Id`.",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "MCP resource for instance {id} is not found")
      })
  public Response delete(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP resource", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id) {
    return delete(uriInfo, securityContext, id, false, true);
  }

  @DELETE
  @Path("/name/{fqn}")
  @Operation(
      operationId = "deleteMcpResourceByFQN",
      summary = "Delete an MCP resource by fully qualified name",
      description = "Delete an MCP resource by `fullyQualifiedName`.",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "MCP resource for instance {fqn} is not found")
      })
  public Response delete(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Fully qualified name of the MCP resource",
              schema = @Schema(type = "string"))
          @PathParam("fqn")
          String fqn) {
    return deleteByName(uriInfo, securityContext, fqn, false, true);
  }

  @PUT
  @Path("/{id}/restore")
  @Operation(
      operationId = "restoreMcpResource",
      summary = "Restore a soft deleted MCP resource",
      description = "Restore a soft deleted MCP resource.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully restored the MCP resource.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpResource.class)))
      })
  public Response restoreMcpResource(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP resource", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id) {
    return restoreEntity(uriInfo, securityContext, id);
  }

  private McpResource getMcpResource(CreateMcpResourceRequest create, String user) {
    McpResource mcpResource =
        repository
            .copy(new McpResource(), create, user)
            .withService(getEntityReference(Entity.MCP_SERVICE, create.getService()))
            .withResourceType(create.getResourceType())
            .withUri(create.getUri())
            .withMimeType(create.getMimeType())
            .withSize(create.getSize())
            .withLastModified(create.getLastModified());
    // Convert metadata from API schema to entity schema if needed
    if (create.getMetadata() != null) {
      // TODO: Implement conversion from API Metadata to Entity Metadata
      // mcpResource.withMetadata(convertMetadata(create.getMetadata()));
    }
    return mcpResource;
  }
}
