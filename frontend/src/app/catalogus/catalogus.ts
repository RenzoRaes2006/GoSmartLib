import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, OnDestroy, signal } from '@angular/core';
import { Boek } from '../models/Boek';
import { BoekService } from '../services/boek.service';
import { Router, RouterLink, NavigationStart } from '@angular/router';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { FilterOptions } from '../models/FilterOptions';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-catalogus',
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './catalogus.html',
  styleUrl: './catalogus.css',
  standalone: true,
})
export class Catalogus implements OnInit, OnDestroy {
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
  leesniveau = signal<string | undefined>(undefined);
  selectedHoofdGenre = signal<string>('');
  selectedThematischeTag = signal<string | undefined>(undefined);
  paginaError = signal<string>('');
  maxPaginaLimit = 1500;
  huidigePagina = signal<number>(0);
  paginaGrootte = signal<number>(10);
  totaalPaginas = signal<number>(1);
  totaalBoeken = signal<number>(0);

  // Key voor sessionStorage
  private readonly FILTERS_KEYS = 'catalogus_filters';

  private boekService = inject(BoekService);
  private router = inject(Router);

  // Abonneer op router events om filters te wissen bij navigatie naar andere pagina's dan details
  private navSub: Subscription = this.router.events
    .pipe(filter((e) => e instanceof NavigationStart))
    .subscribe((e) => {
      const url = (e as NavigationStart).url;
      if (!url.startsWith('/details/')) {
        sessionStorage.removeItem(this.FILTERS_KEYS);
      }
    });

  ngOnDestroy(): void {
    this.navSub.unsubscribe();
  }

  // Beschikbare thematische tags
  beschikbareTags: string[] = [
    'Armoede & ongelijkheid',
    'Avontuur & ontdekking',
    'Diversiteit & inclusie',
    'Familie',
    'Gender & seksualiteit',
    'Identiteit & zelfbeeld',
    'Liefde & relaties',
    'Macht & onrecht',
    'Mentale gezondheid',
    'Migratie & afkomst',
    'Overleven',
    'Rouw & verlies',
    'School & prestatiedruk',
    'Sociale media',
    'Toekomst & technologie',
    'Vriendschap',
  ];

  // Genre hiërarchie
  genreHiërarchie = {
    Avontuur: [],
    'Coming-of-age': [],
    'Detective / misdaad': [],
    Dystopie: [],
    Fantasy: [],
    'Fictie algemeen': [],
    'Graphic novel / strip': [],
    'Historische roman': [],
    Horror: [],
    Humor: [],
    'Literaire roman': [],
    'Non-fictie algemeen': [
      'Biografie / autobiografie',
      'Filosofie',
      'Geschiedenis',
      'Kunst & cultuur',
      'Maatschappij & politiek',
      'Psychologie',
      'Wetenschap & technologie',
    ],
    'Oorlog & conflict': [],
    Poëzie: [],
    Romantiek: [],
    'Spanning / thriller': [],
  };

  // Bij het initialiseren van de component, probeer opgeslagen filters te laden of laad alle boeken
  ngOnInit(): void {
    const stored = sessionStorage.getItem(this.FILTERS_KEYS);
    if (stored) {
      try {
        // Parse de opgeslagen filters
        const filters = JSON.parse(stored);

        // Herstel de signalen met de opgeslagen waarden
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
        this.leesniveau.set(filters.leesniveau);
        this.selectedHoofdGenre.set(filters.selectedHoofdGenre || '');
        this.selectedThematischeTag.set(filters.selectedThematischeTag);
        this.huidigePagina.set(filters.huidigePagina || 0);

        this.updateFilters(false);
        return;
      } catch (e) {
        console.error('Fout bij parsen filters:', e);
      }
    }
    this.updateFilters();
  }

  // Om filters bij te werken en boeken opnieuw te laden op basis van de nieuwe filters
  updateFilters(resetPagina = true): void {
    if (resetPagina) {
      this.huidigePagina.set(0);
    }
    const min = this.minPagina();
    const max = this.maxPagina();

    this.paginaError.set('');

    if (min !== undefined && min < 0) {
      this.paginaError.set("Minimum pagina's mag niet negatief zijn");
      return;
    }
    if (max !== undefined && max < 0) {
      this.paginaError.set("Maximum pagina's mag niet negatief zijn");
      return;
    }
    if (min !== undefined && max !== undefined && min > max) {
      this.paginaError.set('"Min" mag niet groter zijn dan "Max"');
      return;
    }

    // Bouw genres-array: subgenre > hoofdgenre + alle subgenres > geen filter
    const subGenres = this.getSubGenres(this.selectedHoofdGenre());
    const genres = this.selectedGenre()?.trim()
      ? [this.selectedGenre()!.trim()]
      : this.selectedHoofdGenre()
        ? [this.selectedHoofdGenre(), ...subGenres]
        : undefined;

    // Bouw het filterobject op basis van de huidige waarden van de signalen
    const filterOptions: FilterOptions = {
      zoekterm: this.zoekTerm()?.trim() || undefined,
      genres,
      thematischeTags: this.selectedThematischeTag() ? [this.selectedThematischeTag()!] : undefined,
      uitgever: this.selectedUitgever()?.trim() || undefined,
      minPagina: (this.minPagina() ?? 0) > 0 ? this.minPagina() : undefined,
      maxPagina:
        (this.maxPagina() ?? this.maxPaginaLimit) < this.maxPaginaLimit
          ? this.maxPagina()
          : undefined,
      sorteerOp: this.selectedSorteerOp() || undefined,
      sorteerRichting: this.selectedSorteerRichting() || 'ASC',
      beschikbaarheid: this.beschikbaarheid() === true ? 'true' : undefined,
      uitgaveJaarVan: this.uitgaveJaarVan(),
      uitgaveJaarTot: this.uitgaveJaarTot(),
      leesniveau: this.leesniveau(),
      page: this.huidigePagina(),
      size: this.paginaGrootte(),
    };

    // Update de filters in de state en laad de gefilterde boeken
    this.filters.set(filterOptions);
    this.laadGefilterdeBoeken();

    // Sla de huidige filters op in sessionStorage zodat ze behouden blijven bij navigatie
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
      leesniveau: this.leesniveau(),
      selectedHoofdGenre: this.selectedHoofdGenre(),
      selectedThematischeTag: this.selectedThematischeTag(),
      huidigePagina: this.huidigePagina(),
    };

    // Sla de filters op als JSON-string in sessionStorage
    sessionStorage.setItem(this.FILTERS_KEYS, JSON.stringify(huidigeFilters));
  }

  onMinPaginaSlider(event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = +input.value;
    const max = this.maxPagina() ?? this.maxPaginaLimit;
    const clamped = Math.min(value, max);
    if (value !== clamped) {
      input.value = String(clamped);
    }
    this.minPagina.set(clamped);
    this.updateFilters();
  }

  onMaxPaginaSlider(event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = +input.value;
    const min = this.minPagina() ?? 0;
    const clamped = Math.max(value, min);
    if (value !== clamped) {
      input.value = String(clamped);
    }
    this.maxPagina.set(clamped);
    this.updateFilters();
  }

  opSorteerRichtingVerandering(): void {
    this.selectedSorteerRichting.set(this.selectedSorteerRichting() === 'ASC' ? 'DESC' : 'ASC');
    this.updateFilters();
  }

  // Om gefilterde boeken te laden op basis van de huidige filters
  private laadGefilterdeBoeken(): void {
    this.boekService.getBoekenGefilterd(this.filters()).subscribe({
      next: (data) => {
        this.boekenlijst.set(data.content);
        this.totaalPaginas.set(data.totalPages);
        this.totaalBoeken.set(data.totalElements);
      },
      error: (err) => console.log('Fout bij ophalen gefilterde boeken:', err),
    });
  }

  // Om de hoofdgenres op te halen, deze kunnen worden gebruikt in een dropdown of selectielijst
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

  // Om subgenres op te halen op basis van het geselecteerde hoofdgenre
  getSubGenres(hoofdGenre: string): string[] {
    return this.genreHiërarchie[hoofdGenre as keyof typeof this.genreHiërarchie] || [];
  }

  // Om de filters bij te werken en boeken opnieuw te laden op basis van de nieuwe filters
  onHoofdGenreChange(): void {
    this.selectedGenre.set(undefined);
    this.updateFilters();
  }

  resetFilters(): void {
    const hadFilters =
      !!this.zoekTerm()?.trim() ||
      this.selectedGenre() !== undefined ||
      !!this.selectedUitgever()?.trim() ||
      this.minPagina() !== undefined ||
      this.maxPagina() !== undefined ||
      this.selectedSorteerOp() !== undefined ||
      this.beschikbaarheid() !== undefined ||
      this.uitgaveJaarVan() !== undefined ||
      this.uitgaveJaarTot() !== undefined ||
      this.leesniveau() !== undefined ||
      this.selectedThematischeTag() !== undefined ||
      !!this.selectedHoofdGenre();

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
    this.leesniveau.set(undefined);
    this.selectedHoofdGenre.set('');
    this.selectedThematischeTag.set(undefined);
    sessionStorage.removeItem(this.FILTERS_KEYS);
    this.updateFilters(hadFilters);
  }

  gaNaarVolgendePagina() {
    if (this.huidigePagina() < this.totaalPaginas() - 1) {
      this.huidigePagina.set(this.huidigePagina() + 1);
      this.updateFilters(false);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  gaNaarVorigePagina() {
    if (this.huidigePagina() > 0) {
      this.huidigePagina.set(this.huidigePagina() - 1);
      this.updateFilters(false);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }
}
