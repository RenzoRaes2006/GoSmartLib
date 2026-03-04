import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GebruikerService } from '../services/GebruikerService';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { Boek } from '../models/Boek';

@Component({
  selector: 'app-leeslijst',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './leeslijst.html',
  styleUrl: './leeslijst.css',
})
export class Leeslijst implements OnInit {
  boekLijst: Boek[] = [];
  huidigeGebruikerId!: number;

  constructor(
    private gebruikerService: GebruikerService,
    private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const idParam = params.get('id'); //:id ophalen uit url
      if (idParam !== null) {
        this.huidigeGebruikerId = +idParam; //number naar string
        this.laadBoeken(this.huidigeGebruikerId);
      }
    });
  }

  laadBoeken(userId: number) {
    this.gebruikerService.getLeeslijst(userId).subscribe({
      next: (data) => {
        this.boekLijst = data;
      },
      error: (err) => {
        console.log('Niet geladen: ', err);
      },
    });
  }
  verwijderBoek(id: number | undefined) {
    if (id !== undefined) {
      this.boekLijst = this.boekLijst.filter((b) => b.id !== id);
    }
  }
}
