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
MCP service models
"""
from typing import Any, Dict, List, Optional

from pydantic import BaseModel, Field


class MCPTool(BaseModel):
    """MCP Tool Model"""

    name: str
    description: Optional[str] = None
    category: Optional[str] = None
    inputSchema: Optional[Dict[str, Any]] = Field(None, alias="input_schema")

    class Config:
        populate_by_name = True


class MCPResource(BaseModel):
    """MCP Resource Model"""

    uri: str
    name: str
    description: Optional[str] = None
    mimeType: Optional[str] = Field(None, alias="mime_type")

    class Config:
        populate_by_name = True


class MCPPrompt(BaseModel):
    """MCP Prompt Model"""

    name: str
    description: Optional[str] = None
    arguments: Optional[Dict[str, Any]] = None


class MCPServerInfo(BaseModel):
    """MCP Server Information Model"""

    name: str
    version: Optional[str] = None
    protocolVersion: Optional[str] = Field(None, alias="protocol_version")
    serverInfo: Optional[Dict[str, Any]] = Field(None, alias="server_info")
    capabilities: Optional[Dict[str, Any]] = None

    class Config:
        populate_by_name = True


class MCPInitializeResponse(BaseModel):
    """MCP Initialize Response Model"""

    protocolVersion: str = Field(alias="protocol_version")
    serverInfo: MCPServerInfo = Field(alias="server_info")
    capabilities: Optional[Dict[str, Any]] = None

    class Config:
        populate_by_name = True


class MCPListResponse(BaseModel):
    """Base class for MCP list responses"""

    pass


class MCPToolsListResponse(MCPListResponse):
    """MCP Tools List Response"""

    tools: List[MCPTool]


class MCPResourcesListResponse(MCPListResponse):
    """MCP Resources List Response"""

    resources: List[MCPResource]


class MCPPromptsListResponse(MCPListResponse):
    """MCP Prompts List Response"""

    prompts: List[MCPPrompt]
