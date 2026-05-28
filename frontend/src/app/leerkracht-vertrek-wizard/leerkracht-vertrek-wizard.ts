import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  computed,
  inject,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import {
  AdminGebruiker,
  AdminGebruikerService,
  KandidaatLeerkracht,
  LeerkrachtVertrekPreview,
  VertrekKlassLeeslijst,
} from '../services/AdminGebruikerService';

/**
 * Wizard-modal die de admin door het vertrek van een leerkracht loodst:
 *  1. Bevestiging — toont een korte uitleg en open-uitleningen-check.
 *  2. Leeslijsten — per actieve leeslijst kiest de admin tussen archiveren
 *     (default) of overdragen naar een actieve collega van dezelfde school.
 *     Een bulk-actie laat alle lijsten in één klik naar dezelfde leerkracht
 *     gaan.
 *  3. Samenvatting — laatste controle op aantal overdrachten + archiveringen.
 *
 * Niet-toegewezen leeslijsten worden door de backend automatisch gearchiveerd.
 * Alles loopt door één PATCH-call zodat de backend het atomair kan houden.
 */
@Component({
  selector: 'app-leerkracht-vertrek-wizard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './leerkracht-vertrek-wizard.html',
  styleUrl: './leerkracht-vertrek-wizard.css',
})
export class LeerkrachtVertrekWizard implements OnInit {
  @Input({ required: true }) leerkracht!: AdminGebruiker;
  @Output() voltooid = new EventEmitter<void>();
  @Output() geannuleerd = new EventEmitter<void>();

  private service = inject(AdminGebruikerService);

  stap = signal<1 | 2 | 3 | 'klaar'>(1);
  laden = signal<boolean>(false);
  bezig = signal<boolean>(false);
  fout = signal<string | null>(null);

  preview = signal<LeerkrachtVertrekPreview | null>(null);

  // Mapping leeslijstId → nieuweLeerkrachtId; afwezige sleutel betekent "archiveren".
  // We bewaren dit als plain object zodat het direct als request-body geserialiseerd kan worden.
  keuzes = signal<Record<number, number>>({});

  bulkLeerkrachtId = signal<number | ''>('');

  // --- afgeleide weergavewaarden ---
  geenLeeslijsten = computed(() => (this.preview()?.leeslijsten.length ?? 0) === 0);
  geblokkeerd = computed(() => (this.preview()?.aantalOpenLeningen ?? 0) > 0);

  aantalOvergedragen = computed(() => Object.keys(this.keuzes()).length);
  aantalGearchiveerd = computed(() => {
    const totaal = this.preview()?.leeslijsten.length ?? 0;
    return totaal - this.aantalOvergedragen();
  });

  ngOnInit(): void {
    this.laadPreview();
  }

  private laadPreview(): void {
    this.laden.set(true);
    this.fout.set(null);
    this.service.getLeerkrachtVertrekPreview(this.leerkracht.id).subscribe({
      next: (p) => {
        this.preview.set(p);
        this.keuzes.set({});
        this.laden.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.fout.set(this.foutTekst(err, 'Kon vertrek-preview niet laden.'));
        this.laden.set(false);
      },
    });
  }

  trackLijst(_idx: number, lijst: VertrekKlassLeeslijst): number {
    return lijst.id;
  }

  trackKandidaat(_idx: number, k: KandidaatLeerkracht): number {
    return k.id;
  }

  kandidaatLabel(k: KandidaatLeerkracht): string {
    const naam = [k.voorNaam, k.achternaam].filter((s) => s && s.trim()).join(' ').trim();
    return naam || k.smartschoolId;
  }

  /** Kies-handler voor een rij in stap 2. '' = archiveren, anders leerkracht-id. */
  zetKeuzeVoorLijst(lijstId: number, waarde: string): void {
    const huidig = { ...this.keuzes() };
    if (!waarde) {
      delete huidig[lijstId];
    } else {
      huidig[lijstId] = Number(waarde);
    }
    this.keuzes.set(huidig);
  }

  keuzeVoorLijst(lijstId: number): number | '' {
    const v = this.keuzes()[lijstId];
    return v == null ? '' : v;
  }

  pasBulkToe(): void {
    const id = this.bulkLeerkrachtId();
    const lijsten = this.preview()?.leeslijsten ?? [];
    if (!id) {
      // bulk = "alles archiveren"
      this.keuzes.set({});
      return;
    }
    const nieuw: Record<number, number> = {};
    for (const l of lijsten) nieuw[l.id] = Number(id);
    this.keuzes.set(nieuw);
  }

  naarStap2(): void {
    if (this.geblokkeerd()) return;
    if (this.geenLeeslijsten()) {
      // Geen lijsten te behandelen — sla stap 2 over.
      this.stap.set(3);
    } else {
      this.stap.set(2);
    }
  }

  naarStap3(): void {
    this.stap.set(3);
  }

  terug(): void {
    if (this.stap() === 3) {
      // Als er geen leeslijsten waren, hebben we stap 2 overgeslagen — keer terug naar stap 1.
      this.stap.set(this.geenLeeslijsten() ? 1 : 2);
    } else if (this.stap() === 2) {
      this.stap.set(1);
    }
  }

  annuleer(): void {
    if (this.bezig()) return;
    this.geannuleerd.emit();
  }

  bevestig(): void {
    if (this.bezig()) return;
    this.bezig.set(true);
    this.fout.set(null);
    this.service.markeerLeerkrachtVertrokken(this.leerkracht.id, this.keuzes()).subscribe({
      next: () => {
        this.bezig.set(false);
        this.stap.set('klaar');
      },
      error: (err: HttpErrorResponse) => {
        this.bezig.set(false);
        this.fout.set(this.foutTekst(err, 'Vertrek markeren mislukt.'));
      },
    });
  }

  sluitNaSucces(): void {
    this.voltooid.emit();
  }

  private foutTekst(err: HttpErrorResponse, fallback: string): string {
    const body = err?.error;
    if (typeof body === 'string' && body.trim()) return body;
    if (body && typeof body.message === 'string') return body.message;
    return fallback;
  }
}
