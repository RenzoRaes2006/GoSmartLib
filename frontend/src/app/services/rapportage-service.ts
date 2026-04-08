import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { GerapporteerdeBeoordelingAdmin } from '../models/gerapporteerde-beoordeling-repository';

@Injectable({
  providedIn: 'root',
})
export class RapportageService {
  private apiUrl = window.location.hostname === 'localhost' ? 'http://localhost:8081/api' : '/api';

  constructor(private http: HttpClient) {}

  rapporteerBeoordeling(id: number, reden: string) {
    return this.http.post(`${this.apiUrl}/reviews/${id}/report`, reden);
  }

  getRapportages(): Observable<GerapporteerdeBeoordelingAdmin[]> {
    return this.http.get<GerapporteerdeBeoordelingAdmin[]>(`${this.apiUrl}/reports`);
  }

  negeerRapportage(rapportId: number) {
    return this.http.delete(`${this.apiUrl}/reports/${rapportId}`);
  }
}
