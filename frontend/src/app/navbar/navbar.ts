import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LeenMandjeService } from '../services/leen-mandje.service';
import { ToegankelijkheidService } from '../services/toegankelijkheid';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
  standalone: true,
})
export class Navbar implements OnInit {
  mandjeService = inject(LeenMandjeService);
  aantalInMandje: number = 0;
  toegankelijkheidService = inject(ToegankelijkheidService);
  isDyslexieActief: boolean = false;

  ngOnInit() {
    this.mandjeService.mandje$.subscribe((boeken) => {
      this.aantalInMandje = boeken.length;
    });
    this.toegankelijkheidService.dyslexieModus$.subscribe((actief) => {
      this.isDyslexieActief = actief;
    });
  }
  toggleDyslexie() {
    this.toegankelijkheidService.toggleDyslexieFont();
  }
}
