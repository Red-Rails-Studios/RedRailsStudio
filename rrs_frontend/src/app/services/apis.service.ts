import { Injectable } from '@angular/core';
import { TrainsInfo } from '../models/trainsInfo.model';
import { Resources } from '../models/resources.model';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';


@Injectable({
  providedIn: 'root' //Für die ganze anwendung erreichbar
})
export class APISService {
  private apiUrl = 'http://localhost:8080'; // URL to web api
  trainInfo: TrainsInfo = {
  total_trains: 0,
  total_passangers: 0,
  total_railways: 0
};


  constructor(private http: HttpClient) {
  }

  postNewPlayer(sessionName: string, playerName: string){
    return this.http.post(`http://localhost:8080/session/${sessionName}/${playerName}`, null);
  }

  getTotalTrains(sessionName: string, playerUid: string): Observable<any> {
  return this.http.get(`http://localhost:8080/session/${sessionName}/player/${playerUid}/train/`);
    //.get<TrainsInfo>(`${this.apiUrl}/getTrainInfo?playerId=${this.playerId}`).pipe(map((info: TrainsInfo) => info.total_trains));
  }

  getResources(sessionName: string, playerUid: string): Observable<any> {
  return this.http.get<Resources>(`http://localhost:8080/session/${sessionName}/player/${playerUid}/resource/`)
    //.get<Resources>(`${this.apiUrl}/getResources?playerId=${this.playerId}`).pipe(map((resources: Resources) => resources.DB_coin));
  }

  buyTrain(sessionName: string, playerUid: string) {
    return this.http.post(`http://localhost:8080/session/${sessionName}/player/${playerUid}/train`, null);
    //return this.http.post(`${this.apiUrl}/session?sessionName=${sessionName}playerId=${playerUid}/train`, null).subscribe((result: any) => console.log(result))
  }

  startSession(sessionName: string): Observable<any> {
    return this.http.patch(`http://localhost:8080/session/${sessionName}/start`, null)
    //return this.http.patch(`${this.apiUrl}/session?sessionName=${sessionName}`, null);
  }

  createSession(sessionName: string): Observable<any> {
    return this.http.post(`http://localhost:8080/session/${sessionName}`, null);
    //return this.http.post(`${this.apiUrl}/session?sessionName=${sessionName}`, null);
  }

  killSession(sessionName: string): Observable<any> {
    return this.http.patch(`http://localhost:8080/session/${sessionName}/kill`, null)
  }

}
