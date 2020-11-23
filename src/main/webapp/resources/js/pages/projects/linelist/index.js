import React from "react";
import { render } from "react-dom";
import { setBaseUrl } from "../../../utilities/url-utilities";
import { Select, Space, Table } from "antd";

__webpack_public_path__ = setBaseUrl(`dist/`);

function App() {
  const [entries, setEntries] = React.useState();
  const [templates, setTemplates] = React.useState([]);
  const [currentTemplate, setCurrentTemplate] = React.useState();

  const rowSelection = {
    onChange: (selectedRowKeys, selectedRows) => {
      console.log(
        `selectedRowKeys: ${selectedRowKeys}`,
        "selectedRows: ",
        selectedRows
      );
    },
    getCheckboxProps: (record) => ({
      // disabled: record.name === "Disabled User", // Column configuration not to be checked
      name: record.name,
    }),
  };

  React.useEffect(() => {
    const templatePromise = fetch(
      setBaseUrl(`/linelist/templates?projectId=${window.project.id}`)
    ).then((response) => response.json());

    Promise.all([templatePromise]).then(([templateResponse]) => {
      setTemplates(templateResponse);
      setCurrentTemplate(templateResponse[0]);
    });
  }, []);

  React.useEffect(() => {
    // Get the entries
    if (currentTemplate) {
      fetch(
        `/linelist/entries?projectId=${
          window.project.id
        }&${currentTemplate.fields
          .map((field) => `fieldIds=${field.id}`)
          .join("&")}`
      )
        .then((response) => response.json())
        .then(setEntries);
    }
  }, [currentTemplate]);

  const selectedTemplateChange = (id) => {
    const t = templates.find((t) => t.id === id);
    setCurrentTemplate(t);
  };

  return (
    <Space direction="vertical" style={{ width: `100%` }}>
      <Space>
        {/*<MetadataFieldsSelect selectedIds={[]} />*/}
        <Select
          value={currentTemplate?.id}
          style={{ width: 300 }}
          onChange={selectedTemplateChange}
        >
          {templates &&
            templates.map((t) => (
              <Select.Option key={`template-${t.id}`} value={t.id}>
                {t.name}
              </Select.Option>
            ))}
          }
        </Select>
      </Space>
      <Table
        rowSelection={{
          type: "checkbox",
          ...rowSelection,
        }}
        tableLayout="auto"
        rowKey={(entry) => `entry-${entry.id}`}
        scroll={{ x: "max-content" }}
        dataSource={entries}
        columns={currentTemplate?.fields}
      />
    </Space>
  );
}

// Render the application
render(<App />, document.querySelector("#root"));
