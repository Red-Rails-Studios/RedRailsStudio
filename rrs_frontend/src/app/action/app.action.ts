import { createAction, props } from "@ngrx/store";
import { Resources } from "../../models/resources.model";

export const updateResources = createAction(
  "[Resources] Update Resources",
  props<{ resources: Resources }>()
);