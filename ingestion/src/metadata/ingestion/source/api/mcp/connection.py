#  Copyright 2025 Collate
#  Licensed under the Collate Community License, Version 1.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#  https://github.com/open-metadata/OpenMetadata/blob/main/ingestion/LICENSE
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
"""
Source connection handler for MCP services
"""
from typing import Optional

from metadata.generated.schema.entity.automations.workflow import (
    Workflow as AutomationWorkflow,
)
from metadata.generated.schema.entity.services.connections.api.mcpConnection import (
    McpConnection,
)
from metadata.ingestion.connections.test_connections import test_connection_steps
from metadata.ingestion.ometa.ometa_api import OpenMetadata
from metadata.ingestion.source.api.mcp.client import StdioMCPClient


def get_connection(connection: McpConnection) -> StdioMCPClient:
    """
    Create connection based on the MCP connection type
    """
    if connection.config:
        # For now, we only support stdio connections
        return StdioMCPClient(
            command=connection.config.command,
            args=connection.config.args or [],
            env=connection.config.env,
            cwd=connection.config.cwd,
        )
    else:
        raise ValueError("MCP connection configuration is required")


def test_connection(
    metadata: OpenMetadata,
    connection: McpConnection,
    service_type: str,
    automation_workflow: Optional[AutomationWorkflow] = None,
) -> None:
    """
    Test connection to MCP service
    """

    async def test_connection_inner():
        client = get_connection(connection)
        try:
            # Test 1: Initialize connection
            response = await client.initialize()

            # Test 2: List tools
            tools = await client.list_tools()

            # Test 3: List resources
            resources = await client.list_resources()

            # Test 4: List prompts
            prompts = await client.list_prompts()

        finally:
            await client.close()

    def test_mcp_connection():
        """Test MCP connection"""
        import asyncio

        asyncio.run(test_connection_inner())

    test_fn = {
        "name": "MCP Connection Test",
        "fn": test_mcp_connection,
    }

    test_connection_steps(
        metadata=metadata,
        test_fn=test_fn,
        service_type=service_type,
        automation_workflow=automation_workflow,
    )
