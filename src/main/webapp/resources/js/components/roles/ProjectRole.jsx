import { notification, Select } from "antd";
import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { RolesContext } from "../../contexts/roles-context";
import { fetchProjectRoles } from "../../pages/projects/redux/projectSlice";

/**
 * React component to render the project role.  If the user can manage members,
 * then a select component will be rendered allowing the user to change the role
 * any member.  If the user cannot manage, just the label for the project role
 * will be rendered
 *
 * @param {object} user - the current user to be rendered
 * @returns {*}
 * @constructor
 */
export function ProjectRole({
  item,
  // eslint-disable-next-line no-console
  updateFn = () => console.error("updateFn is required"), // TODO: replace this in slice
}) {
  const dispatch = useDispatch();
  const { canManage, roles } = useSelector((state) => state.project);
  const [role, setRole] = useState(item.role);
  const [loading, setLoading] = useState(false);

  console.log(roles);

  React.useEffect(() => {
    dispatch(fetchProjectRoles());
  }, [dispatch]);

  /**
   * Find the translation for any project role.  If the role is not found,
   * just return "UNKNOWN"
   *
   * @param key
   * @returns {*}
   */
  const getRoleFromKey = (key) => {
    const role = roles.find((r) => r.value === key);
    return role ? role.label : "UNKNOWN";
  };

  React.useEffect(() => {
    fetchProjectRoles();
  }, []);

  const onChange = (value) => {
    setLoading(true);
    updateFn({
      id: item.id,
      role: value,
    })
      .then((message) => {
        notification.success({ message });
        setRole(value);
      })
      .catch((error) =>
        notification.error({
          message: error.response.data,
        })
      )
      .finally(() => setLoading(false));
  };

  return canManage ? (
    <Select
      className="t-role-select"
      value={role}
      style={{ width: "100%" }}
      onChange={onChange}
      loading={loading}
      disabled={loading}
    >
      {roles.map((role) => (
        <Select.Option
          className={`t-${role.value}`}
          value={role.value}
          key={role.value}
        >
          {role.label}
        </Select.Option>
      ))}
    </Select>
  ) : (
    getRoleFromKey(item.role)
  );
}
