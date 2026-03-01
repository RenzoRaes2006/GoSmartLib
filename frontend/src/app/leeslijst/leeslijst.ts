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
    { id: 1, titel: 'De Brief voor de Koning', auteur: 'Tonke Dragt', jaar: 1962 },
    { id: 2, titel: 'Harry Potter en de Steen der Wijzen', auteur: 'J.K. Rowling', jaar: 1997 },
    { id: 3, titel: 'Kruistocht in Spijkerbroek', auteur: 'Thea Beckman', jaar: 1973 },
    { id: 4, titel: 'The Hobbit', auteur: 'J.R.R. Tolkien', jaar: 1937 },
  ];
  constructor(private gebruikerService: GebruikerService) {}

  verwijderBoek(id: number) {
    this.testBoeken = this.testBoeken.filter((b) => b.id !== id);
    console.log('Boek met id ' + id + ' is verwijderd uit de testlijst.');
  }
}
