import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LeerlingService } from '../services/Leerling.service';
import { BoekService } from '../services/boek.service';

@Component({
  selector: 'app-leerkrachtdashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './leerkrachtdashboard.html',
  styleUrl: './leerkrachtdashboard.css',
})
export class Leerkrachtdashboard implements OnInit {
  private leerlingService = inject(LeerlingService);
  private boekService = inject(BoekService);

  huidigePagina = signal<number>(0);
  itemsPerPagina = 5;
  totaalAantalPaginas = signal<number>(0);
  selectedClass = signal<string>('');
  zoekterm = signal<string>('');
  leerlingStats = signal<any>(null);

  zichtbareLeerlingen = signal<any[]>([]);
  selectedStudent = signal<any | null>(null);

  actieveKlassenLeningen = signal<any[]>([]);

  ngOnInit(): void {
    this.haalLeerlingenOp();
    this.haalKlassenLeningenOp();
  }

  haalKlassenLeningenOp() {
    const leraarId = 1;

    this.boekService.getActieveLeningen(leraarId).subscribe({
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
