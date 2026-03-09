import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Beoordeling } from '../models/review.model';

@Injectable({
  providedIn: 'root',
})
export class ReviewService {
  private apiUrl = '/api'; // Dit is de 'Incoming Change' van je groepsgenoot.

  constructor(private http: HttpClient) {}

  getBeoordelingenOpBoekId(bookId: number) {
    return this.http.get<Beoordeling[]>(`${this.apiUrl}/books/${bookId}/reviews`);
  }

  voegBeoordelingToe(beoordeling: Beoordeling) {
    return this.http.post<Beoordeling>(`${this.apiUrl}/reviews`, beoordeling);
  }
}
