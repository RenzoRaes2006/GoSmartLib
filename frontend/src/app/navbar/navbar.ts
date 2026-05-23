import { Component, inject, isDevMode, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { LeenMandjeService } from '../services/leen-mandje.service';
import { ToegankelijkheidService } from '../services/toegankelijkheid';
import { AuthService } from '../services/AuthService';

type DevRole = 'STUDENT' | 'TEACHER' | 'BEHEERDER' | 'ADMIN';

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

  isDevMode = isDevMode();
  huidigeDevRole: DevRole = (localStorage.getItem('devRole') as DevRole) || 'BEHEERDER';

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

  get homeRoute(): string {
    return this.authService.currentUser()?.role === 'teacher' ? '/leerkracht-dashboard' : '/';
  }

  // Leerling/leerkracht zien deze links; beheerder en admin niet.
  get toonLeerlingLinks(): boolean {
    const rol = this.authService.currentUser()?.role;
    return rol !== 'beheerder' && rol !== 'admin';
  }

  wisselDevRole(event: Event) {
    const nieuw = (event.target as HTMLSelectElement).value as DevRole;
    this.authService.setDevRole(nieuw);
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
