package org.openmetadata.service.resources.services.mcpservices;

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
import org.openmetadata.schema.api.data.RestoreEntity;
import org.openmetadata.schema.api.services.CreateMcpService;
import org.openmetadata.schema.api.services.McpConnection;
import org.openmetadata.schema.entity.services.McpService;
import org.openmetadata.schema.entity.services.ServiceType;
import org.openmetadata.schema.entity.services.connections.TestConnectionResult;
import org.openmetadata.schema.type.ChangeEvent;
import org.openmetadata.schema.type.EntityHistory;
import org.openmetadata.schema.type.Include;
import org.openmetadata.schema.type.MetadataOperation;
import org.openmetadata.service.Entity;
import org.openmetadata.service.jdbi3.McpServiceRepository;
import org.openmetadata.service.limits.Limits;
import org.openmetadata.service.resources.Collection;
import org.openmetadata.service.resources.services.ServiceEntityResource;
import org.openmetadata.service.security.Authorizer;
import org.openmetadata.service.security.policyevaluator.OperationContext;
import org.openmetadata.service.util.ResultList;

@Path("/v1/services/mcpServices")
@Tag(
    name = "MCP Services",
    description =
        "APIs related to MCP (Model Context Protocol) Service entities, such as Claude Desktop MCP servers.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Collection(name = "mcpServices")
public class McpServiceResource
    extends ServiceEntityResource<McpService, McpServiceRepository, McpConnection> {
  public static final String COLLECTION_PATH = "v1/services/mcpServices/";
  public static final String FIELDS =
      "pipelines,owners,tags,domains,dataProducts,followers,availableTools,availableResources,availablePrompts,serverInstructions";

  public McpServiceResource(Authorizer authorizer, Limits limits) {
    super(Entity.MCP_SERVICE, authorizer, limits, ServiceType.MCP);
  }

  @Override
  protected List<MetadataOperation> getEntitySpecificOperations() {
    addViewOperation("pipelines", MetadataOperation.VIEW_BASIC);
    return null;
  }

  public static class McpServiceList extends ResultList<McpService> {
    /* Required for serde */
  }

  @GET
  @Operation(
      operationId = "listMcpServices",
      summary = "List MCP services",
      description =
          "Get a list of MCP services. Use `fields` parameter to get only necessary fields.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of MCP services",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpServiceList.class)))
      })
  public ResultList<McpService> list(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Fields requested in the returned resource",
              schema = @Schema(type = "string", example = FIELDS))
          @QueryParam("fields")
          String fieldsParam,
      @Parameter(
              description = "Filter services by domain",
              schema = @Schema(type = "string", example = "Engineering"))
          @QueryParam("domain")
          String domain,
      @DefaultValue("10") @Min(0) @Max(1000000) @QueryParam("limit") int limitParam,
      @Parameter(
              description = "Returns list of MCP services before this cursor",
              schema = @Schema(type = "string"))
          @QueryParam("before")
          String before,
      @Parameter(
              description = "Returns list of MCP services after this cursor",
              schema = @Schema(type = "string"))
          @QueryParam("after")
          String after,
      @Parameter(
              description = "Include all, deleted, or non-deleted entities.",
              schema = @Schema(implementation = Include.class))
          @QueryParam("include")
          @DefaultValue("non-deleted")
          Include include) {
    return listInternal(
        uriInfo, securityContext, fieldsParam, include, domain, limitParam, before, after);
  }

  @GET
  @Path("/{id}")
  @Operation(
      operationId = "getMcpServiceById",
      summary = "Get an MCP service by id",
      description = "Get an MCP service by `id`.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The MCP service",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpService.class))),
        @ApiResponse(
            responseCode = "404",
            description = "MCP service for instance {id} is not found")
      })
  public McpService get(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP service", schema = @Schema(type = "UUID"))
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
      operationId = "getMcpServiceByFQN",
      summary = "Get an MCP service by fully qualified name",
      description = "Get an MCP service by `fullyQualifiedName`.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The MCP service",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpService.class))),
        @ApiResponse(
            responseCode = "404",
            description = "MCP service for instance {fqn} is not found")
      })
  public McpService getByName(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Fully qualified name of the MCP service",
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

  @POST
  @Operation(
      operationId = "createMcpService",
      summary = "Create an MCP service",
      description = "Create a new MCP service.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The MCP service",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpService.class))),
        @ApiResponse(responseCode = "400", description = "Bad request")
      })
  public Response create(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @RequestBody(
              description = "Create a new MCP service",
              content =
                  @Content(
                      mediaType = "application/json",
                      examples = {
                        @ExampleObject(
                            name = "mcpService",
                            value =
                                "{\n"
                                    + "  \"name\": \"myMcpServer\",\n"
                                    + "  \"displayName\": \"My MCP Server\",\n"
                                    + "  \"description\": \"An MCP server for custom tools\",\n"
                                    + "  \"serviceType\": \"Mcp\",\n"
                                    + "  \"connection\": {\n"
                                    + "    \"config\": {\n"
                                    + "      \"type\": \"Mcp\",\n"
                                    + "      \"serverUrl\": \"mcp://localhost:3000\"\n"
                                    + "    }\n"
                                    + "  }\n"
                                    + "}")
                      }))
          @Valid
          CreateMcpService create) {
    McpService service = getService(create, securityContext.getUserPrincipal().getName());
    return create(uriInfo, securityContext, service);
  }

  @PATCH
  @Path("/{id}")
  @Operation(
      operationId = "patchMcpService",
      summary = "Update an MCP service",
      description = "Update an existing MCP service using JsonPatch.",
      externalDocs =
          @ExternalDocumentation(
              description = "JsonPatch RFC",
              url = "https://tools.ietf.org/html/rfc6902"))
  @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
  public Response patch(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP service", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id,
      @RequestBody(
              description = "JsonPatch with array of operations",
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_PATCH_JSON,
                      examples = {
                        @ExampleObject(
                            name = "Update description",
                            value =
                                "[{\"op\":\"replace\",\"path\":\"/description\",\"value\":\"Updated description\"}]")
                      }))
          JsonPatch patch) {
    return patchInternal(uriInfo, securityContext, id, patch);
  }

  @PUT
  @Operation(
      operationId = "createOrUpdateMcpService",
      summary = "Update MCP service",
      description = "Create or update an MCP service.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The updated MCP service",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpService.class)))
      })
  public Response createOrUpdate(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @RequestBody(description = "Create or update MCP service", required = true) @Valid
          CreateMcpService create) {
    McpService service = getService(create, securityContext.getUserPrincipal().getName());
    return createOrUpdate(uriInfo, securityContext, service);
  }

  @DELETE
  @Path("/{id}")
  @Operation(
      operationId = "deleteMcpService",
      summary = "Delete an MCP service by id",
      description = "Delete an MCP service by `id`.",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "MCP service for instance {id} is not found")
      })
  public Response delete(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Recursively delete this entity and it's children. (Default `false`)")
          @DefaultValue("false")
          @QueryParam("recursive")
          boolean recursive,
      @Parameter(description = "Hard delete the entity. (Default = `false`)")
          @QueryParam("hardDelete")
          @DefaultValue("false")
          boolean hardDelete,
      @Parameter(description = "Id of the MCP service", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id) {
    return delete(uriInfo, securityContext, id, recursive, hardDelete);
  }

  @DELETE
  @Path("/name/{fqn}")
  @Operation(
      operationId = "deleteMcpServiceByFQN",
      summary = "Delete an MCP service by fully qualified name",
      description = "Delete an MCP service by `fullyQualifiedName`.",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "MCP service for instance {fqn} is not found")
      })
  public Response delete(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Recursively delete this entity and it's children. (Default `false`)")
          @DefaultValue("false")
          @QueryParam("recursive")
          boolean recursive,
      @Parameter(description = "Hard delete the entity. (Default = `false`)")
          @QueryParam("hardDelete")
          @DefaultValue("false")
          boolean hardDelete,
      @Parameter(
              description = "Fully qualified name of the MCP service",
              schema = @Schema(type = "string"))
          @PathParam("fqn")
          String fqn) {
    return deleteByName(uriInfo, securityContext, fqn, recursive, hardDelete);
  }

  @DELETE
  @Path("/async/{id}")
  @Operation(
      operationId = "deleteMcpServiceAsync",
      summary = "Asynchronously delete an MCP service by Id",
      description =
          "Asynchronously delete an MCP service. If entities belong to the service, it can't be deleted.",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "MCP service for instance {id} is not found")
      })
  public Response deleteByIdAsync(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Recursively delete this entity and it's children. (Default `false`)")
          @DefaultValue("false")
          @QueryParam("recursive")
          boolean recursive,
      @Parameter(description = "Hard delete the entity. (Default = `false`)")
          @QueryParam("hardDelete")
          @DefaultValue("false")
          boolean hardDelete,
      @Parameter(description = "Id of the MCP service", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id) {
    return deleteByIdAsync(uriInfo, securityContext, id, recursive, hardDelete);
  }

  @GET
  @Path("/{id}/versions")
  @Operation(
      operationId = "listMcpServiceVersions",
      summary = "List MCP service versions",
      description = "Get a list of all the versions of an MCP service identified by `id`",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of MCP service versions",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = EntityHistory.class)))
      })
  public EntityHistory listVersions(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP service", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id) {
    return super.listVersionsInternal(securityContext, id);
  }

  @GET
  @Path("/{id}/versions/{version}")
  @Operation(
      operationId = "getMcpServiceVersionById",
      summary = "Get an MCP service version",
      description = "Get an MCP service version by `id`.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "MCP service",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpService.class))),
        @ApiResponse(
            responseCode = "404",
            description = "MCP service for instance {id} and version {version} is not found")
      })
  public McpService getVersion(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP service", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id,
      @Parameter(
              description = "MCP service version number in the form `major`.`minor`",
              schema = @Schema(type = "string", example = "0.1 or 1.1"))
          @PathParam("version")
          String version) {
    return super.getVersionInternal(securityContext, id, version);
  }

  @PUT
  @Path("/{id}/followers")
  @Operation(
      operationId = "addFollowerToMcpService",
      summary = "Add a follower",
      description = "Add a user identified by `userId` as follower of this MCP service",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "OK",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ChangeEvent.class))),
        @ApiResponse(
            responseCode = "404",
            description = "MCP service for instance {id} is not found")
      })
  public Response addFollower(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP service", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id,
      @Parameter(
              description = "Id of the user to be added as follower",
              schema = @Schema(type = "UUID"))
          UUID userId) {
    return repository
        .addFollower(securityContext.getUserPrincipal().getName(), id, userId)
        .toResponse();
  }

  @DELETE
  @Path("/{id}/followers/{userId}")
  @Operation(
      operationId = "deleteFollowerFromMcpService",
      summary = "Remove a follower",
      description = "Remove the user identified `userId` as a follower of the MCP service.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "OK",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ChangeEvent.class))),
      })
  public Response deleteFollower(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP service", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id,
      @Parameter(
              description = "Id of the user being removed as follower",
              schema = @Schema(type = "UUID"))
          @PathParam("userId")
          UUID userId) {
    return repository
        .deleteFollower(securityContext.getUserPrincipal().getName(), id, userId)
        .toResponse();
  }

  @PUT
  @Path("/{id}/testConnectionResult")
  @Operation(
      operationId = "addTestConnectionResultToMcpService",
      summary = "Add test connection result",
      description = "Add test connection result to the MCP service.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully updated the MCP service",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpService.class)))
      })
  public McpService addTestConnectionResult(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the MCP service", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id,
      @Valid TestConnectionResult testConnectionResult) {
    OperationContext operationContext = new OperationContext(entityType, MetadataOperation.CREATE);
    authorizer.authorize(securityContext, operationContext, getResourceContextById(id));
    McpService service = repository.addTestConnectionResult(id, testConnectionResult);
    return decryptOrNullify(securityContext, service);
  }

  @PUT
  @Path("/restore")
  @Operation(
      operationId = "restoreMcpService",
      summary = "Restore a soft deleted MCP service",
      description = "Restore a soft deleted MCP service.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully restored the MCP service.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = McpService.class)))
      })
  public Response restoreMcpService(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Valid RestoreEntity restore) {
    return restoreEntity(uriInfo, securityContext, restore.getId());
  }

  private McpService getService(CreateMcpService create, String user) {
    return getService(new McpServiceMapper().createToEntity(create, user), create);
  }

  private McpService getService(McpService service, CreateMcpService create) {
    service.setConnection(create.getConnection());
    service.setServerInstructions(create.getServerInstructions());
    return service;
  }

  @Override
  protected McpService nullifyConnection(McpService service) {
    service.withConnection(null);
    return service;
  }

  @Override
  protected String extractServiceType(McpService service) {
    return service.getServiceType().value();
  }
}
