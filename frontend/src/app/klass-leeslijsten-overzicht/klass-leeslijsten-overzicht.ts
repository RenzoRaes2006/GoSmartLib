import { Component } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { KlassLeeslijst } from '../models/klass-leeslijst.model';
import { KlassLeeslijstService } from '../services/klassLeeslijstService';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-klass-leeslijsten-overzicht',
  standalone: true,
  imports: [RouterLink, FormsModule],
  templateUrl: './klass-leeslijsten-overzicht.html',
  styleUrl: './klass-leeslijsten-overzicht.css',
})
export class KlassLeeslijstenOverzichtComponent {
  leeslijsten: KlassLeeslijst[] = [];
  leerkrachtId = 1; // Tijdelijk hardcoded, later uit je auth-service halen

  showCreateModal: boolean = false;
  nieuweLijstNaam: string = '';

  constructor(private leeslijstService: KlassLeeslijstService, private router: Router) { }

  ngOnInit(): void {
    this.laadLeeslijsten();
  }

  laadLeeslijsten(): void {
    this.leeslijstService.getLeeslijstenByLeerkracht(this.leerkrachtId)
      .subscribe({
        next: (data) => {
          this.leeslijsten = data;
        },
        error: (err) => {
          console.error('Fout bij het laden van leeslijsten:', err);
        }
      });
  }

  openNieuweLijstModal(): void {
    this.showCreateModal = true;
    this.nieuweLijstNaam = '';
  }

  sluitModal(): void {
    this.showCreateModal = false;
    this.nieuweLijstNaam = '';
  }

  bevestigNieuweLijst(): void {
    if (!this.nieuweLijstNaam.trim()) return;

    this.leeslijstService.createLeeslijst(this.leerkrachtId, { naam: this.nieuweLijstNaam.trim() })
      .subscribe({
        next: (nieuweLijst) => {
          this.sluitModal();
          this.router.navigate(['/leeslijsten', nieuweLijst.id]);
        },
        error: (err) => {
          console.error('Fout bij het aanmaken:', err);
        }
      });
  }
}
