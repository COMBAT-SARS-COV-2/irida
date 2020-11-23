import React from "react";
import { setBaseUrl } from "../../../utilities/url-utilities";
import { Button, Checkbox, Dropdown, Menu, Space } from "antd";
import { IconDropDown } from "../../../components/icons/Icons";

export function MetadataFieldsSelect({ selectedIds, updateSelectedFields }) {
  const [visible, setVisible] = React.useState(false);
  const [fields, setFields] = React.useState();

  React.useEffect(() => {
    fetch(setBaseUrl(`/linelist/fields?projectId=${window.project.id}`))
      .then((response) => response.json())
      .then(setFields);
  }, []);

  function changeFieldSelection(field) {
    console.log(field);
  }

  return (
    <Dropdown
      onVisibleChange={() => setVisible(!visible)}
      visible={visible}
      overlay={
        <Menu>
          {fields
            ? [...fields].map((field) => {
                return (
                  <Menu.Item key={`field=${field.id}`}>
                    <Checkbox
                      onChange={() => changeFieldSelection(field)}
                      checked={field.checked}
                    >
                      {field.title}
                    </Checkbox>
                  </Menu.Item>
                );
              })
            : null}
        </Menu>
      }
    >
      <Button>
        Columns <IconDropDown />
      </Button>
    </Dropdown>
  );
}
