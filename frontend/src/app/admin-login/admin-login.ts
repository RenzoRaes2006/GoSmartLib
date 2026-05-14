import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../services/AuthService';

@Component({
  selector: 'app-admin-login',
  imports: [FormsModule],
  templateUrl: './admin-login.html',
  styleUrl: './admin-login.css',
})
export class AdminLogin {
  gebruikersnaam = '';
  wachtwoord = '';
  foutmelding = '';
  bezig = false;

  private apiUrl = window.location.hostname === 'localhost'
    ? 'http://localhost:8081'
    : 'https://api-sp09.cloud-ap.be';

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
