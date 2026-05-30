import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdminGebruiker, AdminGebruikerService } from '../services/AdminGebruikerService';

@Component({
  selector: 'app-beheerder-rollen',
  imports: [CommonModule, RouterLink],
  templateUrl: './beheerder-rollen.html',
  styleUrl: './beheerder-rollen.css',
})
export class BeheerderRollenComponent implements OnInit {
  private adminGebruikerService = inject(AdminGebruikerService);

  leerkrachten = signal<AdminGebruiker[]>([]);
  leerkrachtenLaden = signal<boolean>(false);
  promotieBezig = signal<number | null>(null);

  beheerderModalDoel = signal<AdminGebruiker | null>(null);
  beheerderModalFout = signal<string>('');

  toastActief = signal<boolean>(false);
  toastBericht = signal<string>('');
  toastType = signal<'succes' | 'fout'>('succes');

  ngOnInit(): void {
    this.laadLeerkrachten();
  }

  laadLeerkrachten(): void {
    this.leerkrachtenLaden.set(true);
    this.adminGebruikerService.getLeerkrachtenVanMijnSchool(0, 10000).subscribe({
      next: (page) => {
        this.leerkrachten.set(page.content ?? []);
        this.leerkrachtenLaden.set(false);
      },
      error: () => {
        this.leerkrachten.set([]);
        this.leerkrachtenLaden.set(false);
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
    this.adminGebruikerService.setBeheerderVanMijnSchool(leerkracht.id, nieuweWaarde).subscribe({
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
