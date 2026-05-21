import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BoekService } from '../services/boek.service';
import { Boek } from '../models/Boek';
import { LeeslijstService } from '../services/leeslijstService';
import { FormsModule } from '@angular/forms';
import { StatistiekService } from '../services/statistiek.service';
import { AuthService } from '../services/AuthService';

@Component({
  selector: 'app-home-page-leerling',
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './home-page.html',
  styleUrl: './home-page.css',
})
export class HomePage implements OnInit {
  private boekService = inject(BoekService);
  private leeslijstService = inject(LeeslijstService);
  private statistiekService = inject(StatistiekService);
  private authService = inject(AuthService);

  // Gebruikersinfo & Gamification
  naam = computed(() => this.authService.currentUser()?.name ?? '');
  schoolId = '12345'; // Tijdelijk hardcoded — komt later uit AuthService/Smartschool

  gelezenBoeken = signal<number>(0);
  leesDoel = signal<number>(10);
  aantalActieveLeningen = signal<number>(0);
  aantalTeLaat = signal<number>(0);

  // Variabelen voor de pop-up (Modal)
  isEditModalOpen = signal<boolean>(false);
  nieuwLeesdoel = signal<number>(10);

  // Echte data uit database
  uitgelichteBoeken = signal<Boek[]>([]);
  leeslijsten = signal<any[]>([]);

  // Leningen uit de database
  leningen = signal<any[]>([]);

  // Variabelen voor de Verleng-Modal
  isVerlengModalOpen = signal<boolean>(false);
  teVerlengenLeningId = signal<number | null>(null);
  teVerlengenBoekTitel = signal<string>('');
  verlengFout = signal<string>('');
  isVerlengenBezig = signal<boolean>(false);

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
      error: (err) => console.error('Kon uitgelichte boeken niet laden:', err),
    });

    // 2. Haal mappen/leeslijsten op voor deze gebruiker
    this.leeslijstService.getMijnMappen().subscribe({
      next: (mappen) => {
        this.leeslijsten.set(mappen.slice(0, 3));
      },
      error: (err) => console.error('Kon leeslijsten/mappen niet laden:', err),
    });

    // 3. Haal de actieve leningen op
    this.haalLeningenOp();

    this.statistiekService.getLeerlingStatistieken(this.schoolId).subscribe({
      next: (stats) => {
        this.gelezenBoeken.set(stats.gelezenBoeken ?? 0);
        this.leesDoel.set(stats.leesDoel ?? 10);
        this.aantalActieveLeningen.set(stats.aantalActieveLeningen ?? 0);
        this.aantalTeLaat.set(stats.aantalTeLaat ?? 0);
      },
      error: (err) => console.error('Kon leerling-statistieken niet laden:', err),
    });
  }

  haalLeningenOp() {
    this.boekService.getMijnActieveLeningen().subscribe({
      next: (data) => {
        const verwerkteLeningen = data.map((uitlening) => {
          // 1. Check of de lening een gloednieuwe aanvraag is (nog niet uitgeleend)
          if (uitlening.status === 'AANGEVRAAGD' || !uitlening.inleverDatum) {
            return {
              id: uitlening.id,
              titel: uitlening.boek.titel,
              resterendeDagen: 0, // Geen dagen want de termijn is nog niet gestart
              isAanvraag: true,
              isVerlengd: uitlening.verlengd || false,
              isVerlengingAangevraagd: false, // Een nieuwe lening kan geen verlenging zijn
            };
          }

          // 2. Datum berekening voor actieve leningen (inclusief leningen waarvan verlenging is aangevraagd)
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
            isAanvraag: false,
            isVerlengd: uitlening.verlengd || false,
            isVerlengingAangevraagd: uitlening.status === 'VERLENGING_AANGEVRAAGD',
          };
        });

        this.leningen.set(verwerkteLeningen);
      },
      error: (err) => console.error('Kon actieve leningen niet ophalen:', err),
    });
  }

  // Berekent breedte van de progress bar
  get progressPercentage(): number {
    if (this.leesDoel() === 0) return 0;
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
      veiligDoel = 10;
    }

    // 3. Sla de veilige waarde op
    this.leesDoel.set(veiligDoel);

    // 4. Sluit de pop-up
    this.isEditModalOpen.set(false);
  }

  openVerlengModal(leningId: number) {
    const lening = this.leningen().find((l) => l.id === leningId);
    if (lening) {
      this.teVerlengenLeningId.set(lening.id);
      this.teVerlengenBoekTitel.set(lening.titel);
      this.verlengFout.set('');
      this.isVerlengModalOpen.set(true);
    }
  }

  sluitVerlengModal() {
    this.isVerlengModalOpen.set(false);
    this.teVerlengenLeningId.set(null);
    this.teVerlengenBoekTitel.set('');
    this.verlengFout.set('');
  }

  bevestigVerlengen() {
    const leningId = this.teVerlengenLeningId();
    if (leningId === null) return;

    this.isVerlengenBezig.set(true);
    this.verlengFout.set('');

    this.boekService.verlengLening(leningId).subscribe({
      next: () => {
        this.isVerlengenBezig.set(false);
        this.sluitVerlengModal();
        this.haalLeningenOp();
      },
      error: (err) => {
        this.isVerlengenBezig.set(false);
        console.error('Fout bij verlengen:', err);
        if (err && err.name === 'HttpErrorResponse' && err.status === 0) {
          this.verlengFout.set('Kan de server niet bereiken. Controleer of de backend draait.');
        } else {
          this.verlengFout.set(err.error || 'Er ging iets mis bij het verlengen.');
        }
      },
    });
  }
}
