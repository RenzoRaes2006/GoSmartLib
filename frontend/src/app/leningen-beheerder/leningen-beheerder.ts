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

  // Signals voor de data en de zoekterm
  alleLeningen = signal<any[]>([]);
  zoekterm = signal<string>('');

  // Signals voor paginering
  huidigePagina = signal<number>(1);
  itemsPerPagina = 10;

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

  // 1. Computed Signal: Filter eerst op alles wat binnen 7 dagen (of eerder) binnen moet
  dringendeLeningen = computed(() => {
    const overEenWeek = new Date();
    overEenWeek.setDate(overEenWeek.getDate() + 7);
    overEenWeek.setHours(23, 59, 59, 999);

    const gefilterd = this.alleLeningen().filter(lening => {
      if (!lening.inleverDatum) return false;
      const inleverDatum = new Date(lening.inleverDatum);
      return inleverDatum <= overEenWeek;
    });

    // Sorteer direct zodat de dringendste bovenaan staan
    return gefilterd.sort((a, b) => new Date(a.inleverDatum).getTime() - new Date(b.inleverDatum).getTime());
  });

  // 2. Computed Signal: Pas de zoekbalk toe op de dringende leningen
  gefilterdeLeningen = computed(() => {
    const term = this.zoekterm().toLowerCase();
    const dringendeLijst = this.dringendeLeningen();

    if (!term) return dringendeLijst;

    return dringendeLijst.filter(lening => {
      const boekTitel = lening.boek?.titel?.toLowerCase() || '';
      const leerlingNaam = `${lening.gebruiker?.voorNaam || ''} ${lening.gebruiker?.achternaam || ''}`.toLowerCase();
      return boekTitel.includes(term) || leerlingNaam.includes(term);
    });
  });

  // 3. Computed Signal: Bereken het totaal aantal pagina's
  totaalAantalPaginas = computed(() => {
    const totaal = Math.ceil(this.gefilterdeLeningen().length / this.itemsPerPagina);
    return totaal === 0 ? 1 : totaal;
  });

  // 4. Computed Signal: Knip de lijst op in maximaal 10 items voor de huidige pagina
  zichtbareLeningen = computed(() => {
    const startIndex = (this.huidigePagina() - 1) * this.itemsPerPagina;
    const eindIndex = startIndex + this.itemsPerPagina;
    return this.gefilterdeLeningen().slice(startIndex, eindIndex);
  });

  // Acties voor de html
  updateZoekterm(term: string) {
    this.zoekterm.set(term);
    this.huidigePagina.set(1); // Spring terug naar pagina 1 bij een nieuwe zoekopdracht
  }

  vorigePagina(): void {
    if (this.huidigePagina() > 1) {
      this.huidigePagina.update(p => p - 1);
    }
  }

  volgendePagina(): void {
    if (this.huidigePagina() < this.totaalAantalPaginas()) {
      this.huidigePagina.update(p => p + 1);
    }
  }
}