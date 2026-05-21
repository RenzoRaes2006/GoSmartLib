import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { LeeslijstService } from '../services/leeslijstService';
import { AuthService } from '../services/AuthService';

@Component({
  selector: 'app-leeslijst',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './leeslijst.html',
  styleUrl: './leeslijst.css',
})
export class Leeslijst implements OnInit {
  leeslijstService = inject(LeeslijstService);
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);
  mappen: any[] = [];
  geselecteerdeMap: any = null;
  isLoading: boolean = true;

  get isLeerling(): boolean {
    const role = (this.authService.currentUser()?.role ?? '').toLowerCase();
    return role === 'student' || role === 'leerling';
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const autoOpenId = params.get('id');
      this.haalMappenOp(autoOpenId);
    });
  }

  haalMappenOp(autoOpenMapId: string | null = null) {
    this.isLoading = true;
    this.leeslijstService.getMijnMappen().subscribe({
      next: (data) => {
        const mapLijst = data || [];

        let gefilterdeMappen = mapLijst.filter(
          (map: any) =>
            (map.boeken && map.boeken.length > 0) || map.naam.toLowerCase() === 'favorieten',
        );

        const heeftFavorieten = gefilterdeMappen.some(
          (m: any) => m.naam.toLowerCase() === 'favorieten',
        );
        if (!heeftFavorieten) {
          gefilterdeMappen.unshift({ id: 'fav', naam: 'Favorieten', boeken: [] });
        }

        this.mappen = gefilterdeMappen;
        this.isLoading = false;

        if (autoOpenMapId) {
          const gevonden = this.mappen.find((m: any) => String(m.id) === String(autoOpenMapId));
          if (gevonden) {
            this.geselecteerdeMap = gevonden;
          }
        }
      },
      error: (err) => {
        console.error('Fout bij ophalen mappen', err);
        this.mappen = [{ id: 'fav', naam: 'Favorieten', boeken: [] }];
        this.isLoading = false;
      },
    });
  }
  openMap(map: any) {
    this.geselecteerdeMap = map;
  }

  // Terugknop naar het album-overzicht
  terugNaarOverzicht() {
    this.geselecteerdeMap = null;
    this.haalMappenOp();
  }

  // Verwijder boek
  verwijderBoek(boekId: number, event: Event) {
    event.stopPropagation();
    if (!this.geselecteerdeMap) return;

    const mapNaam = this.geselecteerdeMap.naam;

    this.leeslijstService.verwijderBoekUitMijnMap(mapNaam, boekId).subscribe({
      next: () => {
        this.geselecteerdeMap.boeken = this.geselecteerdeMap.boeken.filter(
          (b: any) => b.id !== boekId,
        );

        if (this.geselecteerdeMap.boeken.length === 0) {
          if (this.geselecteerdeMap.naam.toLowerCase() !== 'favorieten') {
            this.mappen = this.mappen.filter((m) => m.id !== this.geselecteerdeMap.id);
          }
          this.geselecteerdeMap = null;
        }
      },
      error: (err) => console.error('Fout bij verwijderen', err),
    });
  }
}
