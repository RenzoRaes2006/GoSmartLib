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
  private boekenlijst = signal<Boek[]>([]);
  zoekTerm = signal<string>('');

  private boekService = inject(BoekService);


  get gefilterdeBoeken() {
    const term = this.zoekTerm().toLowerCase().trim();
    if (!term) return this.boekenlijst();

    return this.boekenlijst().filter(boek =>
      boek.titel.toLowerCase().includes(term) ||
      (boek.isbn && boek.isbn.includes(term)) ||
      boek.auteur.toLowerCase().includes(term)
    );
  }

  //op locatie sorteren
  ngOnInit(): void {
    this.boekService.getBoeken().subscribe({
      next: (data) => {
        const gesorteerd = data.sort((a, b) => a.locatie.localeCompare(b.locatie));
        this.boekenlijst.set(gesorteerd);
      },
      error: (err) => console.log('Fout bij ophalen boeken:', err),
    });
  }

  onSearch(term: string): void {
    this.zoekTerm.set(term);
  }
}
