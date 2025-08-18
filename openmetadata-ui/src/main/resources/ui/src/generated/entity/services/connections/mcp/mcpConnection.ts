/**
 * MCP Connection Config
 */
export interface MCPConnection {
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
