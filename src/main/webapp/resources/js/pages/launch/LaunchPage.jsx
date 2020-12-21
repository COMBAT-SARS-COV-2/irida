import React from "react";
import { Card, Col, Row, Skeleton, Spin } from "antd";
import { SPACE_LG } from "../../styles/spacing";
import { useLaunch } from "./launch-context";
import { LaunchContent } from "./LaunchContent";

/**
 * React component to render the pipeline launch page.
 * @returns {JSX.Element}
 * @constructor
 */
export function LaunchPage() {
  return (
    <React.Suspense fallback={<div>loading ....</div>}>
      <LaunchContent />
    </React.Suspense>
  );
}
