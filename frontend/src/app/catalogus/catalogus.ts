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
  selectedUitgever = signal<string | undefined>(undefined);
  minPagina = signal<number | undefined>(undefined);
  maxPagina = signal<number | undefined>(undefined);
  selectedSorteerOp = signal<string | undefined>(undefined);
  selectedSorteerRichting = signal<'ASC' | 'DESC'>('ASC');
  beschikbaarheid = signal<boolean | undefined>(undefined);
  uitgaveJaarVan = signal<number | undefined>(undefined);
  uitgaveJaarTot = signal<number | undefined>(undefined);
  populariteit = signal<number | undefined>(undefined);
  leesniveau = signal<string | undefined>(undefined);
  selectedHoofdGenre = signal<string>('');
  jaarError = signal<string>('');

  private readonly FILTERS_KEYS = 'catalogus_filters';

  private boekService = inject(BoekService);

  genreHiërarchie = {
    'Non-fictie algemeen': [
      'Biografie / autobiografie',
      'Wetenschap & technologie',
      'Filosofie',
      'Maatschappij & politiek',
      'Psychologie',
      'Geschiedenis',
      'Kunst & cultuur',
    ],
    'Fictie algemeen': [],
    'Literaire roman': [],
    'Spanning / thriller': [],
    'Detective / misdaad': [],
    Fantasy: [],
    Dystopie: [],
    'Historische roman': [],
    Romantiek: [],
    'Coming-of-age': [],
    Avontuur: [],
    'Oorlog & conflict': [],
    Horror: [],
    Humor: [],
    'Graphic novel / strip': [],
    Poëzie: [],
  };

  ngOnInit(): void {
    const stored = sessionStorage.getItem(this.FILTERS_KEYS);
    if (stored) {
      try {
        const filters = JSON.parse(stored);

        this.zoekTerm.set(filters.zoekTerm || '');
        this.selectedGenre.set(filters.selectedGenre);
        this.selectedUitgever.set(filters.selectedUitgever);
        this.minPagina.set(filters.minPagina);
        this.maxPagina.set(filters.maxPagina);
        this.selectedSorteerOp.set(filters.selectedSorteerOp);
        this.selectedSorteerRichting.set(filters.selectedSorteerRichting || 'ASC');
        this.beschikbaarheid.set(filters.beschikbaarheid);
        this.uitgaveJaarVan.set(filters.uitgaveJaarVan);
        this.uitgaveJaarTot.set(filters.uitgaveJaarTot);
        this.populariteit.set(filters.populariteit);
        this.leesniveau.set(filters.leesniveau);
        this.selectedHoofdGenre.set(filters.selectedHoofdGenre || '');

        this.updateFilters();
        return;
      } catch (e) {
        console.error('Fout bij parsen filters:', e);
      }
    }
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
    const huidigeJaar = new Date().getFullYear();
    const jaarVan = this.uitgaveJaarVan();
    const jaarTot = this.uitgaveJaarTot();

    this.jaarError.set('');

    if (jaarVan && (jaarVan < 1900 || jaarVan > huidigeJaar)) {
      this.jaarError.set(`Voer een jaar in tussen 1900 en ${huidigeJaar}`);
      return;
    }
    if (jaarTot && (jaarTot < 1900 || jaarTot > huidigeJaar)) {
      this.jaarError.set(`Voer een jaar in tussen 1900 en ${huidigeJaar}`);
      return;
    }
    if (jaarVan && jaarTot && jaarVan > jaarTot) {
      this.jaarError.set('"Van" mag niet groter zijn dan "Tot"');
      return;
    }

    const filterOptions: FilterOptions = {
      zoekterm: this.zoekTerm()?.trim() || undefined,
      genre: this.selectedGenre()?.trim() || this.selectedHoofdGenre(),
      uitgever: this.selectedUitgever()?.trim() || undefined,
      minPagina: this.minPagina(),
      maxPagina: this.maxPagina(),
      sorteerOp: this.selectedSorteerOp() || undefined,
      sorteerRichting: this.selectedSorteerRichting() || 'ASC',
      beschikbaarheid: this.beschikbaarheid() === true ? 'true' : undefined,
      uitgaveJaarVan: this.uitgaveJaarVan(),
      uitgaveJaarTot: this.uitgaveJaarTot(),
      populariteit: this.populariteit(),
      leesniveau: this.leesniveau(),
    };

    this.filters.set(filterOptions);
    this.laadGefilterdeBoeken();

    const huidigeFilters = {
      zoekTerm: this.zoekTerm(),
      selectedGenre: this.selectedGenre(),
      selectedUitgever: this.selectedUitgever(),
      minPagina: this.minPagina(),
      maxPagina: this.maxPagina(),
      selectedSorteerOp: this.selectedSorteerOp(),
      selectedSorteerRichting: this.selectedSorteerRichting(),
      beschikbaarheid: this.beschikbaarheid(),
      uitgaveJaarVan: this.uitgaveJaarVan(),
      uitgaveJaarTot: this.uitgaveJaarTot(),
      populariteit: this.populariteit(),
      leesniveau: this.leesniveau(),
      selectedHoofdGenre: this.selectedHoofdGenre(),
    };

    sessionStorage.setItem(this.FILTERS_KEYS, JSON.stringify(huidigeFilters));
  }

  opSorteerRichtingVerandering(): void {
    this.selectedSorteerRichting.set(this.selectedSorteerRichting() === 'ASC' ? 'DESC' : 'ASC');
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

  getHoofdGenres(): string[] {
    return [
      'Fictie algemeen',
      'Literaire roman',
      'Spanning / thriller',
      'Detective / misdaad',
      'Fantasy',
      'Sciencefiction',
      'Dystopie',
      'Historische roman',
      'Romantiek',
      'Coming-of-age',
      'Avontuur',
      'Oorlog & conflict',
      'Horror',
      'Humor',
      'Graphic novel / strip',
      'Poëzie',
      'Non-fictie algemeen',
    ];
  }

  getSubGenres(hoofdGenre: string): string[] {
    return this.genreHiërarchie[hoofdGenre as keyof typeof this.genreHiërarchie] || [];
  }

  onHoofdGenreChange(): void {
    this.selectedGenre.set(undefined);
    this.updateFilters();
  }

  resetFilters(): void {
    this.zoekTerm.set('');
    this.selectedGenre.set(undefined);
    this.selectedUitgever.set('');
    this.minPagina.set(undefined);
    this.maxPagina.set(undefined);
    this.selectedSorteerOp.set(undefined);
    this.selectedSorteerRichting.set('ASC');
    this.beschikbaarheid.set(undefined);
    this.uitgaveJaarVan.set(undefined);
    this.uitgaveJaarTot.set(undefined);
    this.populariteit.set(undefined);
    this.leesniveau.set(undefined);
    this.selectedHoofdGenre.set('');
    sessionStorage.removeItem(this.FILTERS_KEYS);
    this.updateFilters();
  }
}
