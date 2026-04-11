import { Component, inject, OnInit } from '@angular/core';
import { GerapporteerdeBeoordelingAdmin } from '../models/gerapporteerde-beoordeling-repository';
import { ReviewService } from '../services/review-service';
import { RapportageService } from '../services/rapportage-service';

@Component({
  selector: 'app-gerapporteerde-reviews',
  imports: [],
  templateUrl: './gerapporteerde-reviews.html',
  styleUrl: './gerapporteerde-reviews.css',
})
export class GerapporteerdeReviews implements OnInit {
  reviewService = inject(ReviewService);
  rapportageService = inject(RapportageService);
  rapportages: GerapporteerdeBeoordelingAdmin[] = [];

  ngOnInit(): void {
    this.rapportageService.getRapportages().subscribe({
      next: (data) => {
        this.rapportages = data;
      },
      error: (err) => {
        console.error('Fout bij het ophalen van rapportages:', err);
      }
    });
  }

  negeerRapportage(id: number) {
    this.rapportageService.negeerRapportage(id).subscribe({
      next: () => {
        this.rapportages = this.rapportages.filter(r => r.id !== id);
      },
      error: (err) => {
        console.error('Fout bij het negeren van de rapportage:', err);
      }
    });
  }

  verwijderReview(beoordelingId: number) {
    this.reviewService.verwijderRapporteerdeBeoordeling(beoordelingId).subscribe({
      next: () => {
        this.rapportages = this.rapportages.filter(r => r.reviewId !== beoordelingId);
      },
      error: (err) => {
        console.error('Fout bij het verwijderen van de review:', err);
      }
    });
  }
}
