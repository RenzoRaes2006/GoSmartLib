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
    // Bij het opstarten van de app: als er al een token in localStorage zit
    // (bv. na een pagina-refresh), laad dan meteen de gebruiker.
    // Dit zorgt ervoor dat je na een F5 (refresh) niet uitgelogd bent.
    const token = localStorage.getItem('jwtToken');
    if (token) {
      this.setUserFromToken(token);
    }
  }

  /***
   * Lees de gebruikersgegevens uit de JWT-token en sla ze op in currentUser.
   *
   * HOE WERKT EEN JWT-TOKEN?
   * Een JWT bestaat uit 3 delen gescheiden door een punt:
   *   header.payload.signature
   *
   * Het middelste deel (payload) is Base64-gecodeerde JSON met onze data.
   * jwtDecode() doet dit decoderen voor ons.
   *
   * Na decoderen ziet decoded er zo uit:
   *   {
   *     sub:  "E4LD6Wfpo22xSAjmOUHj2g==",  <- smartschoolId
   *     name: "Haitam",
   *     role: "student",
   *     klas: "2SOF",
   *     iat:  1234567890,                   <- aangemaakt op (timestamp)
   *     exp:  1234654290                    <- vervalt op (timestamp)
   *   }
   ***/
  setUserFromToken(token: string) {
    const decoded: any = jwtDecode(token);
    this.currentUser.set({
      id:   decoded.sub,    // subject = smartschoolId
      name: decoded.name,   // weergavenaam van Smartschool
      role: decoded.role,   // "student" of "teacher" van OneRoster
      klas: decoded.klas ?? 'onbekend'  // klasnaam van OneRoster, fallback naar "onbekend"
    });
    console.log('[Auth] Token decoded:', decoded);
    console.log('[Auth] Ingelogde gebruiker:', this.currentUser());
  }

  logout() {
    localStorage.removeItem('jwtToken');
    this.currentUser.set(null);
  }
}
