import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BoekService } from '../services/boek.service';
import { InstellingService } from '../services/instelling-service';

@Component({
  selector: 'app-admin-dashboard',
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard implements OnInit {
  boekService = inject(BoekService);
  huidigeLimiet = signal<number>(0);
  instellingService = inject(InstellingService);
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

  // tijdelijke dummy data — te vervangen door echte statistieken zodra backend-endpoints beschikbaar zijn
  percentageUitgeleend: number = 42;
  teLaatIngeleverd: number = 7;
  populairsteGenre: string = 'Fantasy';
  gemiddeldeLeenduur: number = 14;

  ngOnInit(): void {
    this.laadLimiet();
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
}
