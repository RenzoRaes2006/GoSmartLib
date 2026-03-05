import { Component, inject, OnInit } from '@angular/core';
import {
  FormBuilder,
<<<<<<< HEAD
=======
  FormControl,
>>>>>>> b355cb2e211c6af08de288b29a7547bfeb4829f1
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { BoekService } from '../services/boek.service';
import { Boek } from '../models/Boek';
import { ReviewService } from '../services/review-service';
import { Beoordeling } from '../models/review.model';

@Component({
  selector: 'app-detail',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './detail.html',
  styleUrl: './detail.css',
})
export class Detail implements OnInit {
  beoordelingService = inject(ReviewService);
  boekService = inject(BoekService);
  fb = inject(FormBuilder);
  route = inject(ActivatedRoute);

  boek: Boek | null = null;
  form!: FormGroup;
  submissionError: string = '';
  beoordelingen: Beoordeling[] = [];
  boekWaardering: number = 0;
  waarderingPrecentage: number = 0;

  ngOnInit(): void {
    this.form = this.fb.group({
      waardering: ['', [Validators.required]],
      inhoud: ['', [Validators.required, Validators.maxLength(500)]],
    });

    // ik gebruik hier geen snapshot omdat ik wil dat de code altijd de juiste boek details en beoordelingen ophaalt, ook als de gebruiker al op de detail pagina is en een ander boek selecteert. Met snapshot zou de code alleen de details van het eerste boek ophalen en niet meer reageren op veranderingen in de route parameters.
    this.route.paramMap.subscribe(params => {
      const boekId = Number(params.get('id'));

      this.boekService.getBoekOpId(boekId).subscribe({
        next: (boek) => {
          console.log('Boek details:', boek);
          this.boek = boek;
        },
        error: (err) => console.log('Iets mis gegaan met het krijgen van de boek details. ', err),
      });

      this.beoordelingService.getBeoordelingenOpBoekId(boekId).subscribe({
        next: (beoordelingen) => {
          this.beoordelingen = beoordelingen;
          // Bereken de gemiddelde waardering van de beoordelingen
          this.updateBoekWaardering();
        },
        error: (err) => console.log('Iets mis gegaan met het krijgen van de beoordelingen. ', err),
      });
    });
  }

  updateBoekWaardering() {
    if (this.beoordelingen.length > 0) {
      const totaleWaardering = this.beoordelingen.reduce((sum, beoordeling) => sum + beoordeling.waardering, 0);
      const gemiddelde = totaleWaardering / this.beoordelingen.length;
      this.boekWaardering = Number(gemiddelde.toFixed(1)); // Rond af op 1 decimaal
      this.waarderingPrecentage = (this.boekWaardering / 5) * 100; // Bereken percentage voor de sterwaardering
    }
  }
  submitBeoordeling() {
    if (this.form.invalid) {
      this.submissionError =
        'Vul alle verplichte velden in en zorg dat je commentaar niet langer is dan 500 karakters.';
      return;
    }
    const raw = this.form.value;
    const beoordeling: Beoordeling = {
      gebruikerId: 1, // TODO: haal dit van de ingelogde gebruiker
      boekId: this.boek?.id ?? 0,
      waardering: Number(raw.waardering),
      inhoud: raw.inhoud,
    };

    console.log('Ingediende beoordeling:', beoordeling);

    this.beoordelingService.voegBeoordelingToe(beoordeling).subscribe({
      next: (response) => {
        console.log('Beoordeling succesvol toegevoegd:', response);
        this.form.reset();
        this.submissionError = '';
        this.beoordelingen.push(response); // Voeg de nieuwe beoordeling toe aan de lijst zodat deze direct zichtbaar is
        this.updateBoekWaardering(); // Werk de gemiddelde waardering bij na het toevoegen van een nieuwe beoordeling
      },
      error: (error) => {
        console.error('Fout bij het toevoegen van beoordeling:', error);
        this.submissionError = 'Er ging iets mis bij het toevoegen. Probeer opnieuw.';
      },
    });
  }
}
