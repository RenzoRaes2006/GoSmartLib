import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { BoekService } from '../services/boek.service';
import { InstellingService } from '../services/instelling-service';

@Component({
  selector: 'app-aanvragen-beheer',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './aanvragen-beheer.html',
  styleUrl: './aanvragen-beheer.css',
})
export class AanvragenBeheer implements OnInit {
  private boekService = inject(BoekService);
  instellingService = inject(InstellingService);

  aanvragen = signal<any[]>([]);
  verlengAanvragen = signal<any[]>([]);
  maxLeenLimiet = signal<number>(0);

  itemsPerPage = 4;
  pageNieuw = signal<number>(1);
  pageVerlenging = signal<number>(1);

  gepagineerdeNieuweAanvragen = computed(() => {
    const start = (this.pageNieuw() - 1) * this.itemsPerPage;
    return this.aanvragen().slice(start, start + this.itemsPerPage);
  });

  gepagineerdeVerlengingen = computed(() => {
    const start = (this.pageVerlenging() - 1) * this.itemsPerPage;
    return this.verlengAanvragen().slice(start, start + this.itemsPerPage);
  });

  totaalPaginasNieuw = computed(() => Math.ceil(this.aanvragen().length / this.itemsPerPage) || 1);
  totaalPaginasVerlenging = computed(
    () => Math.ceil(this.verlengAanvragen().length / this.itemsPerPage) || 1,
  );

  ngOnInit(): void {
    this.haalDataOp();
    this.instellingService.getLeenLimiet().subscribe({
      next: (limiet) => this.maxLeenLimiet.set(limiet),
      error: (err) => console.error('Kon limiet niet ophalen', err),
    });
  }

  haalDataOp() {
    this.boekService.getAlleAanvragen().subscribe({
      next: (data) => {
        this.aanvragen.set(data);

        if (this.pageNieuw() > this.totaalPaginasNieuw()) {
          this.pageNieuw.set(this.totaalPaginasNieuw());
        }

        data.forEach((aanvraag: any) => {
          const userId = aanvraag.gebruiker.gebruikerID || aanvraag.gebruiker.id;
          this.boekService.getActieveLeningen(userId).subscribe({
            next: (leningen) => {
              aanvraag.totaalLeningen = leningen.length;
            },
          });
        });
      },
      error: (err) => console.error('Error fetching regular requests:', err),
    });

    this.boekService.getAlleVerlengingAanvragen().subscribe({
      next: (data) => {
        this.verlengAanvragen.set(data);
        if (this.pageVerlenging() > this.totaalPaginasVerlenging()) {
          this.pageVerlenging.set(this.totaalPaginasVerlenging());
        }
      },
      error: (err) => console.error('Error fetching extension requests:', err),
    });
  }

  volgendePageNieuw() {
    if (this.pageNieuw() < this.totaalPaginasNieuw()) this.pageNieuw.update((p) => p + 1);
  }
  vorigePageNieuw() {
    if (this.pageNieuw() > 1) this.pageNieuw.update((p) => p - 1);
  }

  volgendePageVerlenging() {
    if (this.pageVerlenging() < this.totaalPaginasVerlenging())
      this.pageVerlenging.update((p) => p + 1);
  }
  vorigePageVerlenging() {
    if (this.pageVerlenging() > 1) this.pageVerlenging.update((p) => p - 1);
  }

  keurGoed(id: number) {
    this.boekService.keurAanvraagGoed(id).subscribe({
      next: () => this.haalDataOp(),
      error: (err) => alert('Fout bij goedkeuren: ' + err.message),
    });
  }

  wijsAf(id: number) {
    this.boekService.weigerAanvraag(id).subscribe({
      next: () => this.haalDataOp(),
      error: (err) => alert('Fout bij weigeren: ' + err.message),
    });
  }

  keurVerlengingGoed(id: number) {
    this.boekService.keurVerlengingGoed(id).subscribe({
      next: () => this.haalDataOp(),
      error: (err) => alert('Fout bij goedkeuren verlenging: ' + err.message),
    });
  }

  wijsVerlengingAf(id: number) {
    this.boekService.weigerVerlenging(id).subscribe({
      next: () => this.haalDataOp(),
      error: (err) => alert('Fout bij weigeren verlenging: ' + err.message),
    });
  }
}
