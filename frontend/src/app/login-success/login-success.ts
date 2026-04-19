import { Component, inject, Inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-login-success',
  template: '<p>Bezig met inloggen, even geduld...</p>',
  styleUrl: './login-success.css'
})
export class LoginSuccessComponent implements OnInit {
  route = inject(ActivatedRoute);
  router = inject(Router);

  ngOnInit(): void {s
    // Lees de parameters uit de URL
    this.route.queryParams.subscribe(params => {
      const token = params['token'];

      if (token) {
        // 1. Sla de token op in de browser (localStorage)
        localStorage.setItem('jwtToken', token);

        // 2. Stuur de gebruiker door naar de afgeschermde homepagina of catalogus
        // Dit verwijdert ook direct de token uit de zichtbare adresbalk
        this.router.navigate(['/']);
      } else {
        // Als er iets misging en er is geen token, stuur terug naar de login
        this.router.navigate(['/login']);
      }
    });
  }
}