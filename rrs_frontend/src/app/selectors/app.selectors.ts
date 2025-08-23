import { createFeatureSelector, createSelector } from "@ngrx/store";
import { AppState } from "../reducers/app.state";
import { Resources } from "../../models/resources.model";

export const selectResourcesState = createFeatureSelector<AppState, Resources>('resources');

export const selectResources = createSelector(
  selectAppState,
  (state: AppState) => state.resources
);