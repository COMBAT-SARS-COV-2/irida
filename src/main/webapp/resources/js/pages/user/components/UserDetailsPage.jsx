import React, { useState } from "react";
import { useParams } from "react-router-dom";
import {
  Button,
  Form,
  Input,
  notification,
  Select,
  Space,
  Switch,
  Typography,
} from "antd";
import { ContentLoading } from "../../../components/loader";
import { formatDate } from "../../../utilities/date-utilities";

import {
  useGetUserDetailsQuery,
  useEditUserDetailsMutation,
} from "../../../apis/users/users";

/**
 * React component to display the user details page.
 * @returns {*}
 * @constructor
 */
export default function UserDetailsPage() {
  const { userId } = useParams();
  const { data: userDetails, isLoading } = useGetUserDetailsQuery(userId);
  const [editUser] = useEditUserDetailsMutation();

  const [formErrors, setFormErrors] = useState();

  const onFormFinish = (values) => {
    editUser({ userId: userId, ...values })
      .unwrap()
      .then((payload) => {
        notification.success({
          message: i18n("UserDetailsPage.notification.success"),
        });
      })
      .catch((error) => {
        notification.error({
          message: i18n("UserDetailsPage.notification.error"),
        });
        setFormErrors(error.userDetails);
      });
  };

  return (
    <>
      {isLoading ? (
        <ContentLoading message={i18n("UserDetailsPage.loading.message")} />
      ) : (
        <Space direction="vertical">
          <Typography.Title level={4}>{userDetails.user.username}</Typography.Title>
          <Form
            layout="vertical"
            initialValues={userDetails.user}
            onFinish={onFormFinish}
          >
            <Form.Item
              label={i18n("UserDetailsPage.form.label.firstName")}
              name="firstName"
              help={
                formErrors?.find((error) => error.field === "firstName")
                  ?.message
              }
              validateStatus={
                formErrors?.find((error) => error.field === "firstName")
                  ? "error"
                  : undefined
              }
            >
              <Input />
            </Form.Item>
            <Form.Item
              label={i18n("UserDetailsPage.form.label.lastName")}
              name="lastName"
              help={
                formErrors?.find((error) => error.field === "lastName")?.message
              }
              validateStatus={
                formErrors?.find((error) => error.field === "lastName")
                  ? "error"
                  : undefined
              }
            >
              <Input />
            </Form.Item>
            <Form.Item
              label={i18n("UserDetailsPage.form.label.email")}
              name="email"
              help={
                formErrors?.find((error) => error.field === "email")?.message
              }
              validateStatus={
                formErrors?.find((error) => error.field === "email")
                  ? "error"
                  : undefined
              }
            >
              <Input />
            </Form.Item>
            <Form.Item
              label={i18n("UserDetailsPage.form.label.phoneNumber")}
              name="phoneNumber"
              help={
                formErrors?.find((error) => error.field === "phoneNumber")
                  ?.message
              }
              validateStatus={
                formErrors?.find((error) => error.field === "phoneNumber")
                  ? "error"
                  : undefined
              }
            >
              <Input />
            </Form.Item>
            <Form.Item
              label={i18n("UserDetailsPage.form.label.locale")}
              name="locale"
            >
              <Select>
                {userDetails.locales.map((locale, index) => (
                  <Select.Option
                    key={`user-account-details-locale-${index}`}
                    value={locale.language}
                  >
                    {locale.name}
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>
            <Form.Item
              label={i18n("UserDetailsPage.form.label.role")}
              name="role"
              help={
                formErrors?.find((error) => error.field === "role")?.message
              }
              validateStatus={
                formErrors?.find((error) => error.field === "role")
                  ? "error"
                  : undefined
              }
              hidden={!userDetails.admin}
            >
              <Select>
                {userDetails.allowedRoles.map((role, index) => (
                  <Select.Option
                    key={`user-account-details-role-${index}`}
                    value={role.code}
                    disabled={!userDetails.canEditUserStatus}
                  >
                    {role.name}
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>
            <Form.Item
              label={i18n("UserDetailsPage.form.label.enabled")}
              name="enabled"
              valuePropName="checked"
              hidden={!userDetails.admin}
            >
              <Switch disabled={!userDetails.canEditUserStatus} />
            </Form.Item>
            <Form.Item>
              <Button
                type="primary"
                htmlType="submit"
                disabled={!userDetails.canEditUserInfo}
              >
                {i18n("UserDetailsPage.form.button.submit")}
              </Button>
            </Form.Item>
          </Form>
          <Typography.Text type="secondary">
            {userDetails.user.createdDate
              ? i18n(
                  "UserDetailsPage.createdDate",
                  formatDate({ date: userDetails.user.createdDate })
                )
              : ""}
          </Typography.Text>
          <Typography.Text type="secondary">
            {userDetails.user.modifiedDate
              ? i18n(
                  "UserDetailsPage.modifiedDate",
                  formatDate({ date: userDetails.user.modifiedDate })
                )
              : ""}
          </Typography.Text>
          <Typography.Text type="secondary">
            {userDetails.user.lastLogin
              ? i18n(
                  "UserDetailsPage.lastLogin",
                  formatDate({ date: userDetails.user.lastLogin })
                )
              : ""}
          </Typography.Text>
        </Space>
      )}
    </>
  );
}
