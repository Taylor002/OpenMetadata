/**
 * This schema defines the MCP (Model Context Protocol) Service entity, such as Claude
 * Desktop MCP servers.
 */
export interface MCPService {
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
    /**
     * Change that lead to this version of the entity.
     */
    changeDescription?: ChangeDescription;
    connection?:        MCPConnection;
    /**
     * List of data products this entity is part of.
     */
    dataProducts?: EntityReference[];
    /**
     * When `true` indicates the entity has been soft deleted.
     */
    deleted?: boolean;
    /**
     * Description of a MCP service instance.
     */
    description?: string;
    /**
     * Display Name that identifies this MCP service.
     */
    displayName?: string;
    /**
     * List of domains the MCP service belongs to.
     */
    domains?: EntityReference[];
    /**
     * Followers of this MCP service.
     */
    followers?: EntityReference[];
    /**
     * Fully qualified name of the MCP service.
     */
    fullyQualifiedName?: string;
    /**
     * Link to the resource corresponding to this MCP service.
     */
    href?: string;
    /**
     * Unique identifier of this service instance.
     */
    id: string;
    /**
     * Description of incremental changes.
     */
    incrementalChangeDescription?: ChangeDescription;
    /**
     * Name that identifies this MCP service.
     */
    name: string;
    /**
     * Owners of this MCP service.
     */
    owners?: EntityReference[];
    /**
     * References to pipelines deployed for this MCP service to extract metadata, usage, lineage
     * etc.
     */
    pipelines?: EntityReference[];
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
    /**
     * Last test connection results for this service
     */
    testConnectionResult?: TestConnectionResult;
    /**
     * Last update time corresponding to the new version of the entity in Unix epoch time
     * milliseconds.
     */
    updatedAt?: number;
    /**
     * User who made the update.
     */
    updatedBy?: string;
    /**
     * Metadata version of the entity.
     */
    version?: number;
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
 * Change that lead to this version of the entity.
 *
 * Description of the change.
 *
 * Description of incremental changes.
 */
export interface ChangeDescription {
    changeSummary?: { [key: string]: ChangeSummary };
    /**
     * Names of fields added during the version changes.
     */
    fieldsAdded?: FieldChange[];
    /**
     * Fields deleted during the version changes with old value before deleted.
     */
    fieldsDeleted?: FieldChange[];
    /**
     * Fields modified during the version changes with old and new values.
     */
    fieldsUpdated?: FieldChange[];
    /**
     * When a change did not result in change, this could be same as the current version.
     */
    previousVersion?: number;
}

export interface ChangeSummary {
    changedAt?: number;
    /**
     * Name of the user or bot who made this change
     */
    changedBy?:    string;
    changeSource?: ChangeSource;
    [property: string]: any;
}

/**
 * The source of the change. This will change based on the context of the change (example:
 * manual vs programmatic)
 */
export enum ChangeSource {
    Automated = "Automated",
    Derived = "Derived",
    Ingested = "Ingested",
    Manual = "Manual",
    Propagated = "Propagated",
    Suggested = "Suggested",
}

export interface FieldChange {
    /**
     * Name of the entity field that changed.
     */
    name?: string;
    /**
     * New value of the field. Note that this is a JSON string and use the corresponding field
     * type to deserialize it.
     */
    newValue?: any;
    /**
     * Previous value of the field. Note that this is a JSON string and use the corresponding
     * field type to deserialize it.
     */
    oldValue?: any;
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
 * List of data products this entity is part of.
 *
 * This schema defines the EntityReferenceList type used for referencing an entity.
 * EntityReference is used for capturing relationships from one entity to another. For
 * example, a table has an attribute called database of type EntityReference that captures
 * the relationship of a table `belongs to a` database.
 *
 * This schema defines the EntityReference type used for referencing an entity.
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

/**
 * Last test connection results for this service
 *
 * TestConnectionResult is the definition that will encapsulate result of running the test
 * connection steps.
 */
export interface TestConnectionResult {
    /**
     * Last time that the test connection was executed
     */
    lastUpdatedAt?: number;
    /**
     * Test Connection Result computation status.
     */
    status?: StatusType;
    /**
     * Steps to test the connection. Order matters.
     */
    steps: TestConnectionStepResult[];
}

/**
 * Test Connection Result computation status.
 *
 * Enum defining possible Test Connection Result status
 */
export enum StatusType {
    Failed = "Failed",
    Running = "Running",
    Successful = "Successful",
}

/**
 * Function that tests one specific element of the service. E.g., listing schemas, lineage,
 * or tags.
 */
export interface TestConnectionStepResult {
    /**
     * In case of failed step, this field would contain the actual error faced during the step.
     */
    errorLog?: string;
    /**
     * Is this step mandatory to be passed?
     */
    mandatory: boolean;
    /**
     * Results or exceptions to be shared after running the test. This message comes from the
     * test connection definition
     */
    message?: string;
    /**
     * Name of the step being tested
     */
    name: string;
    /**
     * Did the step pass successfully?
     */
    passed: boolean;
}
