import { Component, inject, signal } from '@angular/core';
import { KlassLeeslijst } from '../models/klass-leeslijst.model';
import { Boek } from '../models/Boek';
import { ActivatedRoute, Router } from '@angular/router';
import { BoekService } from '../services/boek.service';
import { KlassLeeslijstService } from '../services/klassLeeslijstService';
import { FilterOptions } from '../models/FilterOptions';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-klass-leeslijst-beheer',
  imports: [FormsModule],
  templateUrl: './klass-leeslijst-beheer.html',
  styleUrl: './klass-leeslijst-beheer.css',
})
export class KlassLeeslijstBeheerComponent {
  // 1. Leeslijst Data
  lijst = signal<KlassLeeslijst | undefined>(undefined);

  // 2. Filter & Zoek Data (Gekopieerd uit catalogus.ts)
  zoekResultaten = signal<Boek[]>([]);
  zoekTerm = signal<string>('');
  isBewerken = signal<boolean>(false);
  bewerkNaam = signal<string>('');
  // Extra signals voor de modals
  showEditModal = signal<boolean>(false);
  showDeleteModal = signal<boolean>(false);

  // Filters: pagina's en leesniveau
  minPagina = signal<number | undefined>(undefined);
  maxPagina = signal<number | undefined>(undefined);
  leesniveau = signal<string | undefined>(undefined);
  paginaError = signal<string>('');
  maxPaginaLimit = 1500;

  // selectedHoofdGenre = signal<string>('');

  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private leeslijstService = inject(KlassLeeslijstService);
  private boekService = inject(BoekService);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.laadLijst(id);
    this.updateFilters(); // Laad direct een initiële lijst met boeken[cite: 13]
  }

  laadLijst(id: number): void {
    this.leeslijstService.getLeeslijstDetails(id).subscribe({
      next: (data) => {
        this.lijst.set(data);
      },
      error: (err) => {
        console.error('Fout bij het laden van de lijst:', err);
      }
    });
  }

  // 3. De krachtige zoekfunctie uit je catalogus[cite: 13]
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
      size: 10
    };

    this.boekService.getBoekenGefilterd(filterOptions).subscribe({
      next: (data) => this.zoekResultaten.set(data.content),
      error: (err) => console.log('Fout bij zoeken:', err)
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
        next: (geupdateLijst) => {
          this.lijst.set(geupdateLijst);
        },
        error: (err) => {
          console.error('Fout bij het toevoegen van boek:', err);
        }
      });
    }
  }

  isAlToegevoegd(boekId: number): boolean {
    return this.lijst()?.boeken.some(b => b.id === boekId) ?? false;
  }

  // Voeg deze methode toe onder je voegBoekToe() methode
  verwijderBoek(boekId: number): void {
    const huidigeLijst = this.lijst();
    if (huidigeLijst) {
      this.leeslijstService.verwijderBoekUitLeeslijst(huidigeLijst.id, boekId).subscribe({
        next: (geupdateLijst) => {
          this.lijst.set(geupdateLijst); // UI update direct
        },
        error: (err) => console.error('Fout bij verwijderen:', err)
      });
    }
  }

  // 1. Naam aanpassen modal openen
  openEditModal(): void {
    this.bewerkNaam.set(this.lijst()?.naam || '');
    this.showEditModal.set(true);
  }

  slaNaamOp(): void {
    const lijst = this.lijst();
    if (lijst && this.bewerkNaam().trim() !== '') {
      this.leeslijstService.updateLeeslijstNaam(lijst.id, this.bewerkNaam()).subscribe(data => {
        this.lijst.set(data);
        this.showEditModal.set(false); // Sluit de modal
      });
    }
  }



  // 2. Verwijder modal openen (vervangt confirm)
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

  // Sluiten van modals
  closeModals(): void {
    this.showEditModal.set(false);
    this.showDeleteModal.set(false);
  }
  // Functie 3: Dummy knop logica
  wijsToeAanKlas(): void {
    alert('Dummy Actie: Deze leeslijst wordt toegewezen aan een klas. (Functionaliteit nog te bouwen)');
  }
}
