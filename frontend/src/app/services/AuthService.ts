import { Injectable, signal } from '@angular/core';
import { jwtDecode } from 'jwt-decode';

@Injectable({ providedIn: 'root' })
export class AuthService {
  /***
   * Het type van de ingelogde gebruiker.
   * Dit is een TypeScript-interface (structuurafspraak):
   *   - id:   de Smartschool ID (= "sub" in de JWT-token)
   *   - name: de weergavenaam, bv. "Haitam"
   *   - role: "student" of "teacher" (van OneRoster)
   *   - klas: de klasnaam, bv. "2SOF" (van OneRoster)
   *
   * Signal<...> betekent: als deze waarde verandert, updaten alle componenten
   * die ernaar luisteren automatisch. Vergelijk met een reactive variabele.
   ***/
  public currentUser = signal<{ id: string; name: string; role: string; klas: string } | null>(null);

  constructor() {
    const token = localStorage.getItem('jwtToken');
    if (token) {
      this.setUserFromToken(token);
    }
  }

  setUserFromToken(token: string) {
    const decoded: any = jwtDecode(token);
    this.currentUser.set({
      id:   decoded.sub,    // subject = smartschoolId
      name: decoded.name,   // weergavenaam van Smartschool
      role: decoded.role,   // "student" of "teacher" van OneRoster
      klas: decoded.klas ?? 'onbekend'  // klasnaam van OneRoster, fallback naar "onbekend"
    });
    // [DEBUG] console.log('[Auth] Token decoded:', decoded);
    // [DEBUG] console.log('[Auth] Ingelogde gebruiker:', this.currentUser());
  }

  logout() {
    localStorage.removeItem('jwtToken');
    this.currentUser.set(null);
  }
}
