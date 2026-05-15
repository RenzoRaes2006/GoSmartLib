import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../services/AuthService';

type LoginResponse = { token?: string; moetWachtwoordWijzigen?: boolean; tijdelijkToken?: string };

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

  // Stap 2: wachtwoord wijzigen bij eerste login
  stap: 'login' | 'wijzigen' = 'login';
  private tijdelijkToken = '';
  nieuwWachtwoord = '';
  nieuwWachtwoordBevestig = '';

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

    this.http.post<LoginResponse>(`${this.apiUrl}/api/auth/admin/login`, {
      gebruikersnaam: this.gebruikersnaam,
      wachtwoord: this.wachtwoord,
    }).subscribe({
      next: (response) => {
        if (response.moetWachtwoordWijzigen && response.tijdelijkToken) {
          this.tijdelijkToken = response.tijdelijkToken;
          this.stap = 'wijzigen';
          this.bezig = false;
          return;
        }
        if (response.token) {
          this.afrondenMetToken(response.token);
        }
      },
      error: () => {
        this.foutmelding = 'Ongeldig gebruikersnaam of wachtwoord.';
        this.bezig = false;
      },
    });
  }

  wachtwoordWijzigen() {
    this.foutmelding = '';

    if (this.nieuwWachtwoord.length < 8) {
      this.foutmelding = 'Wachtwoord moet minstens 8 tekens lang zijn.';
      return;
    }
    if (this.nieuwWachtwoord !== this.nieuwWachtwoordBevestig) {
      this.foutmelding = 'Wachtwoorden komen niet overeen.';
      return;
    }

    this.bezig = true;

    this.http.post<LoginResponse>(
      `${this.apiUrl}/api/auth/admin/wachtwoord-wijzigen`,
      { nieuwWachtwoord: this.nieuwWachtwoord },
      { headers: { Authorization: `Bearer ${this.tijdelijkToken}` } }
    ).subscribe({
      next: (response) => {
        if (response.token) {
          this.afrondenMetToken(response.token);
        }
      },
      error: (err) => {
        this.foutmelding = err?.error?.fout ?? 'Wachtwoord wijzigen mislukt.';
        this.bezig = false;
      },
    });
  }

  private afrondenMetToken(token: string) {
    localStorage.setItem('jwtToken', token);
    this.authService.setUserFromToken(token);
    this.router.navigate(['/admin']);
  }
}
