import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ReviewService } from './review-service';
import { Review } from './review.model';

@Component({
  selector: 'app-detail',
  imports: [ReactiveFormsModule],
  templateUrl: './detail.html',
  styleUrl: './detail.css',
})
export class Detail implements OnInit {
  reviewService = inject(ReviewService);
  form!: FormGroup;
  fb = inject(FormBuilder);
  submissionError: string = "";

  ngOnInit(): void {
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
    const review: Review = {
      userId: 1, // TODO: haal dit van de ingelogde gebruiker
      bookId: 1, // TODO: haal dit van de huidige boek detail pagina
      rating: Number(raw.rating),
      content: raw.comment,
    };

    this.reviewService.addReview(review).subscribe({
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