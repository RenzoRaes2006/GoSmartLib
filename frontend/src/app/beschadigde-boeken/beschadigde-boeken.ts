import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';

interface BoekSchade {
  id: number;
  boekTitel: string;
  boekIsbn: string;
  leerlingSmartschoolId: string;
  leerlingNaam: string | null;
  leerlingKlas: string | null;
  opmerking: string | null;
  gemeldOp: string;
}

@Component({
  selector: 'app-beschadigde-boeken',
  standalone: true,
  imports: [CommonModule, DatePipe, RouterLink],
  templateUrl: './beschadigde-boeken.html',
  styleUrl: './beschadigde-boeken.css',
})
export class BeschadigdeBoeken implements OnInit {
  private http = inject(HttpClient);
  private apiBase =
    window.location.hostname === 'localhost' ? 'http://localhost:8081/api' : '/api';

  schades = signal<BoekSchade[]>([]);
  laden = signal(true);
  fout = signal('');

  ngOnInit(): void {
    this.http.get<BoekSchade[]>(`${this.apiBase}/schade`).subscribe({
      next: (data) => {
        this.schades.set(data ?? []);
        this.laden.set(false);
      },
      error: () => {
        this.fout.set('Kon de lijst van beschadigde boeken niet ophalen.');
        this.laden.set(false);
      },
    });
  }
}
