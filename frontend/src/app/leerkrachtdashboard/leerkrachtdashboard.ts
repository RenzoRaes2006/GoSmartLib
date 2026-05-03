import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LeerlingService } from '../services/Leerling.service'; // Let op: kleine letter l bij leerling.service

@Component({
  selector: 'app-leerkrachtdashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './leerkrachtdashboard.html',
  styleUrl: './leerkrachtdashboard.css',
})
export class Leerkrachtdashboard implements OnInit {
  private leerlingService = inject(LeerlingService);

  huidigePagina = signal<number>(0);
  itemsPerPagina = 5;
  totaalAantalPaginas = signal<number>(0);
  selectedClass = signal<string>('');
  zoekterm = signal<string>('');
  leerlingStats = signal<any>(null);

  zichtbareLeerlingen = signal<any[]>([]);
  selectedStudent = signal<any | null>(null);

  ngOnInit(): void {
    this.haalLeerlingenOp();
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
  this.leerlingService.getLeerlingStats(student.id).subscribe({
    next: (stats) => this.leerlingStats.set(stats),
    error: (err) => console.error('fout bij laden stats', err)
  });
}

closeHistory() {
  this.selectedStudent.set(null);
  this.leerlingStats.set(null);
}

  
}