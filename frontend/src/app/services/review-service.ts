import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Beoordeling } from '../models/review.model';

@Injectable({
  providedIn: 'root',
})
export class ReviewService {
  private apiUrl = window.location.hostname === 'localhost' ? 'http://localhost:8081/api' : '/api';

  constructor(private http: HttpClient) {}

  getBeoordelingenOpBoekId(bookId: number) {
    return this.http.get<Beoordeling[]>(`${this.apiUrl}/books/${bookId}/reviews`);
  }

  voegBeoordelingToe(beoordeling: Omit<Beoordeling, 'id'>) {
    return this.http.post<Beoordeling>(`${this.apiUrl}/reviews`, beoordeling);
  }

  rapporteerBeoordeling(id: number, reden: string) {
    return this.http.post(`${this.apiUrl}/reviews/${id}/report`, { reden });
  }
}