import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { Boek } from '../models/Boek';
import { BoekService } from '../services/boek.service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-catalogus',
  imports: [CommonModule, RouterLink],
  templateUrl: './catalogus.html',
  styleUrl: './catalogus.css',
  standalone: true,
})
export class Catalogus implements OnInit {

  boekenlijst = signal<Boek[]>([]);
  zoekTerm = signal<string>('');

  private boekService = inject(BoekService);

  ngOnInit(): void {
    this.laadBoeken();
  }

  private laadBoeken(term?: string): void {
    this.boekService.getBoeken(term).subscribe({
      next: (data) => {
        this.boekenlijst.set(data);
      },
      error: (err) => console.log('Fout bij ophalen boeken:', err),
    });
  }

  onSearch(term: string): void {
    this.zoekTerm.set(term);
    this.laadBoeken(term);
  }
}