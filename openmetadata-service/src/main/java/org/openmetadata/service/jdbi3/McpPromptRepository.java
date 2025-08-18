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
import org.openmetadata.schema.entity.services.mcp.McpPrompt;
import org.openmetadata.schema.type.EntityReference;
import org.openmetadata.schema.type.Include;
import org.openmetadata.schema.type.Relationship;
import org.openmetadata.service.Entity;
import org.openmetadata.service.resources.services.mcp.McpPromptResource;
import org.openmetadata.service.util.EntityUtil;
import org.openmetadata.service.util.FullyQualifiedName;

@Repository
public class McpPromptRepository extends EntityRepository<McpPrompt> {
  private static final String MCP_PROMPT_UPDATE_FIELDS = "owners,tags,domains,dataProducts";
  private static final String MCP_PROMPT_PATCH_FIELDS = "owners,tags,domains,dataProducts";

  public McpPromptRepository() {
    super(
        McpPromptResource.COLLECTION_PATH,
        Entity.MCP_PROMPT,
        McpPrompt.class,
        Entity.getCollectionDAO().mcpPromptDAO(),
        MCP_PROMPT_PATCH_FIELDS,
        MCP_PROMPT_UPDATE_FIELDS);
    supportsSearch = true;
  }

  @Override
  public void setFullyQualifiedName(McpPrompt mcpPrompt) {
    EntityReference service = mcpPrompt.getService();
    mcpPrompt.setFullyQualifiedName(
        FullyQualifiedName.add(service.getFullyQualifiedName(), mcpPrompt.getName()));
  }

  @Override
  public void prepare(McpPrompt mcpPrompt, boolean update) {
    McpService mcpService = Entity.getEntity(mcpPrompt.getService(), "", Include.ALL);
    mcpPrompt.setService(mcpService.getEntityReference());
    mcpPrompt.setServiceType(mcpService.getServiceType());
  }

  @Override
  public void storeEntity(McpPrompt mcpPrompt, boolean update) {
    // Relationships and fields specific to MCP prompt
    EntityReference service = mcpPrompt.getService();
    mcpPrompt.withService(service);
    store(mcpPrompt, update);
  }

  @Override
  public void storeRelationships(McpPrompt mcpPrompt) {
    EntityReference service = mcpPrompt.getService();
    addRelationship(
        service.getId(),
        mcpPrompt.getId(),
        service.getType(),
        Entity.MCP_PROMPT,
        Relationship.CONTAINS);
  }

  @Override
  public EntityUpdater getUpdater(McpPrompt original, McpPrompt updated, Operation operation) {
    return new McpPromptUpdater(original, updated, operation);
  }

  @Override
  public void setFields(McpPrompt mcpPrompt, EntityUtil.Fields fields) {
    /* Set fields for the entity */
  }

  @Override
  public void clearFields(McpPrompt mcpPrompt, EntityUtil.Fields fields) {
    /* Remove fields that are not requested */
  }

  public class McpPromptUpdater extends EntityUpdater {
    public McpPromptUpdater(McpPrompt original, McpPrompt updated, Operation operation) {
      super(original, updated, operation);
    }

    @Override
    protected void entitySpecificUpdate(boolean consolidatingChanges) {
      recordChange("promptType", original.getPromptType(), updated.getPromptType());
      recordChange("template", original.getTemplate(), updated.getTemplate());
      recordChange("arguments", original.getArguments(), updated.getArguments());
      recordChange("examples", original.getExamples(), updated.getExamples());
    }
  }
}
