import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, OnDestroy, signal } from '@angular/core';
import { Boek } from '../models/Boek';
import { Tag } from '../models/Tag';
import { BoekService } from '../services/boek.service';
import { TagService } from '../services/tag.service';
import { Router, RouterLink, NavigationStart } from '@angular/router';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { FilterOptions } from '../models/FilterOptions';
import { FormsModule } from '@angular/forms';
import { TagsByTypePipe } from '../models/tags-by-type.pipe';

@Component({
  selector: 'app-catalogus',
  imports: [CommonModule, RouterLink, FormsModule, TagsByTypePipe],
  templateUrl: './catalogus.html',
  styleUrl: './catalogus.css',
  standalone: true,
})
export class Catalogus implements OnInit, OnDestroy {
  boekenlijst = signal<Boek[]>([]);
  zoekTerm = signal<string>('');
  filters = signal<FilterOptions>({});

  selectedGenreId = signal<number | null>(null);
  selectedSubGenreId = signal<number | null>(null);
  selectedThematischeId = signal<number | null>(null);
  selectedDidactischeId = signal<number | null>(null);

  selectedUitgever = signal<string | undefined>(undefined);
  minPagina = signal<number | undefined>(undefined);
  maxPagina = signal<number | undefined>(undefined);
  selectedSorteerOp = signal<string | undefined>(undefined);
  selectedSorteerRichting = signal<'ASC' | 'DESC'>('ASC');
  beschikbaarheid = signal<boolean | undefined>(undefined);
  uitgaveJaarVan = signal<number | undefined>(undefined);
  uitgaveJaarTot = signal<number | undefined>(undefined);
  leesniveau = signal<string | undefined>(undefined);
  paginaError = signal<string>('');
  maxPaginaLimit = 1500;
  huidigePagina = signal<number>(0);
  paginaGrootte = signal<number>(10);
  totaalPaginas = signal<number>(1);
  totaalBoeken = signal<number>(0);

  toonDidactisch = signal<boolean>(false);

  beschikbareGenres = signal<Tag[]>([]);
  beschikbareThematischeTags = signal<Tag[]>([]);
  beschikbareDidactischeTags = signal<Tag[]>([]);

  private readonly FILTERS_KEYS = 'catalogus_filters';

  private boekService = inject(BoekService);
  private tagService = inject(TagService);
  private router = inject(Router);

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

  geselecteerdHoofdGenre(): Tag | null {
    const id = this.selectedGenreId();
    if (id == null) return null;
    return this.beschikbareGenres().find((g) => g.id === id) ?? null;
  }

  subGenresVanGeselecteerd(): Tag[] {
    return this.geselecteerdHoofdGenre()?.subTags ?? [];
  }

  onHoofdGenreFilterChange(id: number | null): void {
    this.selectedGenreId.set(id);
    this.selectedSubGenreId.set(null);
    this.updateFilters();
  }

  ngOnInit(): void {
    this.tagService.tree('GENRE').subscribe((t) => this.beschikbareGenres.set(t));
    this.tagService.list('THEMATISCH').subscribe((t) => this.beschikbareThematischeTags.set(t));
    this.tagService.list('DIDACTISCH').subscribe((t) => this.beschikbareDidactischeTags.set(t));

    const stored = sessionStorage.getItem(this.FILTERS_KEYS);
    if (stored) {
      try {
        const f = JSON.parse(stored);
        this.zoekTerm.set(f.zoekTerm || '');
        this.selectedGenreId.set(f.selectedGenreId ?? null);
        this.selectedSubGenreId.set(f.selectedSubGenreId ?? null);
        this.selectedThematischeId.set(f.selectedThematischeId ?? null);
        this.selectedDidactischeId.set(f.selectedDidactischeId ?? null);
        this.selectedUitgever.set(f.selectedUitgever);
        this.minPagina.set(f.minPagina);
        this.maxPagina.set(f.maxPagina);
        this.selectedSorteerOp.set(f.selectedSorteerOp);
        this.selectedSorteerRichting.set(f.selectedSorteerRichting || 'ASC');
        this.beschikbaarheid.set(f.beschikbaarheid);
        this.uitgaveJaarVan.set(f.uitgaveJaarVan);
        this.uitgaveJaarTot.set(f.uitgaveJaarTot);
        this.leesniveau.set(f.leesniveau);
        this.toonDidactisch.set(f.toonDidactisch || false);
        this.huidigePagina.set(f.huidigePagina || 0);

        this.updateFilters(false);
        return;
      } catch (e) {
        console.error('Fout bij parsen filters:', e);
      }
    }
    this.updateFilters();
  }

  updateFilters(resetPagina = true): void {
    if (resetPagina) this.huidigePagina.set(0);

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

    const tagIds: number[] = [];
    const subId = this.selectedSubGenreId();
    const hoofdId = this.selectedGenreId();
    if (subId) {
      tagIds.push(subId);
    } else if (hoofdId) {
      tagIds.push(hoofdId);
    }
    if (this.selectedThematischeId()) tagIds.push(this.selectedThematischeId()!);
    if (this.toonDidactisch() && this.selectedDidactischeId()) {
      tagIds.push(this.selectedDidactischeId()!);
    }

    const filterOptions: FilterOptions = {
      zoekterm: this.zoekTerm()?.trim() || undefined,
      tagIds: tagIds.length ? tagIds : undefined,
      uitgever: this.selectedUitgever()?.trim() || undefined,
      minPagina: (this.minPagina() ?? 0) > 0 ? this.minPagina() : undefined,
      maxPagina:
        (this.maxPagina() ?? this.maxPaginaLimit) < this.maxPaginaLimit
          ? this.maxPagina()
          : undefined,
      sorteerOp: this.selectedSorteerOp() || undefined,
      sorteerRichting: this.selectedSorteerRichting() || 'ASC',
      beschikbaarheid: this.beschikbaarheid() === true ? 'true' : undefined,
      didactischMateriaal: this.toonDidactisch() ? 'true' : undefined,
      uitgaveJaarVan: this.uitgaveJaarVan(),
      uitgaveJaarTot: this.uitgaveJaarTot(),
      leesniveau: this.leesniveau(),
      page: this.huidigePagina(),
      size: this.paginaGrootte(),
    };

    this.filters.set(filterOptions);
    this.laadGefilterdeBoeken();

    const huidigeFilters = {
      zoekTerm: this.zoekTerm(),
      selectedGenreId: this.selectedGenreId(),
      selectedSubGenreId: this.selectedSubGenreId(),
      selectedThematischeId: this.selectedThematischeId(),
      selectedDidactischeId: this.selectedDidactischeId(),
      selectedUitgever: this.selectedUitgever(),
      minPagina: this.minPagina(),
      maxPagina: this.maxPagina(),
      selectedSorteerOp: this.selectedSorteerOp(),
      selectedSorteerRichting: this.selectedSorteerRichting(),
      beschikbaarheid: this.beschikbaarheid(),
      uitgaveJaarVan: this.uitgaveJaarVan(),
      uitgaveJaarTot: this.uitgaveJaarTot(),
      toonDidactisch: this.toonDidactisch(),
      leesniveau: this.leesniveau(),
      huidigePagina: this.huidigePagina(),
    };
    sessionStorage.setItem(this.FILTERS_KEYS, JSON.stringify(huidigeFilters));
  }

  onMinPaginaSlider(event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = +input.value;
    const max = this.maxPagina() ?? this.maxPaginaLimit;
    const clamped = Math.min(value, max);
    if (value !== clamped) input.value = String(clamped);
    this.minPagina.set(clamped);
    this.updateFilters();
  }

  onMaxPaginaSlider(event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = +input.value;
    const min = this.minPagina() ?? 0;
    const clamped = Math.max(value, min);
    if (value !== clamped) input.value = String(clamped);
    this.maxPagina.set(clamped);
    this.updateFilters();
  }

  opSorteerRichtingVerandering(): void {
    this.selectedSorteerRichting.set(this.selectedSorteerRichting() === 'ASC' ? 'DESC' : 'ASC');
    this.updateFilters();
  }

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

  resetFilters(): void {
    this.zoekTerm.set('');
    this.selectedGenreId.set(null);
    this.selectedSubGenreId.set(null);
    this.selectedThematischeId.set(null);
    this.selectedDidactischeId.set(null);
    this.selectedUitgever.set(undefined);
    this.minPagina.set(undefined);
    this.maxPagina.set(undefined);
    this.selectedSorteerOp.set(undefined);
    this.selectedSorteerRichting.set('ASC');
    this.beschikbaarheid.set(undefined);
    this.uitgaveJaarVan.set(undefined);
    this.uitgaveJaarTot.set(undefined);
    this.leesniveau.set(undefined);
    this.toonDidactisch.set(false);
    this.huidigePagina.set(0);
    this.updateFilters();
  }

  vorigePagina(): void {
    if (this.huidigePagina() > 0) {
      this.huidigePagina.update((p) => p - 1);
      this.updateFilters(false);
    }
  }

  volgendePagina(): void {
    if (this.huidigePagina() + 1 < this.totaalPaginas()) {
      this.huidigePagina.update((p) => p + 1);
      this.updateFilters(false);
    }
  }
}
