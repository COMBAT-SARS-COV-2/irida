import { Button, Card, Col, PageHeader, Row, Space, Steps } from "antd";
import React, { useEffect, useState } from "react";
import { render } from "react-dom";
import { Provider, useSelector } from "react-redux";
import { useGetSampleIdsForProjectQuery } from "../../../apis/projects/samples";
import { setBaseUrl } from "../../../utilities/url-utilities";
import { ShareMetadata } from "./ShareMetadata";
import { ShareNoSamples } from "./ShareNoSamples";
import { ShareProject } from "./ShareProject";
import { ShareSamples } from "./ShareSamples";
import store from "./store";

/**
 * Base component for sharing samples between projects.
 *
 * @returns {JSX.Element}
 * @constructor
 */
function ShareApp() {
  const [step, setStep] = useState(0);
  const [prevDisabled, setPrevDisabled] = useState(true);
  const [nextDisabled, setNextDisabled] = useState(true);
  const [samples, setSamples] = useState();
  const [error, setError] = useState(undefined);

  /*
  Create redirect href to project samples page.
  */
  const [redirect] = useState(
    () => window.location.href.match(/(.*)\/share/)[1]
  );

  const { originalSamples = [], currentProject, projectId } = useSelector(
    (state) => state.shareReducer
  );

  const { data: existingIds = [] } = useGetSampleIdsForProjectQuery(projectId, {
    skip: !projectId,
  });

  const filtered = originalSamples.filter(
    (sample) => !existingIds.includes(sample.id)
  );

  const steps = [
    {
      title: "Project",
      component: <ShareProject />,
    },
    {
      title: "Samples",
      component: <ShareSamples samples={filtered} redirect={redirect} />,
    },
    { title: "Metadata Restrictions", component: <ShareMetadata /> },
  ];

  useEffect(() => {
    if (step === 0) {
      setPrevDisabled(true);
      setNextDisabled(projectId === undefined && typeof error === "undefined");
      return;
    }
    setPrevDisabled(false);
    setNextDisabled(step === steps.length - 1);
  }, [error, projectId, step, steps.length]);

  /*
  1. No Samples - this would be if the user came to this page from anything
  other than the share samples link.
   */
  const NO_SAMPLES =
    typeof originalSamples === "undefined" || originalSamples.length === 0;

  if (NO_SAMPLES) {
    return <ShareNoSamples redirect={redirect} />;
  }

  /**
   * Return to previous page (project samples page)
   */
  const goToPrevious = () =>
    (window.location.href = setBaseUrl(`/projects/${currentProject}/samples`));

  const nextStep = () => setStep(step + 1);
  const previousStep = () => setStep(step - 1);

  return (
    <Row>
      <Col xl={{ span: 18, offset: 3 }} xs={24}>
        <Card>
          <PageHeader
            ghost={false}
            title={i18n("ShareSamples.title")}
            onBack={goToPrevious}
          >
            <Row>
              <Col span={6}>
                <Steps
                  direction="vertical"
                  current={step}
                  style={{ height: 400 }}
                >
                  {steps.map((step) => (
                    <Steps.Step key={step.title} title={step.title} />
                  ))}
                </Steps>
              </Col>
              <Col span={18}>
                <Space direction="vertical" style={{ width: `100%` }}>
                  {steps[step].component}
                  {error}
                  <div
                    style={{
                      display: "flex",
                      justifyContent: "space-between",
                    }}
                  >
                    <Button disabled={prevDisabled} onClick={previousStep}>
                      Previous
                    </Button>
                    <Button disabled={nextDisabled} onClick={nextStep}>
                      Next
                    </Button>
                  </div>
                </Space>
              </Col>
            </Row>
          </PageHeader>
        </Card>
      </Col>
    </Row>
  );
}

render(
  <Provider store={store}>
    <ShareApp />
  </Provider>,
  document.querySelector("#root")
);
