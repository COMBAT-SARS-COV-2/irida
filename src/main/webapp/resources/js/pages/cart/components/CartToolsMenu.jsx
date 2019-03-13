import React from "react";
import PropTypes from "prop-types";
import styled from "styled-components";
import { Icon, Menu, Tooltip } from "antd";
import { Link } from "@reach/router";
import { COLOR_BORDER_LIGHT, grey6 } from "../../../styles/colors";
import { SPACE_MD } from "../../../styles/spacing";
import { getI18N } from "../../../utilities/i18n-utilties";

const MenuWrapper = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 65px;
  border-bottom: 1px solid ${COLOR_BORDER_LIGHT};

  .ant-menu {
    line-height: 65px;
  }
`;

/**
 * Stateless UI component for creating tbs in the CartTools.
 * @param {string} pathname - currently visible path
 * @param {list} paths - list containing path definitions
 * @returns {*}
 */
export function CartToolsMenu({ pathname, paths, toggleSidebar, collapsed }) {
  return (
    <MenuWrapper>
      <Menu
        mode="horizontal"
        selectedKeys={[pathname]}
        style={{ borderBottom: `1px solid ${COLOR_BORDER_LIGHT}` }}
      >
        {paths.map(path => (
          <Menu.Item key={path.key}>
            <Link to={path.link}>{path.text}</Link>
          </Menu.Item>
        ))}
      </Menu>
      <Tooltip placement="bottom" title={getI18N("CartTools.menu.toggle")}>
        <Icon
          style={{ color: grey6, fontSize: 24, margin: SPACE_MD }}
          type={collapsed ? "menu-fold" : "menu-unfold"}
          onClick={toggleSidebar}
        />
      </Tooltip>
    </MenuWrapper>
  );
}

CartToolsMenu.propTypes = {
  /** The current visible path */
  pathname: PropTypes.string.isRequired,
  /** List of paths */
  paths: PropTypes.array.isRequired
};
