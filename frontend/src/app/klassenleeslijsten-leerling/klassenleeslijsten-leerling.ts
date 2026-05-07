import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { KlassLeeslijstService } from '../services/klassLeeslijstService';
import { KlassLeeslijst } from '../models/klass-leeslijst.model';

@Component({
  selector: 'app-klassenleeslijsten-leerling',
  imports: [CommonModule, RouterModule],
  templateUrl: './klassenleeslijsten-leerling.html',
  styleUrl: './klassenleeslijsten-leerling.css',
})
export class KlassenleeslijstenLeerlingComponent implements OnInit {
  leeslijsten: KlassLeeslijst[] = [];
  private leeslijstService = inject(KlassLeeslijstService);

  ngOnInit(): void {
    this.leeslijstService.getAllKlassenLeeslijsten().subscribe({
      next: (data) => this.leeslijsten = data,
      error: (err) => console.error('Fout bij ophalen lijsten:', err)
    });
  }

}
