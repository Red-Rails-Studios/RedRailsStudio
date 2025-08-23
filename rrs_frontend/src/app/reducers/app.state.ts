import { Resources} from '../models/resources.model';
import { TrainsInfo } from '../models/trainsInfo.model';
import { iUser } from '../models/user.model';
import { createFeatureSelector, createSelector } from '@ngrx/store';

export interface AppState {
    resources : Resources;
    trainsInfo: TrainsInfo;
    user: iUser;
}