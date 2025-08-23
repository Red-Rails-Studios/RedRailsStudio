import { Component } from '@angular/core';
import { MapComponent } from './components/map/map.component';
import { SettingsComponent } from './components/settings/settings.component';
import { ResoursesComponent } from './components/resourses/resourses.component';
import { APISService } from './services/apis.service';
import { CounterComponent } from './counter/counter.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [MapComponent, ResoursesComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'rrs_frontend';
  sessionId = new Date().getTime().toString();
  playerName: string = '';

  constructor(private apiService: APISService) {


    this.apiService.postNewPlayer('testsession', 'Player1').subscribe((response)=> {console.log('New Player')},
    (error)=> {console.log('Error player')}); //Startet session; //erstellt eine Player

  }

  onCreateSession() {
    this.apiService.createSession(this.sessionId).subscribe((response) => {console.log('Session created')},
    (error) => {console.log('Error creating session')},
     );  //erstellt ein Session
  }

  onStartSession() {
    this.apiService.startSession('testsession').subscribe((response) => {console.log('Session started')},
    (error) => {console.log('Error starting session')},
    );  //startet ein Session
  }

  onKillSession(){
    this.apiService.killSession(this.sessionId).subscribe((response) => {console.log('Session killed')},
    (error) => {console.log('Error killing session')},
  );  //killt ein Session
  }

  onJoinSession(playerName?: string) {
  const name = playerName || this.playerName || 'Player2';
  this.apiService.postNewPlayer('testsession', name).subscribe(
    (response) => { console.log('New Player joined the Session'); },
    (error) => { console.log('Error joining session'); }
  );

 }
onSetPlayerName(name: string) {
  this.playerName = name;
 //TODO: Logik fur namen eingabe verbessern.
}
}
