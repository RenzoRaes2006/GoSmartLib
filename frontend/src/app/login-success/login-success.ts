import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../services/AuthService';

@Component({
  selector: 'app-login-success',
  template: '<p>Bezig met inloggen, even geduld...</p>',
  styleUrl: './login-success.css'
})
export class LoginSuccessComponent implements OnInit {
  route = inject(ActivatedRoute);
  router = inject(Router);
  authService = inject(AuthService);

  ngOnInit(): void {
    // Lees de parameters uit de URL
    this.route.queryParams.subscribe(params => {
      const token = params['token'];

      if (token) {
        // 1. Sla de token op in de browser (localStorage)
        localStorage.setItem('jwtToken', token);

        // 2. Werk direct de huidige gebruiker bij in de app-state
        this.authService.setUserFromToken(token);

        // 3. Stuur de gebruiker door naar de afgeschermde homepagina of catalogus
        // Dit verwijdert ook direct de token uit de zichtbare adresbalk
        this.router.navigate(['/']);
      } else {
        // Als er iets misging en er is geen token, stuur terug naar de login
        this.router.navigate(['/login']);
      }
    });
  }
}