import { Empty, Space, Table } from "antd";
import React from "react";

/**
 * Component for showing metadata fields associated with a project.
 *
 * @param {number} projectId - Identifier for the current project
 * @returns {JSX.Element|string}
 */
export default function MetadataFieldsListMember({ projectId }) {
  const { data: fields, isLoading } = useGetMketadataFieldsForProjectQuery(
    projectId
  );

  const columns = [
    {
      title: i18n("MetadataField.label"),
      dataIndex: "label",
      key: "label",
      className: "t-m-field-label",
    },
    {
      title: i18n("MetadataField.type"),
      dataIndex: "type",
      key: "text",
    },
  ];

  return (
    <Space direction="vertical" style={{ display: "block" }}>
      <Table
        loading={isLoading}
        pagination={false}
        rowClassName={() => `t-m-field`}
        locale={{
          emptyText: (
            <Empty
              description={i18n("MetadataFieldsList.empty")}
              image={Empty.PRESENTED_IMAGE_SIMPLE}
            />
          ),
        }}
        scroll={{ y: 800 }}
        dataSource={fields}
        columns={columns}
      />
    </Space>
  );
}
