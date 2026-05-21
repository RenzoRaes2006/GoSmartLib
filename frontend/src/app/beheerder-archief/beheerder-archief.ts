import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { KlassLeeslijstService } from '../services/klassLeeslijstService';
import { KlassLeeslijst } from '../models/klass-leeslijst.model';

@Component({
  selector: 'app-beheerder-archief',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './beheerder-archief.html',
  styleUrl: './beheerder-archief.css',
})
export class BeheerderArchiefComponent implements OnInit {
  private leeslijstService = inject(KlassLeeslijstService);

  archief = signal<KlassLeeslijst[]>([]);
  laden = signal(false);
  fout = signal<string | null>(null);

  zoekterm = signal('');

  gefilterd = computed(() => {
    const term = this.zoekterm().trim().toLowerCase();
    if (!term) return this.archief();
    return this.archief().filter(l =>
      l.naam.toLowerCase().includes(term) ||
      (l.klas?.toLowerCase().includes(term) ?? false) ||
      (l.leerlingNaam?.toLowerCase().includes(term) ?? false)
    );
  });

  ngOnInit(): void {
    this.laadArchief();
  }

  private laadArchief(): void {
    this.laden.set(true);
    this.fout.set(null);
    this.leeslijstService.getArchief().subscribe({
      next: (data) => {
        this.archief.set(data);
        this.laden.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.fout.set(err.error ?? 'Kon archief niet laden.');
        this.laden.set(false);
      },
    });
  }
}
