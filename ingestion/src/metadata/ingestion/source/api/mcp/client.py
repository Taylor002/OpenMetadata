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
MCP service client
"""
import asyncio
import json
import subprocess
import uuid
from abc import ABC, abstractmethod
from typing import Any, Dict, List, Optional

from metadata.ingestion.source.api.mcp.models import (
    MCPInitializeResponse,
    MCPPrompt,
    MCPPromptsListResponse,
    MCPResource,
    MCPResourcesListResponse,
    MCPTool,
    MCPToolsListResponse,
)
from metadata.utils.logger import ingestion_logger

logger = ingestion_logger()


class MCPClient(ABC):
    """Abstract base class for MCP clients"""

    @abstractmethod
    async def initialize(self) -> MCPInitializeResponse:
        """Initialize the MCP connection"""
        pass

    @abstractmethod
    async def list_tools(self) -> List[MCPTool]:
        """List available tools"""
        pass

    @abstractmethod
    async def list_resources(self) -> List[MCPResource]:
        """List available resources"""
        pass

    @abstractmethod
    async def list_prompts(self) -> List[MCPPrompt]:
        """List available prompts"""
        pass

    @abstractmethod
    async def close(self):
        """Close the connection"""
        pass


class StdioMCPClient(MCPClient):
    """MCP client for stdio communication"""

    def __init__(
        self,
        command: str,
        args: List[str],
        env: Optional[Dict[str, str]] = None,
        cwd: Optional[str] = None,
    ):
        self.command = command
        self.args = args
        self.env = env
        self.cwd = cwd
        self.process = None
        self._read_task = None
        self._responses = {}

    async def start(self):
        """Start the MCP server process"""
        try:
            self.process = await asyncio.create_subprocess_exec(
                self.command,
                *self.args,
                stdin=subprocess.PIPE,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                env=self.env,
                cwd=self.cwd,
            )

            # Start reading responses
            self._read_task = asyncio.create_task(self._read_responses())

        except Exception as e:
            logger.error(f"Failed to start MCP server: {e}")
            raise

    async def _read_responses(self):
        """Read responses from the server"""
        while self.process and self.process.stdout:
            try:
                line = await self.process.stdout.readline()
                if not line:
                    break

                message = json.loads(line.decode())

                # Handle responses
                if "id" in message and message["id"] in self._responses:
                    self._responses[message["id"]].set_result(message)

            except json.JSONDecodeError as e:
                logger.warning(f"Failed to decode JSON response: {e}")
            except Exception as e:
                logger.error(f"Error reading response: {e}")
                break

    async def _send_request(
        self, method: str, params: Optional[Dict[str, Any]] = None
    ) -> Dict[str, Any]:
        """Send a JSON-RPC request to the server"""
        request_id = str(uuid.uuid4())
        request = {
            "jsonrpc": "2.0",
            "method": method,
            "id": request_id,
            "params": params or {},
        }

        if not self.process or not self.process.stdin:
            raise RuntimeError("MCP server process not started")

        # Create a future for the response
        response_future = asyncio.Future()
        self._responses[request_id] = response_future

        # Send the request
        self.process.stdin.write(json.dumps(request).encode() + b"\n")
        await self.process.stdin.drain()

        # Wait for response
        try:
            response = await asyncio.wait_for(response_future, timeout=30.0)
            if "error" in response:
                raise Exception(f"MCP server error: {response['error']}")
            return response.get("result", {})
        finally:
            self._responses.pop(request_id, None)

    async def initialize(self) -> MCPInitializeResponse:
        """Initialize the MCP connection"""
        await self.start()

        result = await self._send_request(
            "initialize",
            {
                "protocolVersion": "1.0.0",
                "clientInfo": {"name": "OpenMetadata", "version": "1.0.0"},
            },
        )

        return MCPInitializeResponse(
            protocol_version=result.get("protocolVersion", "1.0.0"),
            server_info=result.get("serverInfo", {}),
            capabilities=result.get("capabilities", {}),
        )

    async def list_tools(self) -> List[MCPTool]:
        """List available tools"""
        result = await self._send_request("tools/list")
        response = MCPToolsListResponse(**result)
        return response.tools

    async def list_resources(self) -> List[MCPResource]:
        """List available resources"""
        result = await self._send_request("resources/list")
        response = MCPResourcesListResponse(**result)
        return response.resources

    async def list_prompts(self) -> List[MCPPrompt]:
        """List available prompts"""
        result = await self._send_request("prompts/list")
        response = MCPPromptsListResponse(**result)
        return response.prompts

    async def close(self):
        """Close the connection"""
        if self._read_task:
            self._read_task.cancel()

        if self.process:
            self.process.terminate()
            await self.process.wait()


class SSEMCPClient(MCPClient):
    """MCP client for Server-Sent Events communication"""

    def __init__(self, url: str):
        self.url = url
        # SSE implementation would go here
        raise NotImplementedError("SSE MCP client not yet implemented")

    async def initialize(self) -> MCPInitializeResponse:
        """Initialize the MCP connection"""
        raise NotImplementedError("SSE MCP client not yet implemented")

    async def list_tools(self) -> List[MCPTool]:
        """List available tools"""
        raise NotImplementedError("SSE MCP client not yet implemented")

    async def list_resources(self) -> List[MCPResource]:
        """List available resources"""
        raise NotImplementedError("SSE MCP client not yet implemented")

    async def list_prompts(self) -> List[MCPPrompt]:
        """List available prompts"""
        raise NotImplementedError("SSE MCP client not yet implemented")

    async def close(self):
        """Close the connection"""
        raise NotImplementedError("SSE MCP client not yet implemented")
