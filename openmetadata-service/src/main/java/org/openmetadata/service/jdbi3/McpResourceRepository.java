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
import org.openmetadata.schema.entity.services.mcp.McpResource;
import org.openmetadata.schema.type.EntityReference;
import org.openmetadata.schema.type.Include;
import org.openmetadata.schema.type.Relationship;
import org.openmetadata.service.Entity;
import org.openmetadata.service.resources.services.mcp.McpResourceResource;
import org.openmetadata.service.util.EntityUtil;
import org.openmetadata.service.util.FullyQualifiedName;

@Repository
public class McpResourceRepository extends EntityRepository<McpResource> {
  private static final String MCP_RESOURCE_UPDATE_FIELDS = "owners,tags,domains,dataProducts";
  private static final String MCP_RESOURCE_PATCH_FIELDS = "owners,tags,domains,dataProducts";

  public McpResourceRepository() {
    super(
        McpResourceResource.COLLECTION_PATH,
        Entity.MCP_RESOURCE,
        McpResource.class,
        Entity.getCollectionDAO().mcpResourceDAO(),
        MCP_RESOURCE_PATCH_FIELDS,
        MCP_RESOURCE_UPDATE_FIELDS);
    supportsSearch = true;
  }

  @Override
  public void setFullyQualifiedName(McpResource mcpResource) {
    EntityReference service = mcpResource.getService();
    mcpResource.setFullyQualifiedName(
        FullyQualifiedName.add(service.getFullyQualifiedName(), mcpResource.getName()));
  }

  @Override
  public void prepare(McpResource mcpResource, boolean update) {
    McpService mcpService = Entity.getEntity(mcpResource.getService(), "", Include.ALL);
    mcpResource.setService(mcpService.getEntityReference());
    mcpResource.setServiceType(mcpService.getServiceType());
  }

  @Override
  public void storeEntity(McpResource mcpResource, boolean update) {
    // Relationships and fields specific to MCP resource
    EntityReference service = mcpResource.getService();
    mcpResource.withService(service);
    store(mcpResource, update);
  }

  @Override
  public void storeRelationships(McpResource mcpResource) {
    EntityReference service = mcpResource.getService();
    addRelationship(
        service.getId(),
        mcpResource.getId(),
        service.getType(),
        Entity.MCP_RESOURCE,
        Relationship.CONTAINS);
  }

  @Override
  public EntityUpdater getUpdater(McpResource original, McpResource updated, Operation operation) {
    return new McpResourceUpdater(original, updated, operation);
  }

  @Override
  public void setFields(McpResource mcpResource, EntityUtil.Fields fields) {
    /* Set fields for the entity */
  }

  @Override
  public void clearFields(McpResource mcpResource, EntityUtil.Fields fields) {
    /* Remove fields that are not requested */
  }

  public class McpResourceUpdater extends EntityUpdater {
    public McpResourceUpdater(McpResource original, McpResource updated, Operation operation) {
      super(original, updated, operation);
    }

    @Override
    protected void entitySpecificUpdate(boolean consolidatingChanges) {
      recordChange("resourceType", original.getResourceType(), updated.getResourceType());
      recordChange("uri", original.getUri(), updated.getUri());
      recordChange("mimeType", original.getMimeType(), updated.getMimeType());
      recordChange("size", original.getSize(), updated.getSize());
      recordChange("lastModified", original.getLastModified(), updated.getLastModified());
      recordChange("metadata", original.getMetadata(), updated.getMetadata());
    }
  }
}
