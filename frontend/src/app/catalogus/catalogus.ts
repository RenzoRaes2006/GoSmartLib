import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { Boek } from '../models/Boek';
import { BoekService } from '../services/boek.service';
import { RouterLink } from '@angular/router';
import { FilterOptions } from '../models/FilterOptions';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-catalogus',
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './catalogus.html',
  styleUrl: './catalogus.css',
  standalone: true,
})
export class Catalogus implements OnInit {
  boekenlijst = signal<Boek[]>([]);
  zoekTerm = signal<string>('');
  filters = signal<FilterOptions>({});
  selectedGenre = signal<string | undefined>(undefined);
  selectedUitgever = signal<string>('');
  minPagina = signal<number | undefined>(undefined);
  maxPagina = signal<number | undefined>(undefined);
  selectedSorteerOp = signal<string | undefined>(undefined);
  selectedSorteerRichting = signal<'ASC' | 'DESC'>('ASC');

  private boekService = inject(BoekService);

  ngOnInit(): void {
    this.laadBoeken();
  }

  private laadBoeken(term?: string): void {
    this.boekService.getBoeken(term).subscribe({
      next: (data) => {
        this.boekenlijst.set(data);
      },
      error: (err) => console.log('Fout bij ophalen boeken:', err),
    });
  }

  onSearch(term: string): void {
    this.zoekTerm.set(term);
    this.laadBoeken(term);
  }

  updateFilters(): void {
    const filterOptions: FilterOptions = {
      zoekterm: this.zoekTerm()?.trim() || undefined,
      genre: this.selectedGenre()?.trim() || undefined,
      uitgever: this.selectedUitgever()?.trim() || undefined,
      minPagina: this.minPagina(),
      maxPagina: this.maxPagina(),
      sorteerOp: this.selectedSorteerOp(),
      sorteerRichting: this.selectedSorteerRichting() || 'ASC',
    };

    this.filters.set(filterOptions);
    this.laadGefilterdeBoeken();
  }

  opSorteerRichtingVerandering(): void {
    const current = this.selectedSorteerRichting();
    this.selectedSorteerRichting.set(current === 'ASC' ? 'DESC' : 'ASC');
    this.updateFilters();
  }

  private laadGefilterdeBoeken(): void {
    this.boekService.getBoekenGefilterd(this.filters()).subscribe({
      next: (data) => {
        this.boekenlijst.set(data);
      },
      error: (err) => console.log('Fout bij ophalen gefilterde boeken:', err),
    });
  }

  resetFilters(): void {
    this.zoekTerm.set('');
    this.selectedGenre.set(undefined);
    this.selectedUitgever.set('');
    this.minPagina.set(undefined);
    this.maxPagina.set(undefined);
    this.selectedSorteerOp.set(undefined);
    this.selectedSorteerRichting.set('ASC');
    this.updateFilters();
  }
}
