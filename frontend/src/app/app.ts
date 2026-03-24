import { Component, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { Navbar } from './navbar/navbar';
import { Footer } from './footer/footer';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navbar, Footer],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private router = inject(Router);

  toonFooter() {
    return !this.router.url.startsWith('/catalogus') && !this.router.url.startsWith('/boek-beheer');
  }
  // http = inject(HttpClient);
  // status = 'Wacht op input...';

  // saveData(ingevoerdeNaam: string) {

  //   // this.http.post('http://frontend.localhost/api/save', { naam: ingevoerdeNaam }).subscribe({
  //    this.http.post('http://frontend.localhost/api/save', { naam: ingevoerdeNaam }).subscribe({
  //     next: (result) => (this.status = 'Gelukt! Opgeslagen in MySQL.'),
  //     error: (err) => {
  //       this.status = 'Fout bij opslaan!';
  //       console.log(err);
  //     },
  //   });
  // }
}
