import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { LeeslijstService } from '../services/leeslijstService';

@Component({
  selector: 'app-leeslijst',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './leeslijst.html',
  styleUrl: './leeslijst.css',
})
export class Leeslijst implements OnInit {
  leeslijstService = inject(LeeslijstService);
  mappen: any[] = [];
  geselecteerdeMap: any = null;

  ngOnInit(): void {
    this.haalMappenOp();
  }

  haalMappenOp() {
    const gebruikerId = 1;
    this.leeslijstService.getMappenVanGebruiker(gebruikerId).subscribe({
      next: (data) => {
        let gefilterdeMappen = data.filter(
          (map: any) => map.boeken.length > 0 || map.naam.toLowerCase() === 'favorieten',
        );

        const heeftFavorieten = gefilterdeMappen.some(
          (m: any) => m.naam.toLowerCase() === 'favorieten',
        );
        if (!heeftFavorieten) {
          gefilterdeMappen.unshift({ id: 'fav', naam: 'Favorieten', boeken: [] });
        }

        this.mappen = gefilterdeMappen;
      },
      error: (err) => console.error('Fout bij ophalen mappen', err),
    });
  }

  openMap(map: any) {
    this.geselecteerdeMap = map;
  }

  // Terugknop naar het album-overzicht
  terugNaarOverzicht() {
    this.geselecteerdeMap = null;
    this.haalMappenOp();
  }

  // Verwijder boek
  verwijderBoek(boekId: number, event: Event) {
    event.stopPropagation();

    if (!this.geselecteerdeMap) return;

    const mapNaam = this.geselecteerdeMap.naam;
    const gebruikerId = 1;

    this.leeslijstService.verwijderBoekUitMap(gebruikerId, mapNaam, boekId).subscribe({
      next: () => {
        this.geselecteerdeMap.boeken = this.geselecteerdeMap.boeken.filter(
          (b: any) => b.id !== boekId,
        );
        if (this.geselecteerdeMap.boeken.length === 0) {
          this.terugNaarOverzicht();
        }
      },
      error: (err) => console.error('Fout bij verwijderen', err),
    });
  }
}
