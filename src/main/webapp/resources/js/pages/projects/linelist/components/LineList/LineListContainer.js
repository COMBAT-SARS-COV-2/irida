import React from "react";
import { connect } from "react-redux";
import { LineList } from "./LineList";
import { actions } from "../../reducers/templates";
import { actions as cartActions } from "../../../../../redux/reducers/cart";
import { actions as entryActions } from "../../reducers/entries";

/*
Default react-redux boiler plate to connect the current state of the
application to the component. When the state of the application gets
updated (in this case so far it is the loading state), the this connect
method is what triggers the updates.
 */

const mapStateToProps = state => ({
  initializing: state.fields.get("initializing"),
  error: state.fields.get("error"),
  fields: state.fields.get("fields"),
  entries: state.entries.get("entries"),
  templates: state.templates.get("templates"),
  current: state.templates.get("current"),
  modified: state.templates.get("modified"),
  saving: state.templates.get("saving"),
  saved: state.templates.get("saved")
});

const mapDispatchToProps = dispatch => ({
  tableModified: fields => dispatch(actions.tableModified(fields)),
  templateModified: fields => dispatch(actions.templateModified(fields)),
  useTemplate: index => dispatch(actions.use(index)),
  saveTemplate: (name, fields, id) =>
    dispatch(actions.saveTemplate(name, fields, id)),
  addSelectedToCart: samples => dispatch(cartActions.add(samples)),
  entryEdited: (entry, field) => dispatch(entryActions.edited(entry, field))
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(LineList);
