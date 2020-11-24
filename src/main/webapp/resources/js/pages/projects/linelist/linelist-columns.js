import React from "react";
import { setBaseUrl } from "../../../utilities/url-utilities";
import { formatInternationalizedDateTime } from "../../../utilities/date-utilities";

export function fieldRenderFactory(field) {
  return function fieldRender(text, entry) {
    switch (field.type) {
      case "name":
        return (
          <a
            href={setBaseUrl(
              `projects/${window.project.id}/samples/${entry.id}`
            )}
          >
            {text}
          </a>
        );
      case "date":
        return formatInternationalizedDateTime(text);
      default:
        return text;
    }
  };
}
