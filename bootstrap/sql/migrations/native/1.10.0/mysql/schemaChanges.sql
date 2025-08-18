-- create MCP service entity
CREATE TABLE IF NOT EXISTS mcp_service_entity (
    id VARCHAR(36) GENERATED ALWAYS AS (json ->> '$.id') STORED NOT NULL,
    nameHash VARCHAR(256)  NOT NULL COLLATE ascii_bin,
    name VARCHAR(256) GENERATED ALWAYS AS (json ->> '$.name') NOT NULL,
    serviceType VARCHAR(256) GENERATED ALWAYS AS (json ->> '$.serviceType') NOT NULL,
    json JSON NOT NULL,
    updatedAt BIGINT UNSIGNED GENERATED ALWAYS AS (json ->> '$.updatedAt') NOT NULL,
    updatedBy VARCHAR(256) GENERATED ALWAYS AS (json ->> '$.updatedBy') NOT NULL,
    deleted BOOLEAN GENERATED ALWAYS AS (json -> '$.deleted'),
    PRIMARY KEY (id),
    UNIQUE (nameHash),
    INDEX (name),
    INDEX (deleted)
);

-- Add index for deleted and name combination
CREATE INDEX idx_mcp_service_entity_deleted_name ON mcp_service_entity(deleted, name);

-- create MCP Tool entity
CREATE TABLE IF NOT EXISTS mcp_tool_entity (
    id VARCHAR(36) GENERATED ALWAYS AS (json ->> '$.id') STORED NOT NULL,
    nameHash VARCHAR(256)  NOT NULL COLLATE ascii_bin,
    name VARCHAR(256) GENERATED ALWAYS AS (json ->> '$.name') NOT NULL,
    service VARCHAR(36) GENERATED ALWAYS AS (json -> '$.service.id') NOT NULL,
    json JSON NOT NULL,
    updatedAt BIGINT UNSIGNED GENERATED ALWAYS AS (json ->> '$.updatedAt') NOT NULL,
    updatedBy VARCHAR(256) GENERATED ALWAYS AS (json ->> '$.updatedBy') NOT NULL,
    deleted BOOLEAN GENERATED ALWAYS AS (json -> '$.deleted'),
    PRIMARY KEY (id),
    UNIQUE (nameHash),
    INDEX (service),
    INDEX (deleted),
    FOREIGN KEY (service) REFERENCES mcp_service_entity(id) ON DELETE CASCADE
);

-- Add index for deleted and service combination
CREATE INDEX idx_mcp_tool_entity_deleted_service ON mcp_tool_entity(deleted, service);

-- create MCP Resource entity
CREATE TABLE IF NOT EXISTS mcp_resource_entity (
    id VARCHAR(36) GENERATED ALWAYS AS (json ->> '$.id') STORED NOT NULL,
    nameHash VARCHAR(256)  NOT NULL COLLATE ascii_bin,
    name VARCHAR(256) GENERATED ALWAYS AS (json ->> '$.name') NOT NULL,
    uri VARCHAR(512) GENERATED ALWAYS AS (json ->> '$.uri') NOT NULL,
    service VARCHAR(36) GENERATED ALWAYS AS (json -> '$.service.id') NOT NULL,
    json JSON NOT NULL,
    updatedAt BIGINT UNSIGNED GENERATED ALWAYS AS (json ->> '$.updatedAt') NOT NULL,
    updatedBy VARCHAR(256) GENERATED ALWAYS AS (json ->> '$.updatedBy') NOT NULL,
    deleted BOOLEAN GENERATED ALWAYS AS (json -> '$.deleted'),
    PRIMARY KEY (id),
    UNIQUE (nameHash),
    INDEX (service),
    INDEX (deleted),
    FOREIGN KEY (service) REFERENCES mcp_service_entity(id) ON DELETE CASCADE
);

-- Add index for deleted and service combination
CREATE INDEX idx_mcp_resource_entity_deleted_service ON mcp_resource_entity(deleted, service);

-- create MCP Prompt entity
CREATE TABLE IF NOT EXISTS mcp_prompt_entity (
    id VARCHAR(36) GENERATED ALWAYS AS (json ->> '$.id') STORED NOT NULL,
    nameHash VARCHAR(256)  NOT NULL COLLATE ascii_bin,
    name VARCHAR(256) GENERATED ALWAYS AS (json ->> '$.name') NOT NULL,
    service VARCHAR(36) GENERATED ALWAYS AS (json -> '$.service.id') NOT NULL,
    json JSON NOT NULL,
    updatedAt BIGINT UNSIGNED GENERATED ALWAYS AS (json ->> '$.updatedAt') NOT NULL,
    updatedBy VARCHAR(256) GENERATED ALWAYS AS (json ->> '$.updatedBy') NOT NULL,
    deleted BOOLEAN GENERATED ALWAYS AS (json -> '$.deleted'),
    PRIMARY KEY (id),
    UNIQUE (nameHash),
    INDEX (service),
    INDEX (deleted),
    FOREIGN KEY (service) REFERENCES mcp_service_entity(id) ON DELETE CASCADE
);

-- Add index for deleted and service combination
CREATE INDEX idx_mcp_prompt_entity_deleted_service ON mcp_prompt_entity(deleted, service);