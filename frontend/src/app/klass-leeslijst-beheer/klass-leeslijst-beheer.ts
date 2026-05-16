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

  klassen = signal<string[]>([]);
  koppelType = signal<KoppelType>('KLAS');
  koppelKlas = signal<string>('');
  koppelLeerlingKlas = signal<string>('');
  koppelLeerlingen = signal<any[]>([]);
  koppelLeerlingId = signal<number | null>(null);
  koppelLeerlingNaam = signal<string>('');
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
    const filterOptions: FilterOptions = {
      zoekterm: this.zoekTerm()?.trim() || undefined,
      page: 0,
      size: 10,
    };

    this.boekService.getBoekenGefilterd(filterOptions).subscribe({
      next: (data) => this.zoekResultaten.set(data.content),
      error: (err) => console.log('Fout bij zoeken:', err),
    });
  }

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
    if (lijst.klas) {
      this.koppelType.set('KLAS');
      this.koppelKlas.set(lijst.klas);
      this.koppelLeerlingKlas.set('');
      this.koppelLeerlingen.set([]);
      this.koppelLeerlingId.set(null);
      this.koppelLeerlingNaam.set('');
    } else if (lijst.leerlingId) {
      this.koppelType.set('LEERLING');
      this.koppelKlas.set('');
      this.koppelLeerlingId.set(lijst.leerlingId);
      this.koppelLeerlingNaam.set(lijst.leerlingNaam || '');
      this.koppelLeerlingKlas.set('');
      this.koppelLeerlingen.set([]);
    } else {
      this.koppelType.set('KLAS');
    }
    this.showKoppelingModal.set(true);
  }

  onKoppelLeerlingKlasChange(): void {
    this.koppelLeerlingId.set(null);
    this.koppelLeerlingNaam.set('');
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

  kiesKoppelLeerling(leerling: any): void {
    this.koppelLeerlingId.set(leerling.id);
    this.koppelLeerlingNaam.set(leerling.smartschoolId || `Leerling ${leerling.id}`);
  }

  get kanKoppelingOpslaan(): boolean {
    if (this.koppelType() === 'KLAS') return !!this.koppelKlas();
    return this.koppelLeerlingId() !== null;
  }

  slaKoppelingOp(): void {
    const lijst = this.lijst();
    if (!lijst || !this.kanKoppelingOpslaan) return;

    const payload =
      this.koppelType() === 'KLAS'
        ? { klas: this.koppelKlas(), leerlingId: null }
        : { klas: null, leerlingId: this.koppelLeerlingId() };

    this.leeslijstService.updateKoppeling(lijst.id, payload).subscribe({
      next: (data) => {
        this.lijst.set(data);
        this.showKoppelingModal.set(false);
        this.koppelFout.set('');
      },
      error: (err) => {
        console.error('Fout bij koppeling wijzigen:', err);
        this.koppelFout.set(typeof err.error === 'string' ? err.error : 'Wijzigen van koppeling mislukt.');
      },
    });
  }

  terugNaarOverzicht(): void {
    this.router.navigate(['/leeslijsten']);
  }
}
