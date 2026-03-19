import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BoekService } from '../services/boek.service';
import { Boek } from '../models/Boek';
import { LeeslijstService } from '../services/leeslijstService';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-home-page-leerling',
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './home-page-leerling.html',
  styleUrl: './home-page-leerling.css',
})
export class HomePageLeerling implements OnInit {
  private boekService = inject(BoekService);
  private leeslijstService = inject(LeeslijstService);

  // Gebruikersinfo & Gamification
  naam = signal<string>('Emma');
  gebruikerId = 1; // Tijdelijk hardcoded, pas aan naar ingelogde gebruiker

  gelezenBoeken = signal<number>(7);
  leesDoel = signal<number>(10);

  // NIEUW: Variabelen voor de pop-up (Modal)
  isEditModalOpen = signal<boolean>(false);
  nieuwLeesdoel = signal<number>(10);

  // Echte data uit database
  uitgelichteBoeken = signal<Boek[]>([]);
  leeslijsten = signal<any[]>([]);

  // Dummy leningen
  leningen = signal<any[]>([
    { id: 101, titel: 'Java voor Dummies', resterendeDagen: 10 },
    { id: 102, titel: 'Harry Potter en de Steen der Wijzen', resterendeDagen: 30 },
    { id: 103, titel: 'Netwerken Basis', resterendeDagen: 2 }, // Dringend!
  ]);

  // Automatisch sorteren van leningen
  gesorteerdeLeningen = computed(() => {
    return [...this.leningen()].sort((a, b) => a.resterendeDagen - b.resterendeDagen);
  });

  ngOnInit(): void {
    this.boekService.getPopulaireBoeken().subscribe({
      next: (data) => {
        this.uitgelichteBoeken.set(data.slice(0, 5));
      },
      error: (err) => console.error('Kon uitgelichte boeken niet laden:', err)
    });

    // 2. Haal mappen/leeslijsten op voor deze gebruiker (maximaal 3 voor op de homepage)
    this.leeslijstService.getMappenVanGebruiker(this.gebruikerId).subscribe({
      next: (mappen) => {
        this.leeslijsten.set(mappen.slice(0, 3));
      },
      error: (err) => console.error('Kon leeslijsten/mappen niet laden:', err)
    });
  }

  // Berekent breedte van de progress bar
  get progressPercentage(): number {
    return Math.min((this.gelezenBoeken() / this.leesDoel()) * 100, 100);
  }

  openEditModal() {
    this.nieuwLeesdoel.set(this.leesDoel()); // Zet het invulveldje op het huidige doel
    this.isEditModalOpen.set(true);
  }

  sluitEditModal() {
    this.isEditModalOpen.set(false);
  }

  slaLeesdoelOp() {
    // 1. Pas het visueel direct aan op de website
    this.leesDoel.set(this.nieuwLeesdoel());
    
    // 2. Sluit de pop-up
    this.isEditModalOpen.set(false);

    // LATER: Hier sturen we het nieuwe getal (this.leesDoel()) naar je Spring Boot backend!
    console.log("Nieuw leesdoel opgeslagen:", this.leesDoel());
  }
}

