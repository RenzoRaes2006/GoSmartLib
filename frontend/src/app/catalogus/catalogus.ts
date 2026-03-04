import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Boek } from '../models/Boek';
import { BoekService } from '../services/boek.service';
import { RouterLink } from "@angular/router";

@Component({
  selector: 'app-catalogus',
  imports: [CommonModule, RouterLink],
  templateUrl: './catalogus.html',
  styleUrl: './catalogus.css',
  standalone: true
})
export class Catalogus implements OnInit {
  boekenlijst: Boek[] = [];

  constructor(private boekService : BoekService) {}

  ngOnInit(): void {
    this.boekService.getBoeken().subscribe({
      next: (data) => {
        console.log("Boeken succesvol opgehaald: ", data);
        this.boekenlijst = data.sort((a,b) => a.locatie.localeCompare(b.locatie));
      },
      error: (err) => console.log("Iets mis gegaan met het krijgen van de boeken. ", err)
    })
  }

  
}
