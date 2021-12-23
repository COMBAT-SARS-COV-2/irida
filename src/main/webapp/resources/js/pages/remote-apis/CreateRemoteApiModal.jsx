import { Form, Input, Modal } from "antd";
import React from "react";
import { PagedTableContext } from "../../components/ant.design/PagedTable";
import { setBaseUrl } from "../../utilities/url-utilities";

/**
 * Component for creating a new remote api client
 *
 * @param {JSX.Element} children - Button to click to open the modal
 * @returns {JSX.Element}
 * @constructor
 */
export function CreateRemoteApiModal({ children }) {
  const { updateTable } = React.useContext(PagedTableContext);
  const [visible, setVisible] = React.useState(false);
  const [form] = Form.useForm();

  /**
   * Handles submission of form for creating remote api.
   *
   * Ensures that all fields are valid and checks against server exceptions
   * @returns {Promise<void>}
   */
  const submitForm = async () => {
    const values = await form.validateFields();

    const formData = new FormData();
    Object.keys(values).forEach((entry) =>
      formData.append(entry, values[entry])
    );
    const response = await fetch(setBaseUrl(`/ajax/remote_api/create`), {
      method: `POST`,
      body: formData,
    });
    const data = await response.json();
    if (!data.errors) {
      form.resetFields();
      setVisible(false);
      updateTable();
    } else {
      // Dynamically set the errors for fields
      const fields = Object.entries(data.errors).map(([field, error]) => ({
        name: field,
        errors: [error],
      }));
      form.setFields(fields);
    }
  };

  return (
    <>
      {React.cloneElement(children, {
        onClick: () => setVisible(true),
      })}
      <Modal
        className="t-create-api"
        title={`Add Remote Connection`}
        visible={visible}
        onCancel={() => setVisible(false)}
        onOk={submitForm}
      >
        <Form
          form={form}
          layout="vertical"
          name="remote_api"
          initialValues={{
            name: "",
            clientId: "",
            clientSecret: "",
            serviceURI: "",
          }}
        >
          <Form.Item
            name="name"
            label={i18n("remoteapi.name")}
            rules={[{ required: true }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            name="clientId"
            label={i18n("remoteapi.clientid")}
            rules={[{ required: true }, { pattern: /^\S+$/g }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            name="clientSecret"
            label={i18n("remoteapi.details.secret")}
            rules={[{ required: true }, { pattern: /^\S+$/g }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            name="serviceURI"
            label={i18n("remoteapi.serviceurl")}
            rules={[
              { required: true },
              {
                pattern: /^(?:http(s)?:\/\/)?[\w.-]+(?:\.[\w\.-]+)+[\w\-\._~:/?#[\]@!\$&'\(\)\*\+,;=.]+$/gm,
              },
            ]}
          >
            <Input />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
