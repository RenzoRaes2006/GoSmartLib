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
  naam = signal<string>('Jeroen');
  gebruikerId = 1; // Tijdelijk hardcoded, pas aan naar ingelogde gebruiker

  gelezenBoeken = signal<number>(0);
  leesDoel = signal<number>(10);

  // Variabelen voor de pop-up (Modal)
  isEditModalOpen = signal<boolean>(false);
  nieuwLeesdoel = signal<number>(10);

  // Echte data uit database
  uitgelichteBoeken = signal<Boek[]>([]);
  leeslijsten = signal<any[]>([]);

  // Leningen uit de database
  leningen = signal<any[]>([]);

  // Automatisch sorteren van leningen (minste dagen bovenaan)
  gesorteerdeLeningen = computed(() => {
    return [...this.leningen()].sort((a, b) => a.resterendeDagen - b.resterendeDagen);
  });

  ngOnInit(): void {
    // 1. Haal populaire boeken op
    this.boekService.getPopulaireBoeken().subscribe({
      next: (data) => {
        this.uitgelichteBoeken.set(data.slice(0, 5));
      },
      error: (err) => console.error('Kon uitgelichte boeken niet laden:', err)
    });

    // 2. Haal mappen/leeslijsten op voor deze gebruiker
    this.leeslijstService.getMappenVanGebruiker(this.gebruikerId).subscribe({
      next: (mappen) => {
        this.leeslijsten.set(mappen.slice(0, 3));
      },
      error: (err) => console.error('Kon leeslijsten/mappen niet laden:', err)
    });

    // 3. Haal de actieve leningen op
    this.haalLeningenOp();

    this.boekService.getLeengeschiedenis(this.gebruikerId).subscribe({
      next: (geschiedenis) => {
        // Tel alle leningen, behalve degene die door de bibbeheerder zijn geweigerd
        const geldigeLeningen = geschiedenis.filter(lening => lening.status !== 'GEWEIGERD');
        
        // Update de meter op het scherm!
        this.gelezenBoeken.set(geldigeLeningen.length);
      },
      error: (err) => console.error('Kon leengeschiedenis niet laden voor teller:', err)
    });
  }

  haalLeningenOp() {
    this.boekService.getActieveLeningen(this.gebruikerId).subscribe({
      next: (data) => {
        const verwerkteLeningen = data.map((uitlening) => {

          // Check of de lening nog goedgekeurd moet worden
          if (uitlening.status === 'AANGEVRAAGD' || !uitlening.inleverDatum) {
            return {
              id: uitlening.id,
              titel: uitlening.boek.titel,
              resterendeDagen: 999, // Zet ze helemaal onderaan in de sortering
              isAanvraag: true
            };
          }

          // Datum berekening voor goedgekeurde leningen
          const inleverDatum = new Date(uitlening.inleverDatum);
          const vandaag = new Date();

          // Zet beide op middernacht om pure dagen te vergelijken
          inleverDatum.setHours(0, 0, 0, 0);
          vandaag.setHours(0, 0, 0, 0);

          const verschilTijd = inleverDatum.getTime() - vandaag.getTime();
          const verschilDagen = Math.ceil(verschilTijd / (1000 * 3600 * 24));

          return {
            id: uitlening.id,
            titel: uitlening.boek.titel,
            resterendeDagen: verschilDagen,
            isAanvraag: false
          };
        });

        this.leningen.set(verwerkteLeningen);
      },
      error: (err) => console.error('Kon actieve leningen niet ophalen:', err)
    });
  }

  // Berekent breedte van de progress bar
  get progressPercentage(): number {
    return Math.min((this.gelezenBoeken() / this.leesDoel()) * 100, 100);
  }

  openEditModal() {
    this.nieuwLeesdoel.set(this.leesDoel());
    this.isEditModalOpen.set(true);
  }

  sluitEditModal() {
    this.isEditModalOpen.set(false);
  }

  slaLeesdoelOp() {
    // 1. Haal de ingetypte waarde op
    let veiligDoel = this.nieuwLeesdoel();

    // 2. Beveilig de grenzen (min 1, max 100)
    if (veiligDoel < 1) {
      veiligDoel = 1;
    } else if (veiligDoel > 10) {
      veiligDoel = 10; // Je kunt hier 10 veranderen in wat jij een logisch maximum vindt
    }

    // 3. Sla de veilige waarde op
    this.leesDoel.set(veiligDoel);

    // 4. Sluit de pop-up
    this.isEditModalOpen.set(false);
  }

  verwijder(id: number) {
    if (confirm('Weet je zeker dat je deze lening volledig wil verwijderen?')) {
      this.boekService.verwijderLening(id).subscribe({
        next: (bericht) => {
          alert(bericht);
          // refresh the list after deletion (call your fetch function here)
          this.haalLeningenOp(); // of haalAanvragenOp()
        },
        error: (err) => alert('Fout bij verwijderen: ' + err.message)
      });
    }
  }
}