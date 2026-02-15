import { HttpClient } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  http = inject(HttpClient);
  status = 'Wacht op input...';

  saveData(ingevoerdeNaam: string) {
    this.http.post('http://frontend.localhost/api/save', { naam: ingevoerdeNaam }).subscribe({
      next: (result) => (this.status = 'Gelukt! Opgeslagen in MySQL.'),
      error: (err) => {
        this.status = 'Fout bij opslaan!';
        console.log(err);
      },
    });
  }
}
