/**
 * Pipeline and workflow related API functions
 */
import axios from "axios";
import { setBaseUrl } from "../../utilities/url-utilities";
import { notification } from "antd";

const URL = setBaseUrl(`pipelines/ajax`);
const AJAX_URL = setBaseUrl(`ajax/pipeline`);

/**
 * Get the IRIDA workflow description info for a workflow
 * @param workflowUUID Workflow UUID
 * @return {Promise<*>} `data` contains the OK response; `error` contains error information if an error occurred.
 */
export async function getIridaWorkflowDescription(workflowUUID) {
  try {
    const { data } = await axios({
      method: "get",
      url: `${URL}/${workflowUUID}`,
    });
    return { data };
  } catch (error) {
    return { error };
  }
}

/**
 * Get a listing of all Pipelines in IRIDA.
 * @returns {Promise<AxiosResponse<any> | never>}
 */
export const fetchIridaAnalysisWorkflows = async function () {
  var ajaxUrl = URL;
  if (window.PAGE.automatedProject !== null) {
    ajaxUrl = `${ajaxUrl}?automatedProject=${window.PAGE.automatedProject}`;
  }
  return axios.get(ajaxUrl).then((response) => response.data);
};

/**
 * Get details about a specific pipeline to be able to launch.
 * @param id - UUID identifier for the pipeline
 * @returns {*}
 */
export const getPipelineDetails = ({ id }) =>
  axios
    .get(`${AJAX_URL}/${id}`)
    .then(({ data }) => data)
    .catch((error) => {
      throw new Error(error.response.data);
    });

/**
 * Initiate a new IRIDA workflow pipeline
 * @param id - Identifier for the workflow (UUID)
 * @param parameters - pipeline parameters
 * @returns {Promise<AxiosResponse<any>>}
 */
export const launchPipeline = (id, parameters) =>
  axios
    .post(`${AJAX_URL}/${id}`, parameters)
    .then(({ data }) => data)
    .catch((error) => {
      throw new Error(error.response.data);
    });

export function saveNewPipelineParameters({ label, parameters, id }) {
  return axios
    .post(`${AJAX_URL}/${id}/parameters`, { label, parameters })
    .then(({ data }) => data)
    .catch((error) => {
      throw new Error(error.response.data);
    });
}

export async function fetchPipelineSamples() {
  try {
    const response = await axios.get(`${AJAX_URL}/samples`);
    return response.data;
  } catch (e) {
    notification.error(e.response.data);
  }
}
