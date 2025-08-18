/*
 *  Copyright 2023 Collate.
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

import { Card, Col, Row, Space, Table, Tag, Typography } from 'antd';
import { ColumnsType } from 'antd/lib/table';
import { useTranslation } from 'react-i18next';
import DescriptionV1 from '../../components/common/EntityDescription/DescriptionV1';
import ResizablePanels from '../../components/common/ResizablePanels/ResizablePanels';
import { GenericProvider } from '../../components/Customization/GenericProvider/GenericProvider';
import EntityRightPanel from '../../components/Entity/EntityRightPanel/EntityRightPanel';
import { CustomizeEntityType } from '../../constants/Customize.constants';
import { COMMON_RESIZABLE_PANEL_CONFIG } from '../../constants/ResizablePanel.constants';
import { EntityType } from '../../enums/entity.enum';
import { MCPPrompt, MCPResource, MCPTool } from '../../generated/entity/services/mcpService';
import { getEntityName } from '../../utils/EntityUtils';
import { getTagsWithoutTier, getTierTags } from '../../utils/TableUtils';
import { createTagObject } from '../../utils/TagsUtils';

interface McpServiceMainTabContentProps {
  serviceName: string;
  servicePermission: any;
  serviceDetails: any;
  onDescriptionUpdate: (updatedHTML: string) => Promise<void>;
  tags: any[];
  onTagUpdate: (selectedTags?: any[]) => Promise<void>;
  onDataProductUpdate: (dataProducts: any[]) => Promise<void>;
}

const { Title, Text, Paragraph } = Typography;

function McpServiceMainTabContent({
  serviceName,
  servicePermission,
  serviceDetails,
  onDescriptionUpdate,
  onTagUpdate,
  onDataProductUpdate,
}: Readonly<McpServiceMainTabContentProps>) {
  const { t } = useTranslation();

  const tier = getTierTags(serviceDetails?.tags ?? []);
  const tags = getTagsWithoutTier(serviceDetails?.tags ?? []);

  const toolColumns: ColumnsType<MCPTool> = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      render: (name: string) => <Text strong>{name}</Text>,
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: 'Category',
      dataIndex: 'category',
      key: 'category',
      render: (category: string) =>
        category ? <Tag color="blue">{category}</Tag> : '-',
    },
  ];

  const resourceColumns: ColumnsType<MCPResource> = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      render: (name: string) => <Text strong>{name || 'Unnamed'}</Text>,
    },
    {
      title: 'URI',
      dataIndex: 'uri',
      key: 'uri',
      render: (uri: string) => (
        <Text code copyable>
          {uri}
        </Text>
      ),
    },
    {
      title: 'MIME Type',
      dataIndex: 'mimeType',
      key: 'mimeType',
      render: (mimeType: string) =>
        mimeType ? <Tag color="green">{mimeType}</Tag> : '-',
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
  ];

  const promptColumns: ColumnsType<MCPPrompt> = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      render: (name: string) => <Text strong>{name}</Text>,
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: 'Arguments',
      dataIndex: 'arguments',
      key: 'arguments',
      render: (args: any[]) => {
        if (!args || args.length === 0) return '-';
        return (
          <Space direction="vertical" size="small">
            {args.map((arg, index) => (
              <div key={index}>
                <Text code>{arg.name}</Text>
                {arg.required && <Tag color="red">Required</Tag>}
                {arg.type && <Text type="secondary"> ({arg.type})</Text>}
              </div>
            ))}
          </Space>
        );
      },
    },
  ];

  const handleTagSelection = async (selectedTags: any[]) => {
    if (selectedTags) {
      const prevTags =
        tags?.filter((tag) =>
          selectedTags
            .map((selTag) => selTag.tagFQN)
            .includes(tag?.tagFQN as string)
        ) || [];
      const newTags = createTagObject(
        selectedTags.filter((tag) => {
          return !prevTags
            ?.map((prevTag) => prevTag.tagFQN)
            .includes(tag.tagFQN);
        })
      );

      await onTagUpdate([...prevTags, ...newTags]);
    }
  };

  const {
    editTagsPermission,
    editGlossaryTermsPermission,
    editDescriptionPermission,
    editDataProductPermission,
  } = {
    editTagsPermission:
      (servicePermission.EditTags || servicePermission.EditAll) &&
      !serviceDetails.deleted,
    editGlossaryTermsPermission:
      (servicePermission.EditGlossaryTerms || servicePermission.EditAll) &&
      !serviceDetails.deleted,
    editDescriptionPermission:
      (servicePermission.EditDescription || servicePermission.EditAll) &&
      !serviceDetails.deleted,
    editDataProductPermission:
      servicePermission.EditAll && !serviceDetails.deleted,
  };

  return (
    <Row className="main-tab-content" gutter={[0, 16]} wrap={false}>
      <Col className="tab-content-height-with-resizable-panel" span={24}>
        <ResizablePanels
          firstPanel={{
            className: 'entity-resizable-panel-container',
            children: (
              <Row gutter={[16, 16]}>
                <Col data-testid="description-container" span={24}>
                  <DescriptionV1
                    description={serviceDetails.description}
                    entityName={serviceName}
                    entityType={EntityType.MCP_SERVICE}
                    hasEditAccess={editDescriptionPermission}
                    showActions={!serviceDetails.deleted}
                    showCommentsIcon={false}
                    onDescriptionUpdate={onDescriptionUpdate}
                  />
                </Col>

                {serviceDetails.serverInstructions && (
                  <Col span={24}>
                    <Card title="Server Instructions" size="small">
                      <Paragraph>{serviceDetails.serverInstructions}</Paragraph>
                    </Card>
                  </Col>
                )}

                {serviceDetails.availableTools &&
                  serviceDetails.availableTools.length > 0 && (
                    <Col span={24}>
                      <Card
                        title={
                          <Space>
                            <Title level={5} style={{ margin: 0 }}>
                              Available Tools
                            </Title>
                            <Tag color="blue">
                              {serviceDetails.availableTools.length}
                            </Tag>
                          </Space>
                        }
                        size="small">
                        <Table
                          columns={toolColumns}
                          dataSource={serviceDetails.availableTools}
                          pagination={false}
                          rowKey="name"
                          size="small"
                        />
                      </Card>
                    </Col>
                  )}

                {serviceDetails.availableResources &&
                  serviceDetails.availableResources.length > 0 && (
                    <Col span={24}>
                      <Card
                        title={
                          <Space>
                            <Title level={5} style={{ margin: 0 }}>
                              Available Resources
                            </Title>
                            <Tag color="green">
                              {serviceDetails.availableResources.length}
                            </Tag>
                          </Space>
                        }
                        size="small">
                        <Table
                          columns={resourceColumns}
                          dataSource={serviceDetails.availableResources}
                          pagination={false}
                          rowKey="uri"
                          size="small"
                        />
                      </Card>
                    </Col>
                  )}

                {serviceDetails.availablePrompts &&
                  serviceDetails.availablePrompts.length > 0 && (
                    <Col span={24}>
                      <Card
                        title={
                          <Space>
                            <Title level={5} style={{ margin: 0 }}>
                              Available Prompts
                            </Title>
                            <Tag color="purple">
                              {serviceDetails.availablePrompts.length}
                            </Tag>
                          </Space>
                        }
                        size="small">
                        <Table
                          columns={promptColumns}
                          dataSource={serviceDetails.availablePrompts}
                          pagination={false}
                          rowKey="name"
                          size="small"
                        />
                      </Card>
                    </Col>
                  )}

                {(!serviceDetails.availableTools ||
                  serviceDetails.availableTools.length === 0) &&
                  (!serviceDetails.availableResources ||
                    serviceDetails.availableResources.length === 0) &&
                  (!serviceDetails.availablePrompts ||
                    serviceDetails.availablePrompts.length === 0) && (
                    <Col span={24}>
                      <Card>
                        <Text type="secondary">
                          No tools, resources, or prompts are currently available
                          from this MCP server.
                        </Text>
                      </Card>
                    </Col>
                  )}
              </Row>
            ),
            ...COMMON_RESIZABLE_PANEL_CONFIG.LEFT_PANEL,
          }}
          secondPanel={{
            children: (
              <GenericProvider
                data={serviceDetails}
                permissions={servicePermission}
                type={EntityType.MCP_SERVICE as CustomizeEntityType}
                onUpdate={async (updatedData) => {
                  // Handle service update
                }}>
                <div data-testid="entity-right-panel">
                  <EntityRightPanel
                    editDataProductPermission={editDataProductPermission}
                    editGlossaryTermsPermission={editGlossaryTermsPermission}
                    editTagPermission={editTagsPermission}
                    entityType={EntityType.MCP_SERVICE}
                    selectedTags={tags}
                    showDataProductContainer={true}
                    showTaskHandler={false}
                    onDataProductUpdate={onDataProductUpdate}
                    onTagSelectionChange={handleTagSelection}
                  />
                </div>
              </GenericProvider>
            ),
            ...COMMON_RESIZABLE_PANEL_CONFIG.RIGHT_PANEL,
            className:
              'entity-resizable-right-panel-container entity-resizable-panel-container',
          }}
        />
      </Col>
    </Row>
  );
}

export default McpServiceMainTabContent;