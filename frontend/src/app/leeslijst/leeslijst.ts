import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GebruikerService } from '../services/GebruikerService';
import { RouterModule } from '@angular/router';
import { Boek } from '../models/Boek';
import { BoekService } from '../services/boek.service';

@Component({
  selector: 'app-leeslijst',
  imports: [CommonModule, RouterModule],
  templateUrl: './leeslijst.html',
  styleUrl: './leeslijst.css',
})
export class Leeslijst implements OnInit {
  boekLijst: Boek[] = [];

  constructor(private boekService: BoekService) {}

  ngOnInit(): void {
    this.laadBoeken();
  }

  laadBoeken() {
    this.boekService.getBoeken().subscribe({
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
