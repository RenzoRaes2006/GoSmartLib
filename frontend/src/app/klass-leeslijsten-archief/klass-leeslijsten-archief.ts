import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { KlassLeeslijstService } from '../services/klassLeeslijstService';
import { KlassLeeslijst } from '../models/klass-leeslijst.model';

@Component({
  selector: 'app-klass-leeslijsten-archief',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './klass-leeslijsten-archief.html',
  styleUrl: './klass-leeslijsten-archief.css',
})
export class KlassLeeslijstenArchiefComponent implements OnInit {
  private leeslijstService = inject(KlassLeeslijstService);
  private router = inject(Router);

  archief = signal<KlassLeeslijst[]>([]);
  laden = signal(false);
  fout = signal<string | null>(null);
  bezigMetId = signal<number | null>(null);
  succesMelding = signal<string | null>(null);

  ngOnInit(): void {
    this.laadArchief();
  }

  laadArchief(): void {
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

  dearchiveer(lijst: KlassLeeslijst): void {
    const bevestigd = confirm(
      `Wil je "${lijst.naam}" dearchiveren? De lijst komt terug bij je actieve leeslijsten als concept (niet zichtbaar voor leerlingen).`
    );
    if (!bevestigd) return;

    this.bezigMetId.set(lijst.id);
    this.fout.set(null);
    this.leeslijstService.dearchiveer(lijst.id).subscribe({
      next: () => {
        this.archief.update(list => list.filter(l => l.id !== lijst.id));
        this.bezigMetId.set(null);
        this.succesMelding.set(`"${lijst.naam}" is gedearchiveerd. Je vindt hem terug als concept in je leeslijsten.`);
        setTimeout(() => this.succesMelding.set(null), 5000);
      },
      error: (err: HttpErrorResponse) => {
        this.fout.set(err.error ?? 'Dearchiveer mislukt.');
        this.bezigMetId.set(null);
      },
    });
  }
}
