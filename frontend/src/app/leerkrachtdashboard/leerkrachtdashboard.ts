import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { LeerlingService } from '../services/Leerling.service';

@Component({
  selector: 'app-leerkrachtdashboard',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './leerkrachtdashboard.html',
  styleUrl: './leerkrachtdashboard.css',
})
export class Leerkrachtdashboard implements OnInit {
  private leerlingService = inject(LeerlingService);

  // signals voor statusbeheer
  searchQuery = signal<string>('');
  selectedClass = signal<string>('');
  selectedStudent = signal<any | null>(null);
  
  // signals voor paginering
  huidigePagina = signal<number>(1);
  itemsPerPagina = 5;

  // signal voor de opgehaalde data
  students = signal<any[]>([]);

  // beschikbare klassen
  availableClasses = ['1a', '1b', '2a', '2b', '3a'];

  ngOnInit(): void {
    this.haalLeerlingenOp();
  }

  haalLeerlingenOp() {
    this.leerlingService.getLeerlingen().subscribe({
      next: (data) => {
        this.students.set(data);
      },
      error: (err) => console.error('Fout bij ophalen leerlingen:', err)
    });
  }

  // 1. computed signal: filter op zoekterm en klas
  filteredStudents = computed(() => {
    const query = this.searchQuery().toLowerCase();
    const klasFilter = this.selectedClass();

    return this.students().filter(student => {
      const fullName = `${student.firstName} ${student.lastName}`.toLowerCase();
      const matchesName = fullName.includes(query);
      const matchesClass = klasFilter ? student.className === klasFilter : true;

      return matchesName && matchesClass;
    });
  });

  // 2. computed signal: bereken totaal aantal pagina's
  totaalAantalPaginas = computed(() => {
    const totaal = Math.ceil(this.filteredStudents().length / this.itemsPerPagina);
    return totaal === 0 ? 1 : totaal;
  });

  // 3. computed signal: knip de lijst op voor de huidige pagina
  zichtbareLeerlingen = computed(() => {
    const startIndex = (this.huidigePagina() - 1) * this.itemsPerPagina;
    const eindIndex = startIndex + this.itemsPerPagina;
    return this.filteredStudents().slice(startIndex, eindIndex);
  });

  // reset paginering als filters veranderen
  onFilterChange() {
    this.huidigePagina.set(1);
  }

  vorigePagina(): void {
    if (this.huidigePagina() > 1) {
      this.huidigePagina.update(p => p - 1);
    }
  }

  volgendePagina(): void {
    if (this.huidigePagina() < this.totaalAantalPaginas()) {
      this.huidigePagina.update(p => p + 1);
    }
  }

  openHistory(student: any) {
    this.selectedStudent.set(student);
  }

  closeHistory() {
    this.selectedStudent.set(null);
  }
}