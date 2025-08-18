/*
 *  Copyright 2025 Collate.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import { MCPType } from '../generated/entity/services/mcpService';

export const MCP_SERVICE_TYPE_OPTIONS = [
  {
    label: 'MCP',
    value: MCPType.MCP,
  },
];

export const getMcpConfig = (type: string) => {
  const config = {
    [MCPType.MCP]: {
      schema: {
        type: 'object',
        properties: {
          type: {
            title: 'Service Type',
            type: 'string',
            enum: [MCPType.MCP],
            default: MCPType.MCP,
          },
          connection: {
            type: 'object',
            title: 'MCP Service Connection',
            properties: {
              config: {
                type: 'object',
                title: 'Server Configuration',
                properties: {
                  command: {
                    type: 'string',
                    title: 'Command',
                    description: 'Command to run the MCP server (e.g., npx, node, python)',
                  },
                  args: {
                    type: 'array',
                    title: 'Arguments',
                    description: 'Arguments to pass to the command',
                    items: {
                      type: 'string',
                    },
                  },
                  env: {
                    type: 'object',
                    title: 'Environment Variables',
                    description: 'Environment variables to set when running the server',
                    additionalProperties: {
                      type: 'string',
                    },
                  },
                  cwd: {
                    type: 'string',
                    title: 'Working Directory',
                    description: 'Working directory for the server process',
                  },
                },
                required: ['command'],
              },
            },
            required: ['config'],
          },
          availableTools: {
            type: 'array',
            title: 'Available Tools',
            description: 'Tools provided by this MCP server',
            items: {
              type: 'object',
              properties: {
                name: {
                  type: 'string',
                  title: 'Tool Name',
                },
                description: {
                  type: 'string',
                  title: 'Tool Description',
                },
                category: {
                  type: 'string',
                  title: 'Category',
                },
                inputSchema: {
                  type: 'object',
                  title: 'Input Schema',
                  description: 'JSON Schema for tool inputs',
                },
              },
              required: ['name'],
            },
          },
          availableResources: {
            type: 'array',
            title: 'Available Resources',
            description: 'Resources provided by this MCP server',
            items: {
              type: 'object',
              properties: {
                uri: {
                  type: 'string',
                  title: 'URI',
                },
                name: {
                  type: 'string',
                  title: 'Resource Name',
                },
                description: {
                  type: 'string',
                  title: 'Resource Description',
                },
                mimeType: {
                  type: 'string',
                  title: 'MIME Type',
                },
              },
              required: ['uri', 'name'],
            },
          },
          availablePrompts: {
            type: 'array',
            title: 'Available Prompts',
            description: 'Prompts provided by this MCP server',
            items: {
              type: 'object',
              properties: {
                name: {
                  type: 'string',
                  title: 'Prompt Name',
                },
                description: {
                  type: 'string',
                  title: 'Prompt Description',
                },
                arguments: {
                  type: 'object',
                  title: 'Arguments',
                  description: 'Arguments for the prompt',
                },
              },
              required: ['name'],
            },
          },
          serverInstructions: {
            type: 'string',
            title: 'Server Instructions',
            description: 'Instructions for using this MCP server',
          },
        },
        required: ['type', 'connection'],
      },
      uiSchema: {
        type: { 'ui:widget': 'hidden' },
        connection: {
          config: {
            command: {
              'ui:placeholder': 'e.g., npx, node, python',
            },
            args: {
              'ui:placeholder': 'e.g., -y, @modelcontextprotocol/server-everything',
            },
            cwd: {
              'ui:placeholder': '/path/to/server/directory',
            },
          },
        },
        serverInstructions: {
          'ui:widget': 'textarea',
          'ui:rows': 4,
        },
      },
    },
  };

  return config[type as MCPType] || {};
};