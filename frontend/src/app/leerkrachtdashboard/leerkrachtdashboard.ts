import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { LeerlingService } from '../services/Leerling.service';
import { BoekService } from '../services/boek.service';
import { StatistiekService } from '../services/statistiek.service';
import { AuthService } from '../services/AuthService';

@Component({
  selector: 'app-leerkrachtdashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './leerkrachtdashboard.html',
  styleUrl: './leerkrachtdashboard.css',
})
export class Leerkrachtdashboard implements OnInit {
  private leerlingService = inject(LeerlingService);
  private boekService = inject(BoekService);
  private statistiekService = inject(StatistiekService);
  private authService = inject(AuthService);

  isOokBeheerder = computed(() => this.authService.currentUser()?.isBeheerder ?? false);

  boekenAfgelopenWeek = signal<number>(0);
  topBoeken = signal<string[]>([]);
  aantalActieveLeningen = signal<number>(0);
  aantalTeLaat = signal<number>(0);
  aantalLeerlingen = signal<number>(0);
  weekDoel = 25;
  donutVulling = computed(
    () => Math.min(Math.round((this.boekenAfgelopenWeek() / this.weekDoel) * 100), 100) + '%',
  );

  huidigePagina = signal<number>(0);
  itemsPerPagina = 5;
  totaalAantalPaginas = signal<number>(0);
  selectedClass = signal<string>('');
  zoekterm = signal<string>('');
  leerlingStats = signal<any>(null);

  zichtbareLeerlingen = signal<any[]>([]);
  selectedStudent = signal<any | null>(null);
  beschikbareKlassen = signal<string[]>([]);

  actieveKlassenLeningen = signal<any[]>([]);

  ngOnInit(): void {
    this.haalLeerlingenOp();
    this.haalKlassenLeningenOp();
    this.haalStatistiekenOp();
    this.haalKlassenOp();
  }

  haalKlassenOp() {
    this.leerlingService.getKlassen().subscribe({
      next: (klassen) => this.beschikbareKlassen.set(klassen ?? []),
      error: (err) => console.error('fout bij ophalen klassen:', err),
    });
  }

  haalKlassenLeningenOp() {
    this.boekService.getMijnActieveLeningen().subscribe({
      next: (leningen) => {
        const gegroepeerd = leningen.reduce((acc: any, lening: any) => {
          const boekId = lening.boek.id;

          let inleverDate = lening.inleverDatum;
          if (Array.isArray(inleverDate)) {
            inleverDate = new Date(inleverDate[0], inleverDate[1] - 1, inleverDate[2]);
          } else if (typeof inleverDate === 'string') {
            inleverDate = new Date(inleverDate);
          }

          if (!acc[boekId]) {
            acc[boekId] = {
              boek: lening.boek,
              aantal: 0,
              uitleenDatum: lening.uitleenDatum,
              inleverDatum: inleverDate,
              vervaltBinnenkort: this.isVervaltBinnenkort(inleverDate),
            };
          }
          acc[boekId].aantal++;
          return acc;
        }, {});

        this.actieveKlassenLeningen.set(Object.values(gegroepeerd));
      },
      error: (err) => console.error('Fout bij ophalen klassikale leningen', err),
    });
  }

  isVervaltBinnenkort(inleverDatum: any): boolean {
    if (!inleverDatum) return false;

    const datum = inleverDatum instanceof Date ? inleverDatum : new Date(inleverDatum);
    const vandaag = new Date();

    const verschilInTijd = datum.getTime() - vandaag.getTime();
    const verschilInDagen = Math.ceil(verschilInTijd / (1000 * 3600 * 24));

    return verschilInDagen <= 7 && verschilInDagen >= 0;
  }

  haalStatistiekenOp() {
    this.statistiekService.getLeerkrachtStatistieken(this.selectedClass() || undefined).subscribe({
      next: (stats) => {
        this.boekenAfgelopenWeek.set(stats.boekenAfgelopenWeek ?? 0);
        this.topBoeken.set(stats.topBoeken ?? []);
        this.aantalActieveLeningen.set(stats.aantalActieveLeningen ?? 0);
        this.aantalTeLaat.set(stats.aantalTeLaat ?? 0);
        this.aantalLeerlingen.set(stats.aantalLeerlingen ?? 0);
      },
      error: (err) => console.error('fout bij ophalen leerkracht-statistieken:', err),
    });
  }

  haalLeerlingenOp() {
    this.leerlingService
      .getLeerlingen(
        this.huidigePagina(),
        this.itemsPerPagina,
        this.selectedClass(),
        this.zoekterm(),
      )
      .subscribe({
        next: (response) => {
          this.zichtbareLeerlingen.set(response.content);
          this.totaalAantalPaginas.set(response.totalPages);
        },
        error: (err) => console.error('fout bij ophalen leerlingen:', err),
      });
  }

  onSearch() {
    this.huidigePagina.set(0);
    this.haalLeerlingenOp();
  }

  onFilterChange() {
    this.huidigePagina.set(0);
    this.haalLeerlingenOp();
    this.haalStatistiekenOp(); // Haalt ook direct de juiste stats voor deze klas op!
  }

  vorigePagina(): void {
    if (this.huidigePagina() > 0) {
      this.huidigePagina.update((p) => p - 1);
      this.haalLeerlingenOp();
    }
  }

  volgendePagina(): void {
    if (this.huidigePagina() < this.totaalAantalPaginas() - 1) {
      this.huidigePagina.update((p) => p + 1);
      this.haalLeerlingenOp();
    }
  }

  openHistory(student: any) {
    this.selectedStudent.set(student);
    this.leerlingService.getLeerlingStats(student.id).subscribe({
      next: (stats) => this.leerlingStats.set(stats),
      error: (err) => console.error('fout bij laden stats', err),
    });
  }

  closeHistory() {
    this.selectedStudent.set(null);
    this.leerlingStats.set(null);
  }
}
