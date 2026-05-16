import { Component, computed, inject, OnInit, signal } from '@angular/core';
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
  private leeslijstService = inject(KlassLeeslijstService);

  leeslijsten = signal<KlassLeeslijst[]>([]);
  fout = signal<string>('');

  klasLijsten = computed(() => this.leeslijsten().filter((l) => !!l.klas));
  persoonlijkeLijsten = computed(() => this.leeslijsten().filter((l) => !!l.leerlingId));

  ngOnInit(): void {
    this.leeslijstService.getLeeslijstenVoorMij().subscribe({
      next: (data) => this.leeslijsten.set(data),
      error: (err) => {
        console.error('Fout bij ophalen lijsten:', err);
        this.fout.set('Kon de leeslijsten niet laden.');
      },
    });
  }
}
