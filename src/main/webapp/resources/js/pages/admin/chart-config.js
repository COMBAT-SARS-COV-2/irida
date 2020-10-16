import merge from "lodash/merge";
import {
  chartTypes
} from "./statistics-constants";

const chartHeight = 800;

/*
   * Gets the config required for the chart
   * @param chartType - The type of chart (bar, column, line, or pie)
   * @param data - The data for the chart
   * @param statsType - The type of statistics (projects, analyses, samples, users)
   */
export function getChartConfiguration(chartType, data) {

  const customChartTypeConfig = {
    [chartTypes.BAR]: {
      xField: "value",
      yField: "key",
    },
    [chartTypes.PIE]: {
      appendPadding: 10,
      colorField: 'key',
      radius: 0.8,
      angleField: 'value',
      label: {
        content: ''
      },
    },
    [chartTypes.COLUMN]: {},
    [chartTypes.LINE]: {
      colorField: '',
    },
  };

  // The configuration required to display a chart
  const config = {
    data: data,
    padding: 'auto',
    xField: 'key',
    yField: 'value',
    width: "100%",
    height: chartHeight,
    meta: { key: { alias: '' }, value: { alias: '' } },
    label: {
      visible: Boolean(data),
      position: 'middle',
      adjustColor: true,
      style: { fill: '#0D0E68', fontSize: 12, fontWeight: 600, opacity: 0.3 },
    },
    colorField: 'key',
    legend: {
      visible: Boolean(data),
      position: 'bottom',
    },
  };

  return merge(config, customChartTypeConfig[chartType]);
}

export function getTinyChartConfiguration(data) {
  // Tiny chart requires just an array of values
  data = data.map(obj => obj.value);

  const config = {
    data: data,
    title: { visible: false},
    legend: {
      visible: false,
    },
    autoFit: true,
    height: 80,
    columnWidthRatio: 0.5
  }

  return config;
}