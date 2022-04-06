import React from "react";
import { render } from "react-dom";
import ProjectSamples from "./components/ProjectSamples";
import { configureStore } from "@reduxjs/toolkit";
import { setupListeners } from "@reduxjs/toolkit/query";
import { Provider } from "react-redux";
import { samplesApi } from "./services/samples";
import { associatedProjectsApi } from "./../../../apis/projects/associated-projects";
import samplesReducer from "./sample.store";

export const store = configureStore({
  reducer: {
    samples: samplesReducer,
    [samplesApi.reducerPath]: samplesApi.reducer,
    [associatedProjectsApi.reducerPath]: associatedProjectsApi.reducer
  },
  middleware: getDefaultMiddleware =>
    getDefaultMiddleware().concat(
      samplesApi.middleware,
      associatedProjectsApi.middleware
    ),
  devTools: process.env.NODE_ENV !== "production"
});
setupListeners(store.dispatch);

render(
  <Provider store={store}>
    <ProjectSamples />
  </Provider>,
  document.getElementById("root")
);
