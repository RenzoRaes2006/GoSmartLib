import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { KlassLeeslijst } from '../models/klass-leeslijst.model';
import { LeeslijstRequest } from '../models/leeslijst-request.model';
import { KlassLeeslijstService } from '../services/klassLeeslijstService';
import { LeerlingService } from '../services/Leerling.service';
import { FormsModule } from '@angular/forms';

type KoppelType = 'KLAS' | 'LEERLING';

@Component({
  selector: 'app-klass-leeslijsten-overzicht',
  standalone: true,
  imports: [RouterLink, FormsModule],
  templateUrl: './klass-leeslijsten-overzicht.html',
  styleUrl: './klass-leeslijsten-overzicht.css',
})
export class KlassLeeslijstenOverzichtComponent implements OnInit {
  private leeslijstService = inject(KlassLeeslijstService);
  private leerlingService = inject(LeerlingService);
  private router = inject(Router);

  leeslijsten: KlassLeeslijst[] = [];
  klassen: string[] = [];

  showCreateModal = false;
  nieuweLijstNaam = '';
  nieuweLijstBeschrijving = '';
  nieuweLijstZichtbaar = false;
  koppelType: KoppelType = 'KLAS';
  geselecteerdeKlas = '';

  leerlingKlas = '';
  leerlingResultaten: any[] = [];
  geselecteerdeLeerlingIds: number[] = [];

  laadFout = signal<string>('');

  ngOnInit(): void {
    this.laadLeeslijsten();
    this.laadKlassen();
  }

  laadLeeslijsten(): void {
    this.leeslijstService.getMijnLeerkrachtLijsten().subscribe({
      next: (data) => (this.leeslijsten = data),
      error: (err) => {
        console.error('Fout bij het laden van leeslijsten:', err);
        this.laadFout.set('Kon je leeslijsten niet laden.');
      },
    });
  }

  laadKlassen(): void {
    this.leerlingService.getKlassen().subscribe({
      next: (data) => (this.klassen = data),
      error: (err) => console.error('Fout bij ophalen klassen:', err),
    });
  }

  openNieuweLijstModal(): void {
    this.showCreateModal = true;
    this.resetModalState();
  }

  sluitModal(): void {
    this.showCreateModal = false;
    this.resetModalState();
  }

  private resetModalState(): void {
    this.nieuweLijstNaam = '';
    this.nieuweLijstBeschrijving = '';
    this.nieuweLijstZichtbaar = false;
    this.koppelType = 'KLAS';
    this.geselecteerdeKlas = '';
    this.leerlingKlas = '';
    this.leerlingResultaten = [];
    this.geselecteerdeLeerlingIds = [];
  }

  onLeerlingKlasChange(): void {
    this.geselecteerdeLeerlingIds = [];
    if (!this.leerlingKlas) {
      this.leerlingResultaten = [];
      return;
    }
    this.leerlingService.getLeerlingen(0, 50, this.leerlingKlas, '').subscribe({
      next: (page) => (this.leerlingResultaten = page.content || []),
      error: (err) => console.error('Fout bij ophalen leerlingen:', err),
    });
  }

  toggleLeerling(id: number): void {
    if (this.geselecteerdeLeerlingIds.includes(id)) {
      this.geselecteerdeLeerlingIds = this.geselecteerdeLeerlingIds.filter((i) => i !== id);
    } else {
      this.geselecteerdeLeerlingIds = [...this.geselecteerdeLeerlingIds, id];
    }
  }

  isLeerlingGeselecteerd(id: number): boolean {
    return this.geselecteerdeLeerlingIds.includes(id);
  }

  get kanBevestigen(): boolean {
    if (!this.nieuweLijstNaam.trim()) return false;
    if (this.koppelType === 'KLAS') return !!this.geselecteerdeKlas;
    return this.geselecteerdeLeerlingIds.length > 0;
  }

  bevestigNieuweLijst(): void {
    if (!this.kanBevestigen) return;

    const request: LeeslijstRequest = {
      naam: this.nieuweLijstNaam.trim(),
      klas: this.koppelType === 'KLAS' ? this.geselecteerdeKlas : null,
      leerlingIds: this.koppelType === 'LEERLING' && this.geselecteerdeLeerlingIds.length > 0 ? this.geselecteerdeLeerlingIds : null,
      beschrijving: this.nieuweLijstBeschrijving.trim() || null,
      zichtbaar: this.nieuweLijstZichtbaar,
    };

    this.leeslijstService.createLeeslijstVoorMij(request).subscribe({
      next: (nieuweLijst) => {
        this.sluitModal();
        this.router.navigate(['/leeslijsten', nieuweLijst.id]);
      },
      error: (err) => {
        console.error('Fout bij het aanmaken:', err);
        this.laadFout.set('Aanmaken mislukt. Controleer de gegevens en probeer opnieuw.');
      },
    });
  }
}
