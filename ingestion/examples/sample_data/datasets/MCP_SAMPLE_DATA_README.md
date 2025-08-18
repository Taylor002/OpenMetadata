# MCP Service Sample Data

This directory contains sample data for MCP (Model Context Protocol) services in OpenMetadata.

## Files

### mcp_service.json
Contains sample MCP service definitions including:
- **Everything MCP Server**: A comprehensive server with file operations, web browsing, and utility tools
- **GitHub MCP Server**: Specialized for GitHub operations (repositories, issues, pull requests)
- **Database Query MCP Server**: For executing database queries and schema management

Each service includes:
- Connection configuration (command, args, environment variables)
- Available tools with input schemas
- Available resources with URIs and MIME types
- Available prompts with argument definitions
- Server instructions for usage

### mcp_tools_execution.json
Contains sample execution data showing:
- Tool execution examples with inputs and outputs
- Response times and success status
- Prompt execution examples with AI-generated responses

## Usage

### Loading Sample Data

1. **Via API**:
```bash
curl -X POST http://localhost:8585/api/v1/services/mcpServices \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d @mcp_service.json
```

2. **Via Ingestion Workflow**:
Use the sample workflow configuration in `mcp_service_workflow.yaml`

### Sample MCP Server Configurations

#### Everything Server (File & Web Operations)
```yaml
command: npx
args: ["-y", "@modelcontextprotocol/server-everything"]
env:
  NODE_ENV: production
```

#### GitHub Server
```yaml
command: node
args: ["/opt/mcp-servers/github-server.js"]
env:
  GITHUB_TOKEN: "${GITHUB_TOKEN}"
```

#### Database Server
```yaml
command: python
args: ["-m", "mcp_servers.database"]
env:
  DATABASE_URL: "postgresql://user:password@localhost:5432/mydb"
```

## Tool Categories

### File Operations
- `read_file`: Read file contents
- `write_file`: Write content to files
- `list_directory`: List directory contents

### Web Operations
- `search_web`: Search the internet
- `fetch_url`: Fetch content from URLs

### Repository Management
- `list_repositories`: List GitHub repositories
- `get_repository`: Get repository details
- `create_issue`: Create GitHub issues
- `list_pull_requests`: List PRs

### Database Operations
- `execute_query`: Run SQL queries
- `list_tables`: List database tables
- `describe_table`: Get table schema

## Prompts

### Code Review
Reviews code for best practices, security issues, and improvements.

### Generate Documentation
Generates documentation from code or API specifications.

### Data Analysis
Analyzes data and provides insights.

### Query Generation
Generates SQL queries from natural language descriptions.

## Testing

To test MCP service connections:

```python
from metadata.ingestion.source.api.mcp.client import StdioMCPClient
import asyncio

async def test_connection():
    client = StdioMCPClient(
        command="npx",
        args=["-y", "@modelcontextprotocol/server-everything"]
    )
    
    await client.initialize()
    tools = await client.list_tools()
    print(f"Found {len(tools)} tools")
    
    await client.close()

asyncio.run(test_connection())
```

## Notes

- MCP servers run as separate processes
- Environment variables can reference secrets (e.g., `${GITHUB_TOKEN}`)
- Tools have JSON Schema definitions for input validation
- Resources use URI schemes to identify content types
- Prompts can have complex argument structures