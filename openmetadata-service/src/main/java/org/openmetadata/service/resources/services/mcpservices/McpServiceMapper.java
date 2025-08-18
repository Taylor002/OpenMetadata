package org.openmetadata.service.resources.services.mcpservices;

import org.openmetadata.schema.api.services.CreateMcpService;
import org.openmetadata.schema.entity.services.McpService;
import org.openmetadata.service.mapper.EntityMapper;

public class McpServiceMapper implements EntityMapper<McpService, CreateMcpService> {
  @Override
  public McpService createToEntity(CreateMcpService create, String user) {
    return copy(new McpService(), create, user)
        .withServiceType(create.getServiceType())
        .withConnection(create.getConnection())
        .withServerInstructions(create.getServerInstructions());
  }
}
