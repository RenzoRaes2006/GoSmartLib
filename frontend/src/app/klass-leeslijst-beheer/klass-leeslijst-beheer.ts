import { Component, inject, signal } from '@angular/core';
import { KlassLeeslijst } from '../models/klass-leeslijst.model';
import { Boek } from '../models/Boek';
import { ActivatedRoute, Router } from '@angular/router';
import { BoekService } from '../services/boek.service';
import { KlassLeeslijstService } from '../services/klassLeeslijstService';
import { LeerlingService } from '../services/Leerling.service';
import { FilterOptions } from '../models/FilterOptions';
import { FormsModule } from '@angular/forms';

type KoppelType = 'KLAS' | 'LEERLING';

@Component({
  selector: 'app-klass-leeslijst-beheer',
  imports: [FormsModule],
  templateUrl: './klass-leeslijst-beheer.html',
  styleUrl: './klass-leeslijst-beheer.css',
})
export class KlassLeeslijstBeheerComponent {
  lijst = signal<KlassLeeslijst | undefined>(undefined);

  zoekResultaten = signal<Boek[]>([]);
  zoekTerm = signal<string>('');
  bewerkNaam = signal<string>('');
  bewerkBeschrijving = signal<string>('');
  showEditModal = signal<boolean>(false);
  showBeschrijvingModal = signal<boolean>(false);
  showDeleteModal = signal<boolean>(false);
  showKoppelingModal = signal<boolean>(false);

  // Filters: pagina's en leesniveau
  minPagina = signal<number | undefined>(undefined);
  maxPagina = signal<number | undefined>(undefined);
  leesniveau = signal<string | undefined>(undefined);
  paginaError = signal<string>('');
  maxPaginaLimit = 1500;

  // selectedHoofdGenre = signal<string>('');
  klassen = signal<string[]>([]);
  koppelType = signal<KoppelType>('KLAS');
  koppelKlas = signal<string>('');
  koppelLeerlingKlas = signal<string>('');
  koppelLeerlingen = signal<any[]>([]);
  koppelLeerlingIds = signal<number[]>([]);
  koppelFout = signal<string>('');

  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private leeslijstService = inject(KlassLeeslijstService);
  private boekService = inject(BoekService);
  private leerlingService = inject(LeerlingService);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.laadLijst(id);
    this.updateFilters();
    this.leerlingService.getKlassen().subscribe({
      next: (data) => this.klassen.set(data),
      error: (err) => console.error('Fout bij ophalen klassen:', err),
    });
  }

  laadLijst(id: number): void {
    this.leeslijstService.getLeeslijstDetails(id).subscribe({
      next: (data) => this.lijst.set(data),
      error: (err) => console.error('Fout bij het laden van de lijst:', err),
    });
  }

  updateFilters(): void {
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

    const filterOptions: FilterOptions = {
      zoekterm: this.zoekTerm()?.trim() || undefined,
      minPagina: (this.minPagina() ?? 0) > 0 ? this.minPagina() : undefined,
      maxPagina:
        (this.maxPagina() ?? this.maxPaginaLimit) < this.maxPaginaLimit
          ? this.maxPagina()
          : undefined,
      leesniveau: this.leesniveau(),
      page: 0,
      size: 10,
    };

    this.boekService.getBoekenGefilterd(filterOptions).subscribe({
      next: (data) => this.zoekResultaten.set(data.content),
      error: (err) => console.log('Fout bij zoeken:', err),
    });
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

  // 4. Specifieke Leeslijst Acties
  voegBoekToe(boekId: number): void {
    const huidigeLijst = this.lijst();
    if (huidigeLijst) {
      this.leeslijstService.addBoekToLeeslijst(huidigeLijst.id, boekId).subscribe({
        next: (geupdateLijst) => this.lijst.set(geupdateLijst),
        error: (err) => console.error('Fout bij het toevoegen van boek:', err),
      });
    }
  }

  isAlToegevoegd(boekId: number): boolean {
    return this.lijst()?.boeken.some((b) => b.id === boekId) ?? false;
  }

  verwijderBoek(boekId: number): void {
    const huidigeLijst = this.lijst();
    if (huidigeLijst) {
      this.leeslijstService.verwijderBoekUitLeeslijst(huidigeLijst.id, boekId).subscribe({
        next: (geupdateLijst) => this.lijst.set(geupdateLijst),
        error: (err) => console.error('Fout bij verwijderen:', err),
      });
    }
  }

  openEditModal(): void {
    this.bewerkNaam.set(this.lijst()?.naam || '');
    this.showEditModal.set(true);
  }

  slaNaamOp(): void {
    const lijst = this.lijst();
    if (lijst && this.bewerkNaam().trim() !== '') {
      this.leeslijstService.updateLeeslijstNaam(lijst.id, this.bewerkNaam()).subscribe((data) => {
        this.lijst.set(data);
        this.showEditModal.set(false);
      });
    }
  }

  openBeschrijvingModal(): void {
    this.bewerkBeschrijving.set(this.lijst()?.beschrijving || '');
    this.showBeschrijvingModal.set(true);
  }

  slaBeschrijvingOp(): void {
    const lijst = this.lijst();
    if (lijst) {
      this.leeslijstService.updateBeschrijving(lijst.id, this.bewerkBeschrijving()).subscribe({
        next: (data) => {
          this.lijst.set(data);
          this.showBeschrijvingModal.set(false);
        },
        error: (err) => console.error('Fout bij opslaan beschrijving:', err),
      });
    }
  }

  toggleZichtbaar(): void {
    const lijst = this.lijst();
    if (!lijst) return;
    const nieuw = !lijst.zichtbaar;
    this.leeslijstService.updateZichtbaar(lijst.id, nieuw).subscribe({
      next: (data) => this.lijst.set(data),
      error: (err) => console.error('Fout bij wijzigen zichtbaarheid:', err),
    });
  }

  openDeleteModal(): void {
    this.showDeleteModal.set(true);
  }

  bevestigVerwijderen(): void {
    const lijstId = this.lijst()?.id;
    if (lijstId) {
      this.leeslijstService.deleteLeeslijst(lijstId).subscribe(() => {
        this.showDeleteModal.set(false);
        this.router.navigate(['/leeslijsten']);
      });
    }
  }

  closeModals(): void {
    this.showEditModal.set(false);
    this.showBeschrijvingModal.set(false);
    this.showDeleteModal.set(false);
    this.showKoppelingModal.set(false);
    this.koppelFout.set('');
  }

  openKoppelingModal(): void {
    const lijst = this.lijst();
    if (!lijst) return;
    this.koppelFout.set('');
    this.koppelLeerlingKlas.set('');
    this.koppelLeerlingen.set([]);
    if (lijst.klas) {
      this.koppelType.set('KLAS');
      this.koppelKlas.set(lijst.klas);
      this.koppelLeerlingIds.set([]);
    } else if (lijst.leerlingIds && lijst.leerlingIds.length > 0) {
      this.koppelType.set('LEERLING');
      this.koppelKlas.set('');
      this.koppelLeerlingIds.set([...lijst.leerlingIds]);
    } else {
      this.koppelType.set('KLAS');
      this.koppelKlas.set('');
      this.koppelLeerlingIds.set([]);
    }
    this.showKoppelingModal.set(true);
  }

  onKoppelLeerlingKlasChange(): void {
    const klas = this.koppelLeerlingKlas();
    if (!klas) {
      this.koppelLeerlingen.set([]);
      return;
    }
    this.leerlingService.getLeerlingen(0, 50, klas, '').subscribe({
      next: (page) => this.koppelLeerlingen.set(page.content || []),
      error: (err) => console.error('Fout bij ophalen leerlingen:', err),
    });
  }

  toggleLeerling(id: number): void {
    const current = this.koppelLeerlingIds();
    if (current.includes(id)) {
      this.koppelLeerlingIds.set(current.filter((i) => i !== id));
    } else {
      this.koppelLeerlingIds.set([...current, id]);
    }
  }

  isLeerlingGeselecteerd(id: number): boolean {
    return this.koppelLeerlingIds().includes(id);
  }

  get kanKoppelingOpslaan(): boolean {
    if (this.koppelType() === 'KLAS') return !!this.koppelKlas();
    return this.koppelLeerlingIds().length > 0;
  }

  slaKoppelingOp(): void {
    const lijst = this.lijst();
    if (!lijst || !this.kanKoppelingOpslaan) return;

    const payload =
      this.koppelType() === 'KLAS'
        ? { klas: this.koppelKlas(), leerlingIds: null }
        : { klas: null, leerlingIds: this.koppelLeerlingIds() };

    this.leeslijstService.updateKoppeling(lijst.id, payload).subscribe({
      next: (data) => {
        this.lijst.set(data);
        this.showKoppelingModal.set(false);
        this.koppelFout.set('');
      },
      error: (err) => {
        console.error('Fout bij koppeling wijzigen:', err);
        this.koppelFout.set(
          typeof err.error === 'string' ? err.error : 'Wijzigen van koppeling mislukt.',
        );
      },
    });
  }

  terugNaarOverzicht(): void {
    this.router.navigate(['/leeslijsten']);
  }
}
