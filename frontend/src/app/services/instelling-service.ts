import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
@Injectable({
  providedIn: 'root',
})
export class InstellingService {
  private http = inject(HttpClient);
  private apiUrl =
    window.location.hostname === 'localhost'
      ? 'http://localhost:8081/api/instellingen'
      : 'https://api-sp09.cloud-ap.be/api/instellingen';

  getLeenLimiet(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/leenlimiet`);
  }

  updateLeenLimiet(nieuweLimiet: number): Observable<string> {
    return this.http.put(`${this.apiUrl}/leenlimiet`, nieuweLimiet, { responseType: 'text' });
  }

  getInlevertermijn(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/inlevertermijn`);
  }

  updateInlevertermijn(dagen: number): Observable<string> {
    return this.http.put(`${this.apiUrl}/inlevertermijn`, dagen, { responseType: 'text' });
  }
}
