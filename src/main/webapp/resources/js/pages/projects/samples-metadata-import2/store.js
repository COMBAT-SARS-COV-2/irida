import { configureStore } from "@reduxjs/toolkit";
import { metadataImportApi } from "../../../apis/metadata/metadata-import";
import { rootReducer } from "./services/rootReducer";

/*
Redux Store for sample metadata.
For more information on redux stores see: https://redux.js.org/tutorials/fundamentals/part-4-store
 */
export default configureStore({
  reducer: {
    reducer: rootReducer,
    [metadataImportApi.reducerPath]: metadataImportApi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(metadataImportApi.middleware),
});
