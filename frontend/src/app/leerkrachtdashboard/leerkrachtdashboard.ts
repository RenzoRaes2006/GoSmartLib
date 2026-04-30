import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LeerlingService } from '../services/Leerling.service'; // Let op: kleine letter l bij leerling.service
import { StatistiekService } from '../services/statistiek.service';

@Component({
  selector: 'app-leerkrachtdashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './leerkrachtdashboard.html',
  styleUrl: './leerkrachtdashboard.css',
})
export class Leerkrachtdashboard implements OnInit {
  private leerlingService = inject(LeerlingService);
  private statistiekService = inject(StatistiekService);

  boekenAfgelopenWeek = signal<number>(0);
  topBoeken = signal<string[]>([]);
  aantalActieveLeningen = signal<number>(0);
  aantalTeLaat = signal<number>(0);
  aantalLeerlingen = signal<number>(0);
  weekDoel = 25;
  donutVulling = computed(() =>
    Math.min(Math.round((this.boekenAfgelopenWeek() / this.weekDoel) * 100), 100) + '%'
  );

  huidigePagina = signal<number>(0);
  itemsPerPagina = 5;
  totaalAantalPaginas = signal<number>(0);
  selectedClass = signal<string>('');
  zoekterm = signal<string>('');

  zichtbareLeerlingen = signal<any[]>([]);
  selectedStudent = signal<any | null>(null);

  ngOnInit(): void {
    this.haalLeerlingenOp();
    this.haalStatistiekenOp();
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
    this.leerlingService.getLeerlingen(
      this.huidigePagina(), 
      this.itemsPerPagina, 
      this.selectedClass(),
      this.zoekterm()
    ).subscribe({
      next: (response) => {
        // Spring Boot stuurt de data in de 'content' property
        this.zichtbareLeerlingen.set(response.content);
        this.totaalAantalPaginas.set(response.totalPages);
      },
      error: (err) => console.error('fout bij ophalen leerlingen:', err)
    });
  }

  onSearch() {
    this.huidigePagina.set(0);
    this.haalLeerlingenOp();
  }

  onFilterChange() {
    this.huidigePagina.set(0);
    this.haalLeerlingenOp();
    this.haalStatistiekenOp();
  }

  vorigePagina(): void {
    if (this.huidigePagina() > 0) {
      this.huidigePagina.update(p => p - 1);
      this.haalLeerlingenOp();
    }
  }

  volgendePagina(): void {
    if (this.huidigePagina() < this.totaalAantalPaginas() - 1) {
      this.huidigePagina.update(p => p + 1);
      this.haalLeerlingenOp();
    }
  }

  openHistory(student: any) {
    this.selectedStudent.set(student);
  }

  closeHistory() {
    this.selectedStudent.set(null);
  }
}