import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BoekService } from '../services/boek.service';

@Component({
  selector: 'app-admin-dashboard',
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard implements OnInit {
  boekService = inject(BoekService);

  totaalBoeken = 0;
  recenteLeningen: any[] = []; 

  // tijdelijke data
  uitleenRatio: number = 68;
  tijdigeRetour: number = 92;
  uniekeLeners: number = 345;
  nieuweReviews: number = 12;

  ngOnInit(): void {
    // 1. Totaal aantal boeken ophalen
    this.boekService.getBoekenPaged(0, 1).subscribe({
      next: (response) => {
        this.totaalBoeken = response.totalElements;
      },
      error: (error: Error) => {
        console.error('Fout bij het ophalen van boeken:', error);
      },
    });

    // 2. Actieve leningen ophalen, sorteren en beperken tot max 5
    this.boekService.getAlleActieveLeningenSysteem().subscribe({
      next: (response) => {
        
        // STAP A: Sorteer de leningen zodat de kortst bijzijnde inleverdatum bovenaan staat
        const gesorteerdeLeningen = response.sort((a, b) => {
          const datumA = new Date(a.inleverDatum).getTime();
          const datumB = new Date(b.inleverDatum).getTime();
          
          return datumA - datumB; // Oplopend: kleinste (vroegste) datum eerst
        });

        // STAP B: Neem alleen de bovenste 5 van de gesorteerde lijst
        this.recenteLeningen = gesorteerdeLeningen.slice(0, 5);
      },
      error: (error: Error) => {
        console.error('Fout bij het ophalen van actieve leningen:', error);
      }
    });
  }
}