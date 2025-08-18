package org.openmetadata.service.search.indexes;

import java.util.Map;
import org.openmetadata.schema.entity.services.McpService;
import org.openmetadata.service.Entity;

public record McpServiceIndex(McpService mcpService) implements SearchIndex {

  @Override
  public Object getEntity() {
    return mcpService;
  }

  public Map<String, Object> buildSearchIndexDocInternal(Map<String, Object> doc) {
    Map<String, Object> commonAttributes = getCommonAttributesMap(mcpService, Entity.MCP_SERVICE);
    doc.putAll(commonAttributes);
    doc.put("upstreamLineage", SearchIndex.getLineageData(mcpService.getEntityReference()));

    if (mcpService.getServerInstructions() != null) {
      doc.put("serverInstructions", mcpService.getServerInstructions());
    }

    return doc;
  }
}
