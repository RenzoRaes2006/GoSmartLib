import { Component, inject, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { BoekService } from '../services/boek.service';
import { Boek } from '../models/Boek';
import { ReviewService } from '../services/review-service';
import { Beoordeling } from '../models/review.model';
import { LeeslijstService } from '../services/leeslijstService';
import { LeenMandjeService } from '../services/leen-mandje.service';

@Component({
  selector: 'app-detail',
  imports: [ReactiveFormsModule, RouterLink, FormsModule],
  templateUrl: './detail.html',
  styleUrl: './detail.css',
})
export class Detail implements OnInit {
  beoordelingService = inject(ReviewService);
  boekService = inject(BoekService);
  fb = inject(FormBuilder);
  route = inject(ActivatedRoute);
  leeslijstService = inject(LeeslijstService);
  leenMandjeService = inject(LeenMandjeService);

  boek: Boek | null = null;
  form!: FormGroup;
  submissionError: string = '';
  beoordelingen: Beoordeling[] = [];

  isModalOpen: boolean = false;
  isFavoriet: boolean = false;
  succesMeldingMap: string = '';
  mappen: any[] = [];

  // 🚨 NIEUW: De emoji picker logica
  beschikbareEmojis = ['📚', '🎓', '🧠', '💡', '🎨', '🌍', '🔥', '❤️'];
  selectedEmoji = '📚';

  magLenen: boolean = false;
  leenReden: string = 'Status ophalen...';
  isLeenModalOpen: boolean = false;
  succesMeldingLenen: string = '';

  boekWaardering: number = 0;
  waarderingPrecentage: number = 0;

  rapportMelding: string = '';
  rapporteerModalOpen: boolean = false;
  rapporteerReden: string = '';
  geselecteerdeBeoordelingId: number | null = null;
  gerapporteerdeBeoordelingen: number[] = [];

  boekId: number = 0;

  ngOnInit(): void {
    this.form = this.fb.group({
      waardering: ['', [Validators.required]],
      inhoud: ['', [Validators.required, Validators.maxLength(500)]],
    });

    this.route.paramMap.subscribe((params) => {
      const boekId = Number(params.get('id'));
      this.boekId = boekId;

      this.boekService.getBoekOpId(boekId).subscribe({
        next: (boek) => {
          this.boek = boek;
          const gebruikerId = 1;
          this.boekService.checkLeenStatus(gebruikerId, boekId).subscribe({
            next: (status) => {
              this.magLenen = status.magLenen;
              this.leenReden = status.reden;
            },
            error: () => (this.leenReden = 'Kon uitleen status niet ophalen.'),
          });
        },
      });
      this.laadBeoordelingVanBoek();
    });

    const gebruikerId = 1;
    this.leeslijstService.getMappenVanGebruiker(gebruikerId).subscribe({
      next: (data) => {
        // 🚨 FIX: We filteren de "Favorieten" map er direct uit!
        this.mappen = data.filter((map) => map.naam.toLowerCase() !== 'favorieten');
      },
    });
  }

  laadBeoordelingVanBoek() {
    this.beoordelingService.getBeoordelingenOpBoekId(this.boekId).subscribe({
      next: (beoordelingen) => {
        this.beoordelingen = beoordelingen;
        this.updateBoekWaardering();
      },
    });
  }

  updateBoekWaardering() {
    if (this.beoordelingen.length > 0) {
      const totaleWaardering = this.beoordelingen.reduce(
        (sum, beoordeling) => sum + beoordeling.waardering,
        0,
      );
      this.boekWaardering = Number((totaleWaardering / this.beoordelingen.length).toFixed(1));
      this.waarderingPrecentage = (this.boekWaardering / 5) * 100;
    }
  }

  voegToeAanFavorieten() {
    if (!this.boek || this.isFavoriet) return;
    const gebruikerId = 1;
    // 🚨 FIX: De timeout is weg. Zodra je klikt, blijft hij op Opgeslagen staan!
    this.leeslijstService
      .voegBoekToeAanMap(gebruikerId, 'Favorieten', this.boek.id!, '⭐')
      .subscribe({
        next: () => {
          this.isFavoriet = true;
        },
      });
  }

  voegToeAanLeeslijst() {
    if (!this.boek) return;
    this.isModalOpen = true;
    this.selectedEmoji = '📚'; // Reset de emoji als je de modal opent
  }

  sluitModal() {
    this.isModalOpen = false;
    this.succesMeldingMap = '';
  }

  bevestigToevoegenAanMap(bestaandeMap: string, nieuweMap: string) {
    const doelMap = nieuweMap.trim() !== '' ? nieuweMap.trim() : bestaandeMap;

    if (doelMap && doelMap !== '') {
      const gebruikerId = 1;
      // Stuur de geselecteerde emoji mee!
      this.leeslijstService
        .voegBoekToeAanMap(gebruikerId, doelMap, this.boek!.id!, this.selectedEmoji)
        .subscribe({
          next: () => {
            this.succesMeldingMap = `Boek toegevoegd aan '${doelMap}'!`;
            this.leeslijstService.getMappenVanGebruiker(gebruikerId).subscribe((data) => {
              this.mappen = data.filter((map) => map.naam.toLowerCase() !== 'favorieten');
            });
            setTimeout(() => this.sluitModal(), 1500);
          },
          error: () => (this.succesMeldingMap = 'Er ging iets mis...'),
        });
    }
  }

  submitBeoordeling() {
    if (this.form.invalid) {
      this.submissionError = 'Vul alle verplichte velden in (max 500 karakters).';
      return;
    }
    const raw = this.form.value;
    const beoordeling: Omit<Beoordeling, 'id'> = {
      gebruikerId: 1,
      boekId: this.boek?.id ?? 0,
      waardering: Number(raw.waardering),
      inhoud: raw.inhoud,
      gerapporteerd: false,
    };

    this.beoordelingService.voegBeoordelingToe(beoordeling).subscribe({
      next: () => {
        this.form.reset();
        this.submissionError = '';
        this.laadBeoordelingVanBoek();
      },
    });
  }

  openLeenModal() {
    if (this.magLenen) this.isLeenModalOpen = true;
  }

  sluitLeenModal() {
    this.isLeenModalOpen = false;
    this.succesMeldingLenen = '';
  }

  voegToeAanReservering() {
    if (!this.boek) return;
    const resultaat = this.leenMandjeService.voegBoekToe(this.boek);
    this.succesMeldingLenen =
      resultaat === 'Succes'
        ? `'${this.boek.titel}' is toegevoegd aan je reserveringen!`
        : resultaat;
    this.isLeenModalOpen = true;
    setTimeout(() => this.sluitLeenModal(), 2500);
  }

  bevestigRapport() {
    this.beoordelingService
      .rapporteerBeoordeling(this.geselecteerdeBeoordelingId!, this.rapporteerReden)
      .subscribe({
        next: () => {
          const beoordeling = this.beoordelingen.find(
            (b) => b.id === this.geselecteerdeBeoordelingId,
          );
          if (beoordeling) beoordeling.gerapporteerd = true;

          this.rapportMelding = 'Beoordeling succesvol gerapporteerd.';
          this.sluitRapporteerModal();
          setTimeout(() => (this.rapportMelding = ''), 4000);
        },
      });
  }

  openRapporteerModal(beoordelingId: number) {
    this.rapporteerModalOpen = true;
    this.geselecteerdeBeoordelingId = beoordelingId;
  }

  sluitRapporteerModal() {
    this.rapporteerModalOpen = false;
    this.rapporteerReden = '';
  }
}
