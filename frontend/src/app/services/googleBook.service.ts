import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class GoogleBookService {
  private http = inject(HttpClient);

  // Roept onze eigen backend aan i.p.v. rechtstreeks Google Books. De
  // API-sleutel blijft zo op de server en komt niet in de browser terecht.
  private baseUrl =
    window.location.hostname === 'localhost'
      ? 'http://localhost:8081/api/googlebooks'
      : '/api/googlebooks';

  zoekOpIsbn(isbn: string): Observable<any> {
    // Backend geeft het boek terug, of een lege body (204) als er niets gevonden is.
    return this.http.get(`${this.baseUrl}/isbn/${isbn}`);
  }
}
