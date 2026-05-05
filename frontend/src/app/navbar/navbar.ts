import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { LeenMandjeService } from '../services/leen-mandje.service';
import { ToegankelijkheidService } from '../services/toegankelijkheid';
import { AuthService } from '../services/AuthService';

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
  authService = inject(AuthService);
  private router = inject(Router);

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

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
