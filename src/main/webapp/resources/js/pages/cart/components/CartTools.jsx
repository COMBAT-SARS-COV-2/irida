import { Link, Location, navigate, Router } from "@reach/router";
import { Button, Menu, Row, Space } from "antd";
import React, { lazy, Suspense } from "react";
import styled from "styled-components";
import { AnalysesQueue } from "../../../components/AnalysesQueue";
import { IconMenuFold, IconMenuUnfold } from "../../../components/icons/Icons";
import { Pipelines } from "../../../components/pipelines/Pipelines";
import { BORDERED_LIGHT } from "../../../styles/borders";
import { grey1, grey6 } from "../../../styles/colors";
import { SPACE_MD } from "../../../styles/spacing";
import { setBaseUrl } from "../../../utilities/url-utilities";
import { ShareLayout } from "./share";

/*
Lazy loaded since we do not need it unless we came from galaxy.
 */
const GalaxyApp = lazy(() => import("../../../components/galaxy/GalaxyApp"));

const GalaxyComponent = () => (
  <Suspense fallback={<div>Loading ...</div>}>
    <GalaxyApp path="cart/galaxy" default />
  </Suspense>
);

const ToolsWrapper = styled(Row)`
  height: 100%;
  width: 100%;
  border-right: ${BORDERED_LIGHT};
  background-color: ${grey1};
  position: relative;
`;

const ToolsInner = styled.div`
  padding: ${SPACE_MD};
  position: absolute;
  top: 67px;
  right: 0;
  bottom: 0;
  left: 0;
  overflow-x: auto;
`;

const MenuWrapper = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 65px;
  border-bottom: ${BORDERED_LIGHT};
  width: 100%;

  .ant-menu {
    line-height: 65px;
    background-color: transparent;
  }
`;

function CartToolsContent({ count, toggleSidebar, location, collapsed }) {
  console.log(location);
  const [current, setCurrent] = React.useState(location.pathname);
  const [fromGalaxy, setFromGalaxy] = React.useState(
    () => typeof window.GALAXY !== "undefined"
  );

  React.useEffect(() => {
    function removeGalaxy() {
      setFromGalaxy(false);
      navigate(setBaseUrl(`cart/pipelines`));
    }

    if (fromGalaxy) {
      setCurrent("galaxy");
      /*
      If this is within a galaxy session, the user has the opportunity to remove the session
      from IRIDA.  When this happens this listener will ensure that the galaxy tab is removed
      from the UI, and the user is redirected to the pipelines page.
       */
      document.body.addEventListener("galaxy:removal", removeGalaxy);
    }

    return () => {
      document.body.removeEventListener("galaxy:removal", removeGalaxy);
    };
  }, [fromGalaxy]);

  return (
    <ToolsWrapper>
      <MenuWrapper>
        <Menu
          mode="horizontal"
          selectedKeys={[current]}
          style={{ borderBottom: BORDERED_LIGHT }}
          onClick={(e) => setCurrent(e.key)}
        >
          {fromGalaxy && (
            <Menu.Item key="/cart/galaxy">
              <Link to={setBaseUrl(`cart/galaxy`)}>
                {i18n("CartTools.menu.galaxy")}
              </Link>
            </Menu.Item>
          )}
          <Menu.Item key="/cart/pipelines">
            <Link to={setBaseUrl(`cart/pipelines`)}>
              {i18n("CartTools.menu.pipelines")}
            </Link>
          </Menu.Item>
          <Menu.SubMenu
            key="/cart/samples"
            title={i18n("CartTools.menu.samples")}
          >
            <Menu.Item key="share">
              <Link to={setBaseUrl(`cart/share`)}>
                {i18n("CartTools.menu.samples.share")}
              </Link>
            </Menu.Item>
          </Menu.SubMenu>
        </Menu>
        <Space align="center" style={{ padding: `0 ${SPACE_MD}` }}>
          <AnalysesQueue />
          <Button
            type="link"
            onClick={toggleSidebar}
            icon={
              collapsed ? (
                <IconMenuFold style={{ color: grey6, fontSize: 24 }} />
              ) : (
                <IconMenuUnfold style={{ color: grey6, fontSize: 24 }} />
              )
            }
          />
        </Space>
      </MenuWrapper>
      <ToolsInner>
        <Router basepath={setBaseUrl("/cart")}>
          {fromGalaxy && (
            <GalaxyComponent key="galaxy" path={setBaseUrl(`galaxy`)} />
          )}
          <Pipelines
            key="pipelines"
            path={setBaseUrl(`pipelines`)}
            displaySelect={!!count || window.PAGE.automatedProject != null}
          />
          <ShareLayout key="share" path={setBaseUrl(`share`)} />
        </Router>
      </ToolsInner>
    </ToolsWrapper>
  );
}

export default function CartTools({ ...props }) {
  return (
    <Location>
      {({ location }) => <CartToolsContent {...props} location={location} />}
    </Location>
  );
}
