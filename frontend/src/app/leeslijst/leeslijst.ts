import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GebruikerService } from '../services/GebruikerService';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-leeslijst',
  imports: [CommonModule, RouterModule],
  templateUrl: './leeslijst.html',
  styleUrl: './leeslijst.css',
})
export class Leeslijst {
  testBoeken = [
    {
      id: 1,
      titel: 'De Brief voor de Koning',
      auteur: 'Tonke Dragt',
      jaar: 1962,
      afbeelding: 'https://m.media-amazon.com/images/I/51-m9R1-pPL._SY445_SX342_.jpg',
    },
    {
      id: 2,
      titel: 'Harry Potter',
      auteur: 'J.K. Rowling',
      jaar: 1997,
      afbeelding: '',
    },
    {
      id: 3,
      titel: 'Kruistocht in Spijkerbroek',
      auteur: 'Thea Beckman',
      jaar: 1973,
      afbeelding: 'https://images.kb.nl/jp2/high/gvn/KONW01/KONW01_118196150.jp2',
    },
  ];
  constructor(private gebruikerService: GebruikerService) {}

  verwijderBoek(id: number) {
    this.testBoeken = this.testBoeken.filter((b) => b.id !== id);
    console.log('Boek met id ' + id + ' is verwijderd uit de testlijst.');
  }
}
