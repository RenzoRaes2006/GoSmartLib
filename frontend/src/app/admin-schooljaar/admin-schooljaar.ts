import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { SchoolService } from '../services/School.Service';
import { SchooljaarService } from '../services/schooljaar.service';
import { Schooljaar } from '../models/schooljaar.model';
import { RolloverPreview, RolloverResultaat } from '../models/rollover.model';

@Component({
  selector: 'app-admin-schooljaar',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './admin-schooljaar.html',
  styleUrl: './admin-schooljaar.css',
})
export class AdminSchooljaar implements OnInit {

  private schooljaarService = inject(SchooljaarService);
  private schoolService = inject(SchoolService);

  actiefSchooljaar = signal<Schooljaar | null>(null);
  scholen = signal<any[]>([]);
  laden = signal(false);
  fout = signal<string | null>(null);

  geselecteerdeSchoolId = signal<number | null>(null);
  preview = signal<RolloverPreview | null>(null);
  resultaat = signal<RolloverResultaat | null>(null);
  bezig = signal(false);

  forceerVerloren = signal(false);
  nieuwNaam = '';
  nieuwStartDatum = '';
  nieuwEindDatum = '';

  toonPreviewModal = computed(() => this.preview() !== null && this.resultaat() === null);

  ngOnInit(): void {
    this.laadActiefSchooljaar();
    this.laadScholen();
  }

  private laadActiefSchooljaar(): void {
    this.schooljaarService.getActiefSchooljaar().subscribe({
      next: (s) => this.actiefSchooljaar.set(s),
      error: () => this.actiefSchooljaar.set(null),
    });
  }

  private laadScholen(): void {
    this.schoolService.getAlleScholen().subscribe({
      next: (lijst) => this.scholen.set(Array.isArray(lijst) ? lijst : []),
      error: () => this.scholen.set([]),
    });
  }

  onSchoolGeselecteerd(event: Event): void {
    const v = (event.target as HTMLSelectElement).value;
    this.geselecteerdeSchoolId.set(v ? Number(v) : null);
  }

  startRollover(): void {
    const schoolId = this.geselecteerdeSchoolId();
    if (!schoolId) return;

    this.fout.set(null);
    this.laden.set(true);
    this.schooljaarService.previewRollover(schoolId).subscribe({
      next: (p) => {
        this.preview.set(p);
        this.forceerVerloren.set(false);
        this.vulStandaardSchooljaarNaam();
        this.laden.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.fout.set(err.error ?? 'Kon preview niet ophalen.');
        this.laden.set(false);
      },
    });
  }

  private vulStandaardSchooljaarNaam(): void {
    const huidig = this.actiefSchooljaar();
    if (huidig && huidig.naam.match(/^\d{4}-\d{4}$/)) {
      const [, eind] = huidig.naam.split('-').map(Number);
      this.nieuwNaam = `${eind}-${eind + 1}`;
      this.nieuwStartDatum = `${eind}-09-01`;
      this.nieuwEindDatum = `${eind + 1}-06-30`;
    }
  }

  annuleerPreview(): void {
    this.preview.set(null);
    this.fout.set(null);
  }

  bevestigRollover(): void {
    const schoolId = this.geselecteerdeSchoolId();
    if (!schoolId) return;

    const p = this.preview();
    if (!p) return;

    if (!this.nieuwNaam.trim() || !this.nieuwStartDatum || !this.nieuwEindDatum) {
      this.fout.set('Vul naam, startdatum en einddatum in.');
      return;
    }

    if (!p.kanDoorgaan && !this.forceerVerloren()) {
      this.fout.set('Er zijn nog open uitleningen. Vink aan dat ze als verloren mogen worden gemarkeerd.');
      return;
    }

    this.bezig.set(true);
    this.fout.set(null);
    this.schooljaarService.voerRolloverUit(schoolId, {
      forceerVerloren: this.forceerVerloren(),
      nieuwNaam: this.nieuwNaam.trim(),
      nieuwStartDatum: this.nieuwStartDatum,
      nieuwEindDatum: this.nieuwEindDatum,
    }).subscribe({
      next: (r) => {
        this.resultaat.set(r);
        this.preview.set(null);
        this.bezig.set(false);
        this.laadActiefSchooljaar();
      },
      error: (err: HttpErrorResponse) => {
        this.fout.set(err.error ?? 'Rollover mislukt.');
        this.bezig.set(false);
      },
    });
  }

  sluitResultaat(): void {
    this.resultaat.set(null);
    this.nieuwNaam = '';
    this.nieuwStartDatum = '';
    this.nieuwEindDatum = '';
  }
}
