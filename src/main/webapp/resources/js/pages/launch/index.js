import React from "react";
import { render } from "react-dom";
import { LaunchProvider } from "./launch-context";
import { LaunchPage } from "./LaunchPage";
import { RecoilRoot } from "recoil";

/**
 * Render page for launching workflow pipelines.
 */
render(
  <RecoilRoot>
    <LaunchPage />
  </RecoilRoot>,
  document.querySelector("#root")
);
