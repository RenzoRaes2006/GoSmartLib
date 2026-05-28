import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdminGebruiker } from '../services/AdminGebruikerService';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-beheerder-rollen',
  imports: [CommonModule, RouterLink],
  templateUrl: './beheerder-rollen.html',
  styleUrl: './beheerder-rollen.css',
})
export class BeheerderRollenComponent implements OnInit {
  private http = inject(HttpClient);

  private apiUrl = window.location.hostname === 'localhost'
    ? 'http://localhost:8081/api/beheer'
    : 'https://api-sp09.cloud-ap.be/api/beheer';

  leerkrachten = signal<AdminGebruiker[]>([]);
  laden = signal<boolean>(true);

  beheerderModalDoel = signal<AdminGebruiker | null>(null);
  beheerderModalFout = signal<string>('');
  promotieBezig = signal<number | null>(null);

  toastActief = signal<boolean>(false);
  toastBericht = signal<string>('');
  toastType = signal<'succes' | 'fout'>('succes');

  ngOnInit(): void {
    this.laadLeerkrachten();
  }

  laadLeerkrachten(): void {
    this.laden.set(true);
    this.http.get<AdminGebruiker[]>(`${this.apiUrl}/leerkrachten`).subscribe({
      next: (lijst) => {
        this.leerkrachten.set(lijst ?? []);
        this.laden.set(false);
      },
      error: () => {
        this.leerkrachten.set([]);
        this.laden.set(false);
      },
    });
  }

  openBeheerderModal(leerkracht: AdminGebruiker): void {
    this.beheerderModalFout.set('');
    this.beheerderModalDoel.set(leerkracht);
  }

  annuleerBeheerderModal(): void {
    if (this.promotieBezig() !== null) return;
    this.beheerderModalDoel.set(null);
    this.beheerderModalFout.set('');
  }

  bevestigBeheerderToggle(): void {
    const leerkracht = this.beheerderModalDoel();
    if (!leerkracht) return;
    const nieuweWaarde = !leerkracht.isBeheerder;

    this.promotieBezig.set(leerkracht.id);
    this.beheerderModalFout.set('');

    this.http.patch<AdminGebruiker>(
      `${this.apiUrl}/gebruikers/${leerkracht.id}/beheerder`,
      { isBeheerder: nieuweWaarde }
    ).subscribe({
      next: (geupdate) => {
        this.leerkrachten.update(lijst =>
          lijst.map(l => (l.id === geupdate.id ? geupdate : l))
        );
        this.promotieBezig.set(null);
        this.beheerderModalDoel.set(null);
        this.toonToast(
          nieuweWaarde
            ? `${geupdate.naam || geupdate.smartschoolId} is nu beheerder.`
            : `Beheerder-rol ingetrokken voor ${geupdate.naam || geupdate.smartschoolId}.`,
          'succes'
        );
      },
      error: () => {
        this.beheerderModalFout.set('Kan de status niet wijzigen. Probeer opnieuw.');
        this.promotieBezig.set(null);
      },
    });
  }

  toonToast(bericht: string, type: 'succes' | 'fout'): void {
    this.toastBericht.set(bericht);
    this.toastType.set(type);
    this.toastActief.set(true);
    setTimeout(() => this.toastActief.set(false), 3000);
  }
}
