package org.openmetadata.service.jdbi3;

import org.openmetadata.schema.api.services.McpConnection;
import org.openmetadata.schema.entity.services.McpService;
import org.openmetadata.schema.entity.services.ServiceType;
import org.openmetadata.service.Entity;
import org.openmetadata.service.resources.services.mcpservices.McpServiceResource;

public class McpServiceRepository extends ServiceEntityRepository<McpService, McpConnection> {
  public McpServiceRepository() {
    super(
        McpServiceResource.COLLECTION_PATH,
        Entity.MCP_SERVICE,
        Entity.getCollectionDAO().mcpServiceDAO(),
        McpConnection.class,
        "",
        ServiceType.MCP);
    supportsSearch = true;
  }
}
