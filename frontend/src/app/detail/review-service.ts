import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Beoordeling } from './review.model';

@Injectable({
  providedIn: 'root',
})
export class ReviewService {
  private readonly gebruikTraefik = true; // Zet op true als je Traefik gebruikt, anders false
  private apiUrl = this.gebruikTraefik
    ? 'http://frontend.localhost/api'
    : 'http://localhost:8081/api';

  constructor(private http: HttpClient) {}

  // ik moet de pathen nog checken qua restful api
  getBeoordelingenOpBoekId(bookId: number) {
    return this.http.get(`${this.apiUrl}/books/${bookId}/reviews`);
  }

  voegBeoordelingToe(beoordeling: Beoordeling) {
    return this.http.post(`${this.apiUrl}/reviews`, beoordeling);
  }
}
