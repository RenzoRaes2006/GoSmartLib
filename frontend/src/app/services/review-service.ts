import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Beoordeling } from '../models/review.model';

@Injectable({
  providedIn: 'root',
})
export class ReviewService {
  private readonly gebruikTraefik = false; // Zet op true als je Traefik gebruikt, anders false
  private apiUrl = this.gebruikTraefik
    ? 'http://frontend.localhost/api'
    : 'http://localhost:8081/api';

  constructor(private http: HttpClient) {}

  // ik moet de pathen nog checken qua restful api
  getBeoordelingenOpBoekId(bookId: number) {
    return this.http.get<Beoordeling[]>(`${this.apiUrl}/books/${bookId}/reviews`);
  }

  voegBeoordelingToe(beoordeling: Beoordeling) {
    return this.http.post<Beoordeling>(`${this.apiUrl}/reviews`, beoordeling);
  }
}
