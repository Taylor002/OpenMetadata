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

package org.openmetadata.service.jdbi3;

import org.openmetadata.schema.entity.services.McpService;
import org.openmetadata.schema.entity.services.mcp.McpTool;
import org.openmetadata.schema.type.EntityReference;
import org.openmetadata.schema.type.Include;
import org.openmetadata.schema.type.Relationship;
import org.openmetadata.service.Entity;
import org.openmetadata.service.resources.services.mcp.McpToolResource;
import org.openmetadata.service.util.EntityUtil;
import org.openmetadata.service.util.FullyQualifiedName;

@Repository
public class McpToolRepository extends EntityRepository<McpTool> {
  private static final String MCP_TOOL_UPDATE_FIELDS = "owners,tags,domains,dataProducts";
  private static final String MCP_TOOL_PATCH_FIELDS = "owners,tags,domains,dataProducts";

  public McpToolRepository() {
    super(
        McpToolResource.COLLECTION_PATH,
        Entity.MCP_TOOL,
        McpTool.class,
        Entity.getCollectionDAO().mcpToolDAO(),
        MCP_TOOL_PATCH_FIELDS,
        MCP_TOOL_UPDATE_FIELDS);
    supportsSearch = true;
  }

  @Override
  public void setFullyQualifiedName(McpTool mcpTool) {
    EntityReference service = mcpTool.getService();
    mcpTool.setFullyQualifiedName(
        FullyQualifiedName.add(service.getFullyQualifiedName(), mcpTool.getName()));
  }

  @Override
  public void prepare(McpTool mcpTool, boolean update) {
    McpService mcpService = Entity.getEntity(mcpTool.getService(), "", Include.ALL);
    mcpTool.setService(mcpService.getEntityReference());
    mcpTool.setServiceType(mcpService.getServiceType());
  }

  @Override
  public void storeEntity(McpTool mcpTool, boolean update) {
    // Relationships and fields specific to MCP tool
    EntityReference service = mcpTool.getService();
    mcpTool.withService(service);
    store(mcpTool, update);
  }

  @Override
  public void storeRelationships(McpTool mcpTool) {
    EntityReference service = mcpTool.getService();
    addRelationship(
        service.getId(),
        mcpTool.getId(),
        service.getType(),
        Entity.MCP_TOOL,
        Relationship.CONTAINS);
  }

  @Override
  public EntityUpdater getUpdater(McpTool original, McpTool updated, Operation operation) {
    return new McpToolUpdater(original, updated, operation);
  }

  @Override
  public void setFields(McpTool mcpTool, EntityUtil.Fields fields) {
    /* Set fields for the entity */
  }

  @Override
  public void clearFields(McpTool mcpTool, EntityUtil.Fields fields) {
    /* Remove fields that are not requested */
  }

  public class McpToolUpdater extends EntityUpdater {
    public McpToolUpdater(McpTool original, McpTool updated, Operation operation) {
      super(original, updated, operation);
    }

    @Override
    protected void entitySpecificUpdate(boolean consolidatingChanges) {
      recordChange("toolType", original.getToolType(), updated.getToolType());
      recordChange("category", original.getCategory(), updated.getCategory());
      recordChange("inputSchema", original.getInputSchema(), updated.getInputSchema());
      recordChange("outputSchema", original.getOutputSchema(), updated.getOutputSchema());
      recordChange("examples", original.getExamples(), updated.getExamples());
    }
  }
}
