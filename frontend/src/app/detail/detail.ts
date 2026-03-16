import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, NgModel, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { BoekService } from '../services/boek.service';
import { Boek } from '../models/Boek';
import { ReviewService } from '../services/review-service';
import { Beoordeling } from '../models/review.model';
import { LeeslijstService } from '../services/leeslijstService';

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

  boek: Boek | null = null;
  form!: FormGroup;
  submissionError: string = '';
  beoordelingen: Beoordeling[] = [];

  isModalOpen: boolean = false;
  isFavoriet: boolean = false;
  succesMeldingMap: string = '';
  mappen: any[] = [];

  boekWaardering: number = 0;
  waarderingPrecentage: number = 0;

  rapportMelding: string = '';
  rapporteerModalOpen: boolean = false;
  rapporteerReden: string = '';
  geselecteerdeBeoordelingId: number | null = null;


  ngOnInit(): void {
    this.form = this.fb.group({
      waardering: ['', [Validators.required]],
      inhoud: ['', [Validators.required, Validators.maxLength(500)]],
    });

    this.route.paramMap.subscribe((params) => {
      const boekId = Number(params.get('id'));

      this.boekService.getBoekOpId(boekId).subscribe({
        next: (boek) => {
          this.boek = boek;
        },
        error: (err) => console.log('Iets mis gegaan met het krijgen van de boek details. ', err),
      });

      this.beoordelingService.getBeoordelingenOpBoekId(boekId).subscribe({
        next: (beoordelingen) => {
          this.beoordelingen = beoordelingen;
          this.updateBoekWaardering();
        },
        error: (err) => console.log('Iets mis gegaan met het krijgen van de beoordelingen. ', err),
      });
    });

    const gebruikerId = 1;
    this.leeslijstService.getMappenVanGebruiker(gebruikerId).subscribe({
      next: (data) => {
        this.mappen = data;
      },
    });
  }

  updateBoekWaardering() {
    if (this.beoordelingen.length > 0) {
      const totaleWaardering = this.beoordelingen.reduce(
        (sum, beoordeling) => sum + beoordeling.waardering,
        0,
      );
      const gemiddelde = totaleWaardering / this.beoordelingen.length;
      this.boekWaardering = Number(gemiddelde.toFixed(1));
      this.waarderingPrecentage = (this.boekWaardering / 5) * 100;
    }
  }

  voegToeAanFavorieten() {
    if (!this.boek) return;
    const gebruikerId = 1;
    this.leeslijstService.voegBoekToeAanMap(gebruikerId, 'Favorieten', this.boek.id!).subscribe({
      next: () => {
        this.isFavoriet = true;
        setTimeout(() => (this.isFavoriet = false), 3000);
      },
    });
  }

  voegToeAanLeeslijst() {
    if (!this.boek) return;
    this.isModalOpen = true;
  }

  sluitModal() {
    this.isModalOpen = false;
    this.succesMeldingMap = '';
  }

  bevestigToevoegenAanMap(bestaandeMap: string, nieuweMap: string) {
    const doelMap = nieuweMap.trim() !== '' ? nieuweMap.trim() : bestaandeMap;

    if (doelMap && doelMap !== '') {
      const gebruikerId = 1;
      this.leeslijstService.voegBoekToeAanMap(gebruikerId, doelMap, this.boek!.id!).subscribe({
        next: () => {
          this.succesMeldingMap = `Boek toegevoegd aan '${doelMap}'!`;
          this.leeslijstService
            .getMappenVanGebruiker(gebruikerId)
            .subscribe((data) => (this.mappen = data));
          setTimeout(() => this.sluitModal(), 1500);
        },
        error: () => (this.succesMeldingMap = 'Er ging iets mis...'),
      });
    }
  }

  submitBeoordeling() {
    if (this.form.invalid) {
      this.submissionError =
        'Vul alle verplichte velden in en zorg dat je commentaar niet langer is dan 500 karakters.';
      return;
    }
    const raw = this.form.value;
    const beoordeling: Omit<Beoordeling, 'id'> = {
      gebruikerId: 1,
      boekId: this.boek?.id ?? 0,
      waardering: Number(raw.waardering),
      inhoud: raw.inhoud,
    };

    this.beoordelingService.voegBeoordelingToe(beoordeling).subscribe({
      next: (response) => {
        this.form.reset();
        this.submissionError = '';
        this.beoordelingen.push(response);
        this.updateBoekWaardering();
      },
      error: (error) => {
        this.submissionError = 'Er ging iets mis bij het toevoegen. Probeer opnieuw.';
      },
    });
  }

  bevestigRapport() {
    this.beoordelingService.rapporteerBeoordeling(this.geselecteerdeBeoordelingId!, this.rapporteerReden).subscribe({
      next: () => {
        this.rapportMelding = 'Beoordeling succesvol gerapporteerd. Bedankt voor je feedback!';

        this.rapporteerModalOpen = false;
        this.geselecteerdeBeoordelingId = null;
        this.rapporteerReden = '';

        setTimeout(() => this.rapportMelding = '', 4000);
      },
      error: () => {
        this.rapportMelding = 'Er ging iets mis bij het rapporteren. Probeer opnieuw.';
      },
    });
  }

  openRapporteerModal(beoordelingId: number) {
    this.rapporteerModalOpen = true;
    this.geselecteerdeBeoordelingId = beoordelingId;
  }

  sluitRapporteerModal() {
    this.rapporteerModalOpen = false;
    this.geselecteerdeBeoordelingId = null;
    this.rapporteerReden = '';
    this.rapportMelding = '';
  }
}
