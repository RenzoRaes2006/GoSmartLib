import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BoekService } from '../services/boek.service';

@Component({
  selector: 'app-leningen-beheerder',
  imports: [CommonModule, FormsModule],
  templateUrl: './leningen-beheerder.html',
  styleUrl: './leningen-beheerder.css',
})
export class LeningenBeheerder implements OnInit {
  private boekService = inject(BoekService);

  // signals to hold the data
  alleLeningen = signal<any[]>([]);
  zoekterm = signal<string>('');

  ngOnInit(): void {
    this.haalLeningenOp();
  }

  haalLeningenOp() {
    this.boekService.getAlleActieveLeningenSysteem().subscribe({
      next: (data) => {
        this.alleLeningen.set(data);
      },
      error: (err) => console.error('Error fetching system loans:', err)
    });
  }

  // computed signal to filter the list based on the search input
  gefilterdeLeningen = computed(() => {
    const term = this.zoekterm().toLowerCase();
    if (!term) return this.alleLeningen();

    return this.alleLeningen().filter(lening => {
      const boekTitel = lening.boek.titel.toLowerCase();
      const leerlingNaam = `${lening.gebruiker.voorNaam} ${lening.gebruiker.achternaam}`.toLowerCase();
      return boekTitel.includes(term) || leerlingNaam.includes(term);
    });
  });
}
