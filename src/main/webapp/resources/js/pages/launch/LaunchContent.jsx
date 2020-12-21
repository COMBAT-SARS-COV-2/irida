import React from "react";
import { LaunchPageHeader } from "./LaunchPageHeader";
import { useLaunch } from "./launch-context";
import { LaunchForm } from "./LaunchForm";
import { Space } from "antd";
import { SPACE_LG } from "../../styles/spacing";
import { useRecoilState, useRecoilValue, useSetRecoilState } from "recoil";
import { PIPELINE_ID } from "./launch-utilities";
import {
  launchDetailsInfoQuery,
  savedPipelineParameters,
} from "../../recoil/atoms/launch";

/**
 * React component to layout the content of the pipeline launch.
 * It will act as the top level logic controller.
 */
export function LaunchContent() {
  // const [{ pipeline }] = useLaunch();
  const setSavedParameters = useSetRecoilState(savedPipelineParameters);

  const details = useRecoilValue(launchDetailsInfoQuery);

  React.useEffect(() => {
    setSavedParameters(details.savedPipelineParameters);
  }, []);

  return (
    <Space direction="vertical" style={{ width: `100%`, padding: SPACE_LG }}>
      <h1>LAUNCH</h1>
      <SavedParameters />
      {/*<LaunchPageHeader pipeline={pipeline} />*/}
      {/*<LaunchForm />*/}
    </Space>
  );
}

function SavedParameters() {
  const parameters = useRecoilValue(savedPipelineParameters);
  console.log(parameters);
  return (
    <div>
      {parameters.map((p) => (
        <div key={p.id}>{p.label}</div>
      ))}
    </div>
  );
}
