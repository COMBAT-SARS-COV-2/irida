import { atom, selector, selectorFamily } from "recoil";
import { getPipelineDetails } from "../../apis/pipelines/pipelines";
import { PIPELINE_ID } from "../../pages/launch/launch-utilities";

const pipelineIDState = atom({
  key: "piplineId",
  default: PIPELINE_ID,
});

export const savedPipelineParameters = atom({
  key: "savedParameters",
  default: [],
});

const launchState = atom({
  key: "launchState",
  default: {},
});

const launchDetailsQuery = selectorFamily({
  key: "LaunchDetails",
  get: (id) => async () => {
    const response = await getPipelineDetails({ id });
    if (response.error) {
      throw response.error;
    }
    return response;
  },
});

export const launchDetailsInfoQuery = selector({
  key: "LaunchDetailsInfoQuery",
  get: ({ get }) => get(launchDetailsQuery(get(pipelineIDState))),
});
