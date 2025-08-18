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
"""MCP source module"""

import asyncio
import traceback
from typing import Iterable, List, Optional

from metadata.generated.schema.api.services.createMcpService import (
    CreateMcpServiceRequest,
)
from metadata.generated.schema.api.services.mcp.createMcpTool import (
    CreateMcpToolRequest,
)
from metadata.generated.schema.api.services.mcp.createMcpResource import (
    CreateMcpResourceRequest,
)
from metadata.generated.schema.api.services.mcp.createMcpPrompt import (
    CreateMcpPromptRequest,
)
from metadata.generated.schema.entity.services.connections.api.mcpConnection import (
    McpConnection,
)
from metadata.generated.schema.entity.services.ingestionPipelines.status import (
    StackTraceError,
)
from metadata.generated.schema.entity.services.mcpService import (
    McpService,
    McpServiceType,
)
from metadata.generated.schema.metadataIngestion.workflow import (
    Source as WorkflowSource,
)
from metadata.generated.schema.services.connections.mcp.mcpPrompt import McpPrompt
from metadata.generated.schema.services.connections.mcp.mcpResource import McpResource
from metadata.generated.schema.services.connections.mcp.mcpTool import McpTool
from metadata.ingestion.api.models import Either
from metadata.ingestion.api.steps import InvalidSourceException, Source
from metadata.ingestion.api.topology_runner import TopologyRunnerMixin
from metadata.ingestion.models.topology import NodeStage, ServiceTopology, TopologyNode
from metadata.ingestion.ometa.ometa_api import OpenMetadata
from metadata.ingestion.source.api.mcp.client import MCPClient
from metadata.ingestion.source.api.mcp.connection import get_connection
from metadata.ingestion.source.connections import (
    get_connection as get_service_connection,
)
from metadata.utils.logger import ingestion_logger

logger = ingestion_logger()


class McpServiceTopology(ServiceTopology):
    """
    Defines the hierarchy in MCP Services.
    service -> tools, resources, prompts
    """

    root: TopologyNode = TopologyNode(
        producer="get_services",
        stages=[
            NodeStage(
                type_=McpService,
                context="mcp_service",
                processor="yield_create_request_mcp_service",
                overwrite=False,
                must_return=True,
            ),
        ],
        children=[
            TopologyNode(
                producer="get_mcp_tools",
                stages=[
                    NodeStage(
                        type_="mcpTool",
                        context="mcp_tool",
                        processor="yield_create_request_mcp_tool",
                        overwrite=False,
                    ),
                ],
            ),
            TopologyNode(
                producer="get_mcp_resources",
                stages=[
                    NodeStage(
                        type_="mcpResource",
                        context="mcp_resource",
                        processor="yield_create_request_mcp_resource",
                        overwrite=False,
                    ),
                ],
            ),
            TopologyNode(
                producer="get_mcp_prompts",
                stages=[
                    NodeStage(
                        type_="mcpPrompt",
                        context="mcp_prompt",
                        processor="yield_create_request_mcp_prompt",
                        overwrite=False,
                    ),
                ],
            ),
        ],
    )


class McpSource(TopologyRunnerMixin, Source):
    """
    Source implementation to ingest MCP service metadata.
    """

    topology = McpServiceTopology()

    def __init__(self, config: WorkflowSource, metadata: OpenMetadata):
        super().__init__()
        self.config = config
        self.metadata = metadata
        self.source_config = self.config.sourceConfig.config
        self.service = self.metadata.get_by_name(
            entity=McpService, fqn=config.serviceName
        )
        self.client: Optional[MCPClient] = None
        self.service_connection = self.config.serviceConnection.root.config
        self.tools: List[McpTool] = []
        self.resources: List[McpResource] = []
        self.prompts: List[McpPrompt] = []

    @classmethod
    def create(
        cls, config_dict, metadata: OpenMetadata, pipeline_name: Optional[str] = None
    ):
        config: WorkflowSource = WorkflowSource.model_validate(config_dict)
        connection: McpConnection = config.serviceConnection.root.config
        if not isinstance(connection, McpConnection):
            raise InvalidSourceException(
                f"Expected McpConnection, but got {connection}"
            )
        return cls(config, metadata)

    def prepare(self):
        """Prepare the source by connecting to MCP service and collecting metadata"""
        try:
            self.client = get_connection(self.service_connection)

            # Run async operations to collect MCP metadata
            loop = asyncio.new_event_loop()
            asyncio.set_event_loop(loop)
            try:
                loop.run_until_complete(self._collect_metadata())
            finally:
                loop.close()

        except Exception as e:
            logger.error(f"Failed to prepare MCP source: {e}")
            raise

    async def _collect_metadata(self):
        """Collect metadata from MCP service"""
        try:
            # Initialize the connection
            await self.client.initialize()

            # Collect tools
            try:
                tools = await self.client.list_tools()
                self.tools = [
                    McpTool(
                        name=tool.name,
                        description=tool.description,
                        category=tool.category,
                        inputSchema=tool.inputSchema,
                    )
                    for tool in tools
                ]
                logger.info(f"Collected {len(self.tools)} tools from MCP service")
            except Exception as e:
                logger.warning(f"Failed to collect tools: {e}")

            # Collect resources
            try:
                resources = await self.client.list_resources()
                self.resources = [
                    McpResource(
                        uri=resource.uri,
                        name=resource.name,
                        description=resource.description,
                        mimeType=resource.mimeType,
                    )
                    for resource in resources
                ]
                logger.info(
                    f"Collected {len(self.resources)} resources from MCP service"
                )
            except Exception as e:
                logger.warning(f"Failed to collect resources: {e}")

            # Collect prompts
            try:
                prompts = await self.client.list_prompts()
                self.prompts = [
                    McpPrompt(
                        name=prompt.name,
                        description=prompt.description,
                        arguments=prompt.arguments,
                    )
                    for prompt in prompts
                ]
                logger.info(f"Collected {len(self.prompts)} prompts from MCP service")
            except Exception as e:
                logger.warning(f"Failed to collect prompts: {e}")

        finally:
            await self.client.close()

    def get_services(self) -> Iterable[WorkflowSource]:
        """Return the service being processed"""
        yield self.config

    def yield_create_request_mcp_service(
        self, config: WorkflowSource
    ) -> Iterable[Either[CreateMcpServiceRequest]]:
        """Create the MCP service with collected metadata"""
        try:
            # Determine service type based on connection config
            service_type = McpServiceType.Stdio  # Default to stdio
            if hasattr(self.service_connection, "serviceType"):
                service_type = self.service_connection.serviceType

            service_request = CreateMcpServiceRequest(
                name=config.serviceName,
                serviceType=service_type,
                connection=config.serviceConnection.root,
            )

            yield Either(right=service_request)
            self.register_record(service_request=service_request)

        except Exception as exc:
            yield Either(
                left=StackTraceError(
                    name=config.serviceName,
                    error=f"Error creating MCP service request: {exc}",
                    stackTrace=traceback.format_exc(),
                )
            )

    def test_connection(self) -> None:
        """Test the connection to MCP service"""
        service_connection = get_service_connection(
            service=self.service, metadata=self.metadata
        )
        from metadata.ingestion.source.api.mcp.connection import test_connection

        test_connection(
            metadata=self.metadata,
            connection=service_connection,
            service_type=self.service.serviceType.value,
        )

    def get_mcp_tools(self) -> Iterable[Either[CreateMcpToolRequest]]:
        """Yield MCP tools for the service"""
        for tool in self.tools:
            try:
                tool_request = CreateMcpToolRequest(
                    name=tool.name,
                    service=self.context.get().mcp_service,
                    description=tool.description,
                    category=tool.category,
                    inputSchema=tool.inputSchema,
                )
                yield Either(right=tool_request)
                self.register_record(tool_request=tool_request)
            except Exception as exc:
                yield Either(
                    left=StackTraceError(
                        name=tool.name,
                        error=f"Error creating MCP tool request: {exc}",
                        stackTrace=traceback.format_exc(),
                    )
                )

    def get_mcp_resources(self) -> Iterable[Either[CreateMcpResourceRequest]]:
        """Yield MCP resources for the service"""
        for resource in self.resources:
            try:
                resource_request = CreateMcpResourceRequest(
                    name=resource.name or resource.uri,
                    service=self.context.get().mcp_service,
                    uri=resource.uri,
                    description=resource.description,
                    mimeType=resource.mimeType,
                )
                yield Either(right=resource_request)
                self.register_record(resource_request=resource_request)
            except Exception as exc:
                yield Either(
                    left=StackTraceError(
                        name=resource.name or resource.uri,
                        error=f"Error creating MCP resource request: {exc}",
                        stackTrace=traceback.format_exc(),
                    )
                )

    def get_mcp_prompts(self) -> Iterable[Either[CreateMcpPromptRequest]]:
        """Yield MCP prompts for the service"""
        for prompt in self.prompts:
            try:
                prompt_request = CreateMcpPromptRequest(
                    name=prompt.name,
                    service=self.context.get().mcp_service,
                    description=prompt.description,
                    arguments=prompt.arguments,
                )
                yield Either(right=prompt_request)
                self.register_record(prompt_request=prompt_request)
            except Exception as exc:
                yield Either(
                    left=StackTraceError(
                        name=prompt.name,
                        error=f"Error creating MCP prompt request: {exc}",
                        stackTrace=traceback.format_exc(),
                    )
                )

    def yield_create_request_mcp_tool(
        self, tool_request: CreateMcpToolRequest
    ) -> Iterable[Either[CreateMcpToolRequest]]:
        """Process and yield MCP tool create request"""
        yield Either(right=tool_request)

    def yield_create_request_mcp_resource(
        self, resource_request: CreateMcpResourceRequest
    ) -> Iterable[Either[CreateMcpResourceRequest]]:
        """Process and yield MCP resource create request"""
        yield Either(right=resource_request)

    def yield_create_request_mcp_prompt(
        self, prompt_request: CreateMcpPromptRequest
    ) -> Iterable[Either[CreateMcpPromptRequest]]:
        """Process and yield MCP prompt create request"""
        yield Either(right=prompt_request)

    def close(self):
        """Close the source"""
        pass
