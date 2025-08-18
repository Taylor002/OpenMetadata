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
Test MCP Service
"""
import unittest
from unittest.mock import AsyncMock, MagicMock, patch

from metadata.generated.schema.metadataIngestion.workflow import (
    Source as WorkflowSource,
)
from metadata.generated.schema.metadataIngestion.workflow import SourceConfig
from metadata.ingestion.source.api.mcp.client import StdioMCPClient
from metadata.ingestion.source.api.mcp.metadata import McpSource
from metadata.ingestion.source.api.mcp.models import (
    MCPInitializeResponse,
    MCPPrompt,
    MCPResource,
    MCPServerInfo,
    MCPTool,
)


class TestMcpService(unittest.TestCase):
    """Test MCP Service"""

    @patch("metadata.ingestion.source.api.mcp.metadata.OpenMetadata")
    def setUp(self, mock_metadata):
        """Set up test fixtures"""
        self.mock_metadata = mock_metadata

        # Mock service
        self.mock_service = MagicMock()
        self.mock_service.name = "test-mcp-service"
        self.mock_metadata.get_by_name.return_value = self.mock_service

        # Create test configuration
        self.config = WorkflowSource(
            serviceName="test-mcp-service",
            serviceConnection={
                "config": {
                    "type": "McpConnection",
                    "config": {
                        "command": "npx",
                        "args": ["-y", "@modelcontextprotocol/server-everything"],
                        "env": {"NODE_ENV": "test"},
                    },
                }
            },
            sourceConfig=SourceConfig(config={}),
        )

        self.mcp_source = McpSource(self.config, self.mock_metadata)

    @patch("metadata.ingestion.source.api.mcp.metadata.get_connection")
    @patch("asyncio.new_event_loop")
    def test_prepare(self, mock_event_loop, mock_get_connection):
        """Test prepare method"""
        # Mock event loop
        mock_loop = MagicMock()
        mock_event_loop.return_value = mock_loop

        # Mock client
        mock_client = AsyncMock(spec=StdioMCPClient)
        mock_get_connection.return_value = mock_client

        # Mock responses
        mock_client.initialize.return_value = MCPInitializeResponse(
            protocol_version="1.0.0",
            server_info=MCPServerInfo(name="test-server", version="1.0.0"),
            capabilities={},
        )

        mock_client.list_tools.return_value = [
            MCPTool(name="read_file", description="Read file contents"),
            MCPTool(name="write_file", description="Write file contents"),
        ]

        mock_client.list_resources.return_value = [
            MCPResource(
                uri="file:///docs",
                name="Documentation",
                description="API docs",
                mime_type="text/markdown",
            ),
        ]

        mock_client.list_prompts.return_value = [
            MCPPrompt(
                name="code_review",
                description="Review code",
                arguments={"language": {"type": "string"}},
            ),
        ]

        # Run prepare
        self.mcp_source.prepare()

        # Verify results
        self.assertEqual(len(self.mcp_source.tools), 2)
        self.assertEqual(len(self.mcp_source.resources), 1)
        self.assertEqual(len(self.mcp_source.prompts), 1)

        # Verify client methods were called
        mock_client.initialize.assert_called_once()
        mock_client.list_tools.assert_called_once()
        mock_client.list_resources.assert_called_once()
        mock_client.list_prompts.assert_called_once()
        mock_client.close.assert_called_once()

    def test_get_services(self):
        """Test get_services method"""
        services = list(self.mcp_source.get_services())
        self.assertEqual(len(services), 1)
        self.assertEqual(services[0], self.config)

    @patch("metadata.ingestion.source.api.mcp.metadata.get_connection")
    def test_yield_create_request_mcp_service(self, mock_get_connection):
        """Test yield_create_request_mcp_service method"""
        # Set up test data
        self.mcp_source.tools = [
            MagicMock(name="tool1", description="Tool 1"),
        ]
        self.mcp_source.resources = [
            MagicMock(uri="resource1", name="Resource 1"),
        ]
        self.mcp_source.prompts = [
            MagicMock(name="prompt1", description="Prompt 1"),
        ]

        # Run the method
        results = list(self.mcp_source.yield_create_request_mcp_service(self.config))

        # Verify results
        self.assertEqual(len(results), 1)
        result = results[0]
        self.assertTrue(result.right)  # Should be successful

        service_request = result.right
        self.assertEqual(service_request.name, "test-mcp-service")
        self.assertIsNotNone(service_request.availableTools)
        self.assertIsNotNone(service_request.availableResources)
        self.assertIsNotNone(service_request.availablePrompts)


class TestMcpClient(unittest.TestCase):
    """Test MCP Client"""

    def setUp(self):
        """Set up test fixtures"""
        self.client = StdioMCPClient(
            command="npx",
            args=["-y", "@modelcontextprotocol/server-everything"],
            env={"NODE_ENV": "test"},
            cwd=None,
        )

    @patch("asyncio.create_subprocess_exec")
    async def test_start(self, mock_subprocess):
        """Test starting the MCP server process"""
        # Mock process
        mock_process = AsyncMock()
        mock_process.stdout = AsyncMock()
        mock_process.stdin = AsyncMock()
        mock_subprocess.return_value = mock_process

        # Start the client
        await self.client.start()

        # Verify subprocess was created with correct arguments
        mock_subprocess.assert_called_once_with(
            "npx",
            "-y",
            "@modelcontextprotocol/server-everything",
            stdin=unittest.mock.ANY,
            stdout=unittest.mock.ANY,
            stderr=unittest.mock.ANY,
            env={"NODE_ENV": "test"},
            cwd=None,
        )

        self.assertIsNotNone(self.client.process)


if __name__ == "__main__":
    unittest.main()
