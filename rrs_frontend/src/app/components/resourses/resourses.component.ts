import { Component, OnInit } from '@angular/core';
import { APISService } from '../../services/apis.service';
import { CommonModule } from '@angular/common';
import { Store, select } from '@ngrx/store';
import { updateResources } from '../action/app.action';
import { AppState } from '../reducers/app.state';
import { Observable } from 'rxjs';
import { Resources } from '../../models/resources.model';
import { selectResources } from '../selectors/app.selectors';

@Component({
  selector: 'app-resourses',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './resourses.component.html',
  styleUrls: ['./resourses.component.scss']
})
export class ResoursesComponent implements OnInit {
  displayedValue: number = 0; // Display value ist die ausgegebene wert auf den bild schirm
  coinZahl: number = 0; // Anzahl der Coins
  resources$!: Observable<Resources>; // Observable for resources

  constructor(private apiService: APISService, private store: Store<AppState>) {
    this.resources$ = this.store.pipe(select(selectResources));
  }
  ngOnInit(): void {
  //   setInterval(() => {
  //     this.apiService.getTotalTrains('testsession', 'Player1').subscribe((totalTrains: number) => {
  //       this.displayedValue = totalTrains;
  //     });
  //     this.apiService.getResources('testsession', 'Player1').subscribe((coins: number) => { //holt DB coins vom backend service
  //       this.coinZahl = coins;
  //     });
  //   }, 10000)
   }

  StartSession() {
    // Example for creating a session
    this.apiService.createSession (new Date().getTime().toString()).subscribe({
      next: (res) => console.log('Session created', res),
      error: (err) => console.error('Session creation failed', err)
    });
    this.apiService.postNewPlayer('testsession', 'Player1').subscribe; //erstellt eine Player
    this.apiService.startSession('testsession').subscribe; //Startet session
  }
  //TODO: Es sollte nicht mehrmals URLS aufeinmal aufgerufen werden, sondern nur einmal.
  //TODO: Es sollte Anfangs Menu erstellt die ein auswahl fur singel und Multiplayer gibt.

   updtateResources(sessionName: string, playerUid: string): void {
    this.apiService.getResources(sessionName, playerUid).subscribe({
      next: (resources: Resources) => {
        this.store.dispatch(updateResources({ resources })); // Dispatch the action to update resources in the store
      },
      error: (err) => {
        console.error('Failed to fetch resources', err);}
      });
   }
}
