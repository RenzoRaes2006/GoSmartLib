import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BoekService } from '../services/boek.service';
import { InstellingService } from '../services/instelling-service';
import { StatistiekService } from '../services/statistiek.service';

@Component({
  selector: 'app-beheerder-dashboard',
  imports: [CommonModule, RouterLink],
  templateUrl: './beheerder-dashboard.html',
  styleUrl: './beheerder-dashboard.css',
})
export class BeheerderDashboard implements OnInit {
  boekService = inject(BoekService);
  huidigeLimiet = signal<number>(0);
  huidigeTermijn = signal<number>(14);
  instellingService = inject(InstellingService);
  statistiekService = inject(StatistiekService);
  recenteLeningen: any[] = [];

  toastActief = signal<boolean>(false);
  toastBericht = signal<string>('');
  toastType = signal<'succes' | 'fout'>('succes');

  toonToast(bericht: string, type: 'succes' | 'fout') {
    this.toastBericht.set(bericht);
    this.toastType.set(type);
    this.toastActief.set(true);

    setTimeout(() => {
      this.toastActief.set(false);
    }, 3000);
  }

  percentageUitgeleend = signal<number>(0);
  teLaatIngeleverd = signal<number>(0);
  populairsteGenre = signal<string>('—');
  uitleningenDezeMaand = signal<number>(0);

  ngOnInit(): void {
    this.laadLimiet();
    this.laadTermijn();
    this.laadStatistieken();
    this.boekService.getAlleActieveLeningenSysteem().subscribe({
      next: (response) => {
        const gesorteerdeLeningen = response.sort((a, b) => {
          const datumA = new Date(a.inleverDatum).getTime();
          const datumB = new Date(b.inleverDatum).getTime();

          return datumA - datumB;
        });

        this.recenteLeningen = gesorteerdeLeningen.slice(0, 5);
      },
      error: (error: Error) => {
        console.error('Fout bij het ophalen van actieve leningen:', error);
      },
    });
  }
  laadStatistieken() {
    this.statistiekService.getAdminStatistieken().subscribe({
      next: (stats) => {
        this.percentageUitgeleend.set(stats.percentageUitgeleend ?? 0);
        this.teLaatIngeleverd.set(stats.teLaatIngeleverd ?? 0);
        this.populairsteGenre.set(stats.populairsteGenre ?? '—');
        this.uitleningenDezeMaand.set(stats.uitleningenDezeMaand ?? 0);
      },
      error: (err) => console.error('Fout bij ophalen statistieken:', err),
    });
  }

  laadLimiet() {
    this.instellingService.getLeenLimiet().subscribe({
      next: (limiet) => this.huidigeLimiet.set(limiet),
      error: (err) => console.error('Fout bij ophalen van limiet:', err),
    });
  }
  opslaanLimiet(nieuweWaarde: string) {
    const limiet = parseInt(nieuweWaarde, 10);
    if (limiet >= 0) {
      this.instellingService.updateLeenLimiet(limiet).subscribe({
        next: (msg) => {
          this.toonToast('Limiet succesvol aangepast!', 'succes');
          this.huidigeLimiet.set(limiet);
        },
        error: (err) => {
          console.error('CRITIEKE FOUT BIJ OPSLAAN:', err);
          this.toonToast('Fout bij opslaan! Controleer de verbinding.', 'fout');
        },
      });
    } else {
      this.toonToast('De limiet mag niet negatief zijn.', 'fout');
    }
  }

  laadTermijn() {
    this.instellingService.getInlevertermijn().subscribe({
      next: (termijn) => this.huidigeTermijn.set(termijn),
      error: (err) => console.error('Fout bij ophalen van inlevertermijn:', err),
    });
  }

  opslaanTermijn(nieuweWaarde: string) {
    const termijn = parseInt(nieuweWaarde, 10);
    if (termijn >= 1 && termijn <= 365) {
      this.instellingService.updateInlevertermijn(termijn).subscribe({
        next: (msg) => {
          this.toonToast('Inlevertermijn succesvol aangepast!', 'succes');
          this.huidigeTermijn.set(termijn);
        },
        error: (err) => {
          this.toonToast(err.error || 'Fout bij opslaan.', 'fout');
        },
      });
    } else {
      this.toonToast('De termijn moet tussen 1 en 365 dagen liggen.', 'fout');
    }
  }
}
