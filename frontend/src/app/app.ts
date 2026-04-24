import { Component, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { Navbar } from './navbar/navbar';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navbar],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private router = inject(Router);


  // http = inject(HttpClient);
  // status = 'Wacht op input...';

  // saveData(ingevoerdeNaam: string) {

  //   // this.http.post('https://frontend.localhost/api/save', { naam: ingevoerdeNaam }).subscribe({
  //    this.http.post('https://frontend.localhost/api/save', { naam: ingevoerdeNaam }).subscribe({
  //     next: (result) => (this.status = 'Gelukt! Opgeslagen in MySQL.'),
  //     error: (err) => {
  //       this.status = 'Fout bij opslaan!';
  //       console.log(err);
  //     },
  //   });
  // }
}
