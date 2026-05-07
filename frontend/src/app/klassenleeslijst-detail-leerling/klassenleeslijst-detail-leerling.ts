import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { KlassLeeslijstService } from '../services/klassLeeslijstService';
import { ActivatedRoute } from '@angular/router';
import { KlassLeeslijst } from '../models/klass-leeslijst.model';

@Component({
  selector: 'app-klassenleeslijst-detail-leerling',
  imports: [CommonModule, RouterModule],
  templateUrl: './klassenleeslijst-detail-leerling.html',
  styleUrl: './klassenleeslijst-detail-leerling.css',
})
export class KlassenleeslijstDetailLeerlingComponent implements OnInit {
  lijst = signal<KlassLeeslijst | undefined>(undefined);

  private route = inject(ActivatedRoute);
  private leeslijstService = inject(KlassLeeslijstService);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.leeslijstService.getLeeslijstDetails(id).subscribe({
      next: (data) => this.lijst.set(data),
      error: (err) => console.error('Fout bij laden lijst:', err)
    });
  }
}
