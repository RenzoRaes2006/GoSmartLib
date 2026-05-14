import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../services/AuthService';

@Component({
  selector: 'app-login-success',
  template: '<p>Bezig met inloggen, even geduld...</p>',
  styleUrl: './login-success.css',
})
export class LoginSuccessComponent implements OnInit {
  route = inject(ActivatedRoute);
  router = inject(Router);
  authService = inject(AuthService);

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      const token = params['token'];

      if (token) {
        // 1. Sla de token op in de browser zodat die bewaard blijft na een refresh
        localStorage.setItem('jwtToken', token);

        // 2. Werk de ingelogde gebruiker bij in de app-state (signals)
        this.authService.setUserFromToken(token);

        // 3. Stuur door op basis van rol
        const role = this.authService.currentUser()?.role;
        if (role === 'teacher') {
          this.router.navigate(['/leerkracht-dashboard']);
        } else if (role === 'admin') {
          this.router.navigate(['/admin']);
        } else if (role === 'beheerder') {
          this.router.navigate(['/beheerder']);
        } else {
          this.router.navigate(['/']);
        }
      } else {
        this.router.navigate(['/login']);
      }
    });
  }
}
