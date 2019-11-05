import React, { useEffect, useState } from "react";
import { Avatar, Switch, Table, Typography } from "antd";
import { createProjectLink } from "../../../utilities/link-utilities";
import {
  addAssociatedProject,
  getAssociatedProjects,
  removeAssociatedProject
} from "../../../apis/projects/projects";
import { TextFilter } from "../../../components/Tables/fitlers";
import { createListFilterByUniqueAttribute } from "../../../components/Tables/filter-utilities";
import { SPACE_MD } from "../../../styles/spacing";
import { getI18N } from "../../../utilities/i18n-utilties";

const { Text } = Typography;

export default function ViewAssociatedProjects() {
  const [projects, setProjects] = useState([]);
  const [organismFilters, setOrganismFilters] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getAssociatedProjects(window.project.id).then(data => {
      setProjects(data.associatedProjectList);
      setOrganismFilters(
        createListFilterByUniqueAttribute({
          list: data.associatedProjectList,
          attr: "organism"
        })
      );
      setLoading(false);
    });
  }, [getAssociatedProjects]);

  function updateProject(checked, project) {
    setLoading(true);
    let promise;
    if (checked) {
      promise = addAssociatedProject(window.project.id, project.id);
    } else {
      promise = removeAssociatedProject(window.project.id, project.id);
    }
    promise.then(() => {
      project.associated = checked;
      setProjects([...projects]);
      setLoading(false);
    });
  }

  const columns = [
    {
      key: "project",
      render(project) {
        return (
          <>
            <span style={{ marginRight: SPACE_MD }}>
              {window.PAGE.permissions ? (
                <Switch
                  checked={project.associated}
                  loading={project.updating}
                  onClick={checked => updateProject(checked, project)}
                />
              ) : (
                <Avatar icon="folder" />
              )}
            </span>{" "}
            {createProjectLink(project)}
          </>
        );
      },
      title: getI18N("ViewAssociatedProjects.ProjectHeader"),
      filterDropdown(props) {
        return <TextFilter {...props} />;
      },
      onFilter: (value, project) => {
        return project.label
          .toString()
          .toLowerCase()
          .includes(value.toLowerCase());
      },
      sorter: (a, b) => ("" + a.label).localeCompare("" + b.label)
    },
    {
      key: "organism",
      dataIndex: "organism",
      align: "right",
      title: "Organism",
      render(text) {
        return <Text type="secondary">{text}</Text>;
      },
      filters: organismFilters,
      onFilter: (value, record) =>
        record.organism === value || (!record.organism && value === "unknown"),
      sorter: (a, b) => ("" + a.organism).localeCompare("" + b.organism)
    }
  ];

  return (
    <Table
      bordered
      rowKey="id"
      loading={loading}
      columns={columns}
      dataSource={projects}
    />
  );
}
