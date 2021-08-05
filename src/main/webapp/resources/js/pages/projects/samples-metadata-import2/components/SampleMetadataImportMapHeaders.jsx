import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { navigate } from "@reach/router"
import {
  Button,
  Radio,
  Typography,
} from "antd";
import { SampleMetadataImportWizard } from "./SampleMetadataImportWizard";
import { BlockRadioInput } from "../../../../components/ant.design/forms/BlockRadioInput";
import { useSetColumnProjectSampleMetadataMutation } from "../../../../apis/metadata/metadata-import";
import { IconArrowLeft, IconArrowRight } from "../../../../components/icons/Icons";

const { Text } = Typography

/**
 * React component that displays Step #2 of the Sample Metadata Uploader.
 * This page is where the user selects the sample name column.
 * @returns {*}
 * @constructor
 */
export function SampleMetadataImportMapHeaders({ projectId }) {
  const dispatch = useDispatch();
  const [column, setColumn] = useState();
  const { headers, sampleNameColumn } = useSelector((state) => state.reducer);
  const [updateColumn, {isError}] = useSetColumnProjectSampleMetadataMutation(projectId, sampleNameColumn);

  React.useEffect(() => {
    setColumn(sampleNameColumn ? sampleNameColumn : headers[0]);
  }, []);

  const onSubmit = () => {
    updateColumn({ projectId: projectId, sampleNameColumn: column });
    navigate('review');
  };

  return (
    <SampleMetadataImportWizard currentStep={1}>
      <Text>
        {i18n("SampleMetadataImportMapHeaders.description")}
      </Text>
      <Radio.Group style={{ width: `100%` }} value={column} onChange={(e) => setColumn(e.target.value)}>
        {headers.map((header, index) => (
          <BlockRadioInput key={`radio-item-header-${index}`}>
            <Radio key={`radio-header-${index}`} value={header}>
              {header}
            </Radio>
          </BlockRadioInput>
        ))}
      </Radio.Group>
      <div style={{display:'flex'}}>
        <Button icon={<IconArrowLeft />} onClick={() => navigate(-1)}> {i18n("SampleMetadataImportMapHeaders.back")}</Button>
        <Button onClick={onSubmit} style={{marginLeft:'auto'}}>
          {i18n("SampleMetadataImportMapHeaders.next")}
          <IconArrowRight />
        </Button>
      </div>
    </SampleMetadataImportWizard>
  );
}