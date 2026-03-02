import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { ReviewService } from './review-service';
import { Beoordeling } from './review.model';
import { BoekService } from '../services/boek.service';
import { Boek } from '../models/Boek';

@Component({
  selector: 'app-detail',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './detail.html',
  styleUrl: './detail.css',
})
export class Detail implements OnInit {
  reviewService = inject(ReviewService);
  boekService = inject(BoekService);
  fb = inject(FormBuilder);
  route = inject(ActivatedRoute);

  boek: Boek | null = null;
  form!: FormGroup;
  submissionError: string = "";
  boekGenresString: string = "";

  ngOnInit(): void {
    const boekId = Number(this.route.snapshot.paramMap.get('id'));
    this.boekService.getBoekOpId(boekId).subscribe({
      next: (boek) => {
        console.log('Boek details:', boek);
        this.boek = boek;
        this.boekGenresString = boek.genre.join(', ');
      },
      error: (err) => console.log("Iets mis gegaan met het krijgen van de boek details. ", err)
    });

    this.form = this.fb.group({
      rating: ['', [Validators.required]],
      comment: ['', [Validators.required, Validators.maxLength(500)]],
    });
  }

  submitReview() {
    if (this.form.invalid) {
      this.submissionError = "Vul alle verplichte velden in en zorg dat je commentaar niet langer is dan 500 karakters.";
      return;
    }
    const raw = this.form.value;
    const review: Beoordeling = {
      gebruikerId: 1, // TODO: haal dit van de ingelogde gebruiker
      boekId: this.boek?.id ?? 0,
      waardering: Number(raw.rating),
      inhoud: raw.comment,
    };

    console.log('Ingediende review:', review);
    
    this.reviewService.voegBeoordelingToe(review).subscribe({
      next: (response) => {
        console.log('Review succesvol toegevoegd:', response);
        this.form.reset();
        this.submissionError = "";
      },
      error: (error) => {
        console.error('Fout bij het toevoegen van review:', error);
        this.submissionError = "Er ging iets mis bij het toevoegen. Probeer opnieuw.";
      }
    });
  }

}