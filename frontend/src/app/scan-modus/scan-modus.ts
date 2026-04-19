import { Component, computed, ElementRef, inject, signal, viewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ScanService } from '../services/scan.service';
import { Gebruiker } from '../models/Gebruiker.mode';
import { MandjeItem, ScanBatchItemResult, ScanModus as ModusType } from '../models/scan.model';

const IDLE_MS = 60_000;

type Stap = 'LEERLING' | 'MODUS' | 'SCAN' | 'RESULTAAT';

@Component({
  selector: 'app-scan-modus',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './scan-modus.html',
  styleUrl: './scan-modus.css',
})
export class ScanModus {
  private scanService = inject(ScanService);

  stap = signal<Stap>('LEERLING');

  leerlingInput = signal('');
  geselecteerdeLeerling = signal<Gebruiker | null>(null);
  leerlingFout = signal('');

  modus = signal<ModusType | null>(null);

  isbnInput = signal('');
  mandje = signal<MandjeItem[]>([]);
  scanFout = signal('');

  resultaten = signal<ScanBatchItemResult[]>([]);
  bezig = signal(false);

  toonKlaarBevestiging = signal(false);

  naamOpScherm = computed(() => {
    const l = this.geselecteerdeLeerling();
    if (!l) return '';
    const achter = l.achterNaam ?? '';
    return `${l.voorNaam} ${achter.charAt(0)}.`;
  });

  succesvolCount = computed(() => this.resultaten().filter((r) => r.status === 'OK').length);
  fouteCount = computed(() => this.resultaten().filter((r) => r.status === 'FOUT').length);

  private isbnVeld = viewChild<ElementRef<HTMLInputElement>>('isbnVeld');
  private idleTimer: ReturnType<typeof setTimeout> | null = null;

  zoekLeerling(): void {
    const invoer = this.leerlingInput().trim();
    this.leerlingFout.set('');
    if (!invoer) return;

    if (!/^\d+$/.test(invoer)) {
      this.leerlingFout.set('Typ of scan een SchoolId (alleen cijfers).');
      return;
    }

    this.scanService.zoekLeerlingOpSchoolId(Number(invoer)).subscribe({
      next: (leerling) => {
        this.geselecteerdeLeerling.set(leerling);
        this.leerlingInput.set('');
        this.stap.set('MODUS');
        this.resetIdleTimer();
      },
      error: () => this.leerlingFout.set('Leerling niet gevonden.'),
    });
  }

  kiesModus(m: ModusType): void {
    this.modus.set(m);
    this.stap.set('SCAN');
    this.resetIdleTimer();
    setTimeout(() => this.isbnVeld()?.nativeElement.focus(), 0);
  }

  terugNaarLeerling(): void {
    this.sessieReset();
  }

  terugNaarModus(): void {
    this.modus.set(null);
    this.mandje.set([]);
    this.isbnInput.set('');
    this.scanFout.set('');
    this.stap.set('MODUS');
    this.resetIdleTimer();
  }

  scanIsbn(): void {
    const isbn = this.isbnInput().trim();
    this.scanFout.set('');
    if (!isbn) return;

    if (!(/^\d{13}$/.test(isbn) || /^\d{10}$/.test(isbn) || /^GO\d{11}$/.test(isbn))) {
      this.scanFout.set('Ongeldig ISBN. Gebruik 10 of 13 cijfers, of een GO-nummer.');
      return;
    }

    if (this.mandje().some((i) => i.isbn === isbn)) {
      this.scanFout.set('Dit boek staat al in het mandje.');
      this.isbnInput.set('');
      return;
    }

    this.resetIdleTimer();

    this.scanService.zoekBoekOpIsbn(isbn).subscribe({
      next: (boek) => {
        this.mandje.update((m) => [{ isbn, titel: boek.titel }, ...m]);
        this.isbnInput.set('');
        setTimeout(() => this.isbnVeld()?.nativeElement.focus(), 0);
      },
      error: () => {
        this.scanFout.set('Boek met dit ISBN niet gevonden.');
        this.isbnInput.set('');
      },
    });
  }

  verwijderUitMandje(isbn: string): void {
    this.mandje.update((m) => m.filter((i) => i.isbn !== isbn));
    this.resetIdleTimer();
  }

  vraagKlaarBevestiging(): void {
    if (this.mandje().length === 0) {
      this.sessieReset();
      return;
    }
    this.toonKlaarBevestiging.set(true);
  }

  annuleerKlaar(): void {
    this.toonKlaarBevestiging.set(false);
    this.resetIdleTimer();
  }

  bevestigKlaar(): void {
    this.toonKlaarBevestiging.set(false);
    this.commitMandje();
  }

  private commitMandje(): void {
    const leerling = this.geselecteerdeLeerling();
    const actie = this.modus();
    const isbns = this.mandje().map((i) => i.isbn);
    if (!leerling || !actie || isbns.length === 0) {
      this.sessieReset();
      return;
    }

    this.bezig.set(true);
    this.scanService.verwerkBatch({ gebruikerId: leerling.gebruikerID!, actie, isbns }).subscribe({
      next: (resp) => {
        this.resultaten.set(resp.resultaten);
        this.stap.set('RESULTAAT');
        this.bezig.set(false);
        this.clearIdleTimer();
      },
      error: () => {
        this.scanFout.set('Er ging iets mis bij het verwerken. Probeer opnieuw.');
        this.bezig.set(false);
      },
    });
  }

  nieuweSessie(): void {
    this.sessieReset();
  }

  private sessieReset(): void {
    this.stap.set('LEERLING');
    this.leerlingInput.set('');
    this.geselecteerdeLeerling.set(null);
    this.leerlingFout.set('');
    this.modus.set(null);
    this.isbnInput.set('');
    this.mandje.set([]);
    this.scanFout.set('');
    this.resultaten.set([]);
    this.toonKlaarBevestiging.set(false);
    this.clearIdleTimer();
  }

  private resetIdleTimer(): void {
    this.clearIdleTimer();
    this.idleTimer = setTimeout(() => this.sessieReset(), IDLE_MS);
  }

  private clearIdleTimer(): void {
    if (this.idleTimer) {
      clearTimeout(this.idleTimer);
      this.idleTimer = null;
    }
  }
}
