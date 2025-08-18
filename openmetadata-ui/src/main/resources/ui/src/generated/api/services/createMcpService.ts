/**
 * Create MCP Service entity request
 */
export interface CreateMCPService {
    /**
     * List of prompt templates exposed by this MCP service
     */
    availablePrompts?: MCPPrompt[];
    /**
     * List of resources exposed by this MCP service
     */
    availableResources?: MCPResource[];
    /**
     * List of tools exposed by this MCP service
     */
    availableTools?: MCPTool[];
    connection:      MCPConnection;
    /**
     * List of fully qualified names of data products this entity is part of.
     */
    dataProducts?: string[];
    /**
     * Description of MCP service instance.
     */
    description?: string;
    /**
     * Display Name that identifies this MCP service.
     */
    displayName?: string;
    /**
     * Fully qualified name of the domain the MCP Service belongs to.
     */
    domain?: string;
    /**
     * Life Cycle properties of the entity
     */
    lifeCycle?: LifeCycle;
    /**
     * Name that identifies this MCP service.
     */
    name: string;
    /**
     * Owners of this MCP service.
     */
    owners?: EntityReference[];
    /**
     * Instructions or guidelines for using this MCP server
     */
    serverInstructions?: string;
    /**
     * Type of MCP service
     */
    serviceType: ServiceType;
    /**
     * Tags for this MCP service.
     */
    tags?: TagLabel[];
}

/**
 * Prompt template exposed by the MCP service
 */
export interface MCPPrompt {
    /**
     * Arguments for the prompt template
     */
    arguments?: MCPPromptArgument[];
    /**
     * Description of the prompt
     */
    description?: string;
    /**
     * Name of the prompt
     */
    name: string;
}

/**
 * Argument definition for an MCP prompt
 */
export interface MCPPromptArgument {
    /**
     * Description of the argument
     */
    description?: string;
    /**
     * Name of the argument
     */
    name: string;
    /**
     * Whether this argument is required
     */
    required?: boolean;
    /**
     * Expected type of the argument (e.g., string, number, boolean)
     */
    type?: string;
}

/**
 * Resource exposed by the MCP service
 */
export interface MCPResource {
    /**
     * Description of the resource
     */
    description?: string;
    /**
     * MIME type of the resource
     */
    mimeType?: string;
    /**
     * Name of the resource
     */
    name?: string;
    /**
     * URI of the resource
     */
    uri: string;
}

/**
 * MCP Tool exposed by the service
 */
export interface MCPTool {
    /**
     * Category or grouping for the tool
     */
    category?: string;
    /**
     * Description of what the tool does
     */
    description?: string;
    /**
     * Example usage of the tool
     */
    examples?: Example[];
    /**
     * JSON Schema defining the tool's input parameters
     */
    inputSchema?: string;
    /**
     * Name of the tool
     */
    name: string;
    /**
     * JSON Schema defining the tool's output format
     */
    outputSchema?: string;
}

export interface Example {
    description?: string;
    /**
     * Example input for the tool
     */
    input?: { [key: string]: any };
    /**
     * Example output from the tool
     */
    output?: { [key: string]: any };
    [property: string]: any;
}

/**
 * MCP Connection configuration
 */
export interface MCPConnection {
    config?: ConfigClass;
}

/**
 * MCP Connection Config
 */
export interface ConfigClass {
    /**
     * MCP Server Configuration
     */
    config:               MCPServerConfiguration;
    connectionArguments?: { [key: string]: any };
    connectionOptions?:   { [key: string]: string };
    /**
     * Service Type
     */
    type?: MCPType;
}

/**
 * MCP Server Configuration
 *
 * Configuration for running MCP server via stdio
 */
export interface MCPServerConfiguration {
    /**
     * Arguments to pass to the command
     */
    args?: string[];
    /**
     * Command to execute the MCP server
     */
    command: string;
    /**
     * Working directory for the MCP server process
     */
    cwd?: string;
    /**
     * Environment variables for the MCP server process
     */
    env?: { [key: string]: string };
}

/**
 * Service Type
 *
 * Service type.
 */
export enum MCPType {
    MCP = "Mcp",
}

/**
 * Life Cycle properties of the entity
 *
 * This schema defines Life Cycle Properties.
 */
export interface LifeCycle {
    /**
     * Access Details about accessed aspect of the data asset
     */
    accessed?: AccessDetails;
    /**
     * Access Details about created aspect of the data asset
     */
    created?: AccessDetails;
    /**
     * Access Details about updated aspect of the data asset
     */
    updated?: AccessDetails;
}

/**
 * Access Details about accessed aspect of the data asset
 *
 * Access details of an entity
 *
 * Access Details about created aspect of the data asset
 *
 * Access Details about updated aspect of the data asset
 */
export interface AccessDetails {
    /**
     * User, Pipeline, Query that created,updated or accessed the data asset
     */
    accessedBy?: EntityReference;
    /**
     * Any process that accessed the data asset that is not captured in OpenMetadata.
     */
    accessedByAProcess?: string;
    /**
     * Timestamp of data asset accessed for creation, update, read.
     */
    timestamp: number;
}

/**
 * User, Pipeline, Query that created,updated or accessed the data asset
 *
 * This schema defines the EntityReference type used for referencing an entity.
 * EntityReference is used for capturing relationships from one entity to another. For
 * example, a table has an attribute called database of type EntityReference that captures
 * the relationship of a table `belongs to a` database.
 *
 * Owners of this MCP service.
 *
 * This schema defines the EntityReferenceList type used for referencing an entity.
 * EntityReference is used for capturing relationships from one entity to another. For
 * example, a table has an attribute called database of type EntityReference that captures
 * the relationship of a table `belongs to a` database.
 */
export interface EntityReference {
    /**
     * If true the entity referred to has been soft-deleted.
     */
    deleted?: boolean;
    /**
     * Optional description of entity.
     */
    description?: string;
    /**
     * Display Name that identifies this entity.
     */
    displayName?: string;
    /**
     * Fully qualified name of the entity instance. For entities such as tables, databases
     * fullyQualifiedName is returned in this field. For entities that don't have name hierarchy
     * such as `user` and `team` this will be same as the `name` field.
     */
    fullyQualifiedName?: string;
    /**
     * Link to the entity resource.
     */
    href?: string;
    /**
     * Unique identifier that identifies an entity instance.
     */
    id: string;
    /**
     * If true the relationship indicated by this entity reference is inherited from the parent
     * entity.
     */
    inherited?: boolean;
    /**
     * Name of the entity instance.
     */
    name?: string;
    /**
     * Entity type/class name - Examples: `database`, `table`, `metrics`, `databaseService`,
     * `dashboardService`...
     */
    type: string;
}

/**
 * Type of MCP service
 *
 * This schema defines the service types entities which requires a connection.
 */
export enum ServiceType {
    API = "Api",
    Dashboard = "Dashboard",
    Database = "Database",
    Drive = "Drive",
    MCP = "Mcp",
    Messaging = "Messaging",
    Metadata = "Metadata",
    MlModel = "MlModel",
    Pipeline = "Pipeline",
    Search = "Search",
    Security = "Security",
    Storage = "Storage",
}

/**
 * This schema defines the type for labeling an entity with a Tag.
 */
export interface TagLabel {
    /**
     * Description for the tag label.
     */
    description?: string;
    /**
     * Display Name that identifies this tag.
     */
    displayName?: string;
    /**
     * Link to the tag resource.
     */
    href?: string;
    /**
     * Label type describes how a tag label was applied. 'Manual' indicates the tag label was
     * applied by a person. 'Derived' indicates a tag label was derived using the associated tag
     * relationship (see Classification.json for more details). 'Propagated` indicates a tag
     * label was propagated from upstream based on lineage. 'Automated' is used when a tool was
     * used to determine the tag label.
     */
    labelType: LabelType;
    /**
     * Name of the tag or glossary term.
     */
    name?: string;
    /**
     * Label is from Tags or Glossary.
     */
    source: TagSource;
    /**
     * 'Suggested' state is used when a tag label is suggested by users or tools. Owner of the
     * entity must confirm the suggested labels before it is marked as 'Confirmed'.
     */
    state:  State;
    style?: Style;
    tagFQN: string;
}

/**
 * Label type describes how a tag label was applied. 'Manual' indicates the tag label was
 * applied by a person. 'Derived' indicates a tag label was derived using the associated tag
 * relationship (see Classification.json for more details). 'Propagated` indicates a tag
 * label was propagated from upstream based on lineage. 'Automated' is used when a tool was
 * used to determine the tag label.
 */
export enum LabelType {
    Automated = "Automated",
    Derived = "Derived",
    Generated = "Generated",
    Manual = "Manual",
    Propagated = "Propagated",
}

/**
 * Label is from Tags or Glossary.
 */
export enum TagSource {
    Classification = "Classification",
    Glossary = "Glossary",
}

/**
 * 'Suggested' state is used when a tag label is suggested by users or tools. Owner of the
 * entity must confirm the suggested labels before it is marked as 'Confirmed'.
 */
export enum State {
    Confirmed = "Confirmed",
    Suggested = "Suggested",
}

/**
 * UI Style is used to associate a color code and/or icon to entity to customize the look of
 * that entity in UI.
 */
export interface Style {
    /**
     * Hex Color Code to mark an entity such as GlossaryTerm, Tag, Domain or Data Product.
     */
    color?: string;
    /**
     * An icon to associate with GlossaryTerm, Tag, Domain or Data Product.
     */
    iconURL?: string;
}
