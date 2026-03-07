import { Component, inject, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { BoekService } from '../services/boek.service';
import { Boek } from '../models/Boek';
import { ReviewService } from '../services/review-service';
import { Beoordeling } from '../models/review.model';
import { LeeslijstService } from '../services/leeslijstService';

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
  leeslijstService = inject(LeeslijstService);
  boek: Boek | null = null;
  form!: FormGroup;
  submissionError: string = '';
  boekGenresString: string = '';
  beoordelingen: Beoordeling[] = [];

  ngOnInit(): void {
    this.form = this.fb.group({
      waardering: ['', [Validators.required]],
      inhoud: ['', [Validators.required, Validators.maxLength(500)]],
    });

    const boekId = Number(this.route.snapshot.paramMap.get('id'));
    this.boekService.getBoekOpId(boekId).subscribe({
      next: (boek) => {
        console.log('Boek details:', boek);
        this.boek = boek;
        this.boekGenresString = boek.genre.join(', ');
      },
      error: (err) => console.log('Iets mis gegaan met het krijgen van de boek details. ', err),
    });

    this.beoordelingService.getBeoordelingenOpBoekId(boekId).subscribe({
      next: (beoordelingen) => {
        this.beoordelingen = beoordelingen;
      },
      error: (err) => console.log('Iets mis gegaan met het krijgen van de beoordelingen. ', err),
    });

    // ik kan later deze manier gebruken als ik van deze pagina naar een andere pagina van boekdetails ga en terug, zodat ik de details en beoordelingen opnieuw kan laden
    // this.route.paramMap.subscribe(params => {
    //   const boekId = Number(params.get('id'));
    //   // Roep hier boekService en beoordelingService aan met boekId
    // });
  }

  voegToeAanFavorieten() {
    if (!this.boek) return;

    const gebruikerId = 1; // TODO: dynamisch maken via inlog smartschool
    this.leeslijstService.voegBoekToeAanMap(gebruikerId, 'Favorieten', this.boek.id!).subscribe({
      next: (response) => {
        alert('Boek succesvol toegevoegd aan je Favorieten!');
      },
      error: (err) => {
        console.error('Fout bij toevoegen:', err);
        alert('Er ging iets mis bij het toevoegen aan favorieten.');
      },
    });
  }

  voegToeAanLeeslijst() {
    if (!this.boek) return;

    const mapNaam = prompt(
      'Aan welk mapje wil je dit boek toevoegen? (Bijv. Schoolboeken, Wil ik lezen)',
      'Wil ik lezen',
    );

    if (mapNaam && mapNaam.trim() !== '') {
      const gebruikerId = 1; // TODO:  dynamisch maken via inlog smartschool
      this.leeslijstService
        .voegBoekToeAanMap(gebruikerId, mapNaam.trim(), this.boek.id!)
        .subscribe({
          next: (response) => {
            alert(`Boek succesvol toegevoegd aan mapje: ${mapNaam}!`);
          },
          error: (err) => {
            console.error('Fout bij toevoegen:', err);
            alert('Er ging iets mis bij het toevoegen aan de leeslijst.');
          },
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
      },
      error: (error) => {
        console.error('Fout bij het toevoegen van beoordeling:', error);
        this.submissionError = 'Er ging iets mis bij het toevoegen. Probeer opnieuw.';
      },
    });
  }
}
