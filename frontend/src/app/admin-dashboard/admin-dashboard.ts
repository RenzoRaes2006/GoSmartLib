import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BoekService } from '../services/boek.service';

@Component({
  selector: 'app-admin-dashboard',
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard implements OnInit {
  boekService = inject(BoekService);

  totaalBoeken = 0;

  //tijdelijke data
  uitleenRatio: number = 68;
  tijdigeRetour: number = 92;
  uniekeLeners: number = 345;
  nieuweReviews: number = 12;

  ngOnInit(): void {
    this.boekService.getBoeken().subscribe(
      (boeken) => {
        this.totaalBoeken = boeken.length;
      },
      (error) => {
        console.error('Fout bij het ophalen van boeken:', error);
      },
    );
  }
}
