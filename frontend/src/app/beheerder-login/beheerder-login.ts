import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../services/AuthService';

@Component({
  selector: 'app-beheerder-login',
  imports: [FormsModule],
  templateUrl: './beheerder-login.html',
  styleUrl: './beheerder-login.css',
})
export class BeheerderLogin {
  gebruikersnaam = '';
  wachtwoord = '';
  foutmelding = '';
  bezig = false;

  private apiUrl = 'http://api-sp09.cloud-ap.be';

  constructor(
    private http: HttpClient,
    private router: Router,
    private authService: AuthService
  ) {}

  login() {
    this.foutmelding = '';
    this.bezig = true;

    this.http.post<{ token: string }>(`${this.apiUrl}/api/auth/admin/login`, {
      gebruikersnaam: this.gebruikersnaam,
      wachtwoord: this.wachtwoord,
    }).subscribe({
      next: (response) => {
        localStorage.setItem('jwtToken', response.token);
        this.authService.setUserFromToken(response.token);
        this.router.navigate(['/admin']);
      },
      error: () => {
        this.foutmelding = 'Ongeldig gebruikersnaam of wachtwoord.';
        this.bezig = false;
      },
    });
  }
}
