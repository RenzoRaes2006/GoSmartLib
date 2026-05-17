import { Injectable, isDevMode, signal } from '@angular/core';
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
    // Dev-modus: sla de Smartschool-login over en zet meteen een testgebruiker.
    // Rol overrulen via localStorage.setItem('devRole', 'STUDENT'|'TEACHER'|'BEHEERDER'|'ADMIN').
    if (isDevMode()) {
      const role = (localStorage.getItem('devRole') || 'BEHEERDER').toLowerCase();
      this.currentUser.set({
        id: 'dev-user-001',
        name: 'Dev User',
        role,
        klas: 'DEV1',
      });
      return;
    }

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
  }

  /**
   * Wisselt in dev-modus van rol. Verandert localStorage en herlaadt de pagina
   * zodat de nieuwe rol zowel in de frontend (currentUser) als in de backend
   * (via X-Dev-Role header) consistent gebruikt wordt.
   */
  setDevRole(role: 'STUDENT' | 'TEACHER' | 'BEHEERDER' | 'ADMIN') {
    if (!isDevMode()) return;
    localStorage.setItem('devRole', role);
    window.location.reload();
  }

  logout() {
    localStorage.removeItem('jwtToken');
    this.currentUser.set(null);
  }
}
