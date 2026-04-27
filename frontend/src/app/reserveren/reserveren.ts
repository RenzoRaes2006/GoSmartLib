import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { LeenMandjeService } from '../services/leen-mandje.service';
import { BoekService } from '../services/boek.service';
import { Boek } from '../models/Boek';
import { InstellingService } from '../services/instelling-service';

@Component({
  selector: 'app-reserveren',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './reserveren.html',
  styleUrls: ['./reserveren.css'],
})
export class ReserverenComponent implements OnInit {
  mandjeService = inject(LeenMandjeService);
  boekService = inject(BoekService);
  instellingService = inject(InstellingService);

  boekenInMandje: Boek[] = [];

  // simulate user login
  ingelogdeGebruikerId = 1;

  // signal to keep track of active loans
  actieveLeningen = signal<number>(0);

  isBezig = false;
  statusMelding = '';
  isSucces = false;
  maxLeenLimiet = signal<number>(0);

  ngOnInit() {
    this.mandjeService.mandje$.subscribe((boeken) => {
      this.boekenInMandje = boeken;
    });

    // fetch active loans to calculate the limit
    this.boekService.getActieveLeningen(this.ingelogdeGebruikerId).subscribe({
      next: (leningen) => {
        this.actieveLeningen.set(leningen.length);
      },
      error: (err) => console.error('Fout bij ophalen teller', err),
    });
    this.instellingService.getLeenLimiet().subscribe({
      next: (limiet) => this.maxLeenLimiet.set(limiet),
      error: (err) => console.error('Kon limiet niet ophalen', err),
    });
  }

  verwijderBoek(boekId: number) {
    this.mandjeService.verwijderBoek(boekId);
  }

  bevestigReservering() {
    if (this.boekenInMandje.length === 0) {
      this.statusMelding = 'Je mandje is leeg!';
      this.isSucces = false;
      return;
    }

    this.isBezig = true;
    this.statusMelding = 'Je aanvraag wordt doorgestuurd naar de bibliotheek...';

    let aantalKlaar = 0;
    let heeftFouten = false;

    this.boekenInMandje.forEach((boek) => {
      this.boekService.leenBoek(this.ingelogdeGebruikerId, boek.id!).subscribe({
        next: () => {
          aantalKlaar++;
          this.checkOfWeKlaarZijn(aantalKlaar, heeftFouten);
        },
        error: (err) => {
          console.error('Fout bij', boek.titel, err);
          heeftFouten = true;
          aantalKlaar++;
          this.checkOfWeKlaarZijn(aantalKlaar, heeftFouten);
        },
      });
    });
  }

  checkOfWeKlaarZijn(aantalKlaar: number, heeftFouten: boolean) {
    if (aantalKlaar === this.boekenInMandje.length) {
      this.isBezig = false;
      if (heeftFouten) {
        this.statusMelding = 'Er ging iets mis. Mogelijk heb je je limiet bereikt.';
        this.isSucces = false;
      } else {
        this.statusMelding = 'Aanvraag succesvol verstuurd! De bibbeheerder zet je boeken klaar.';
        this.isSucces = true;
        this.mandjeService.maakMandjeLeeg();

        // update the counter visually after a successful request
        this.actieveLeningen.update((count) => count + aantalKlaar);
      }
    }
  }
}
