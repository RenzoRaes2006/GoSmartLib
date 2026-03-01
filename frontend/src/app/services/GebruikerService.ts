import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Gebruiker } from '../models/Gebruiker.mode';
@Injectable({
  providedIn: 'root',
})
export class GebruikerService {
  private apiUrl = '/api/users';

  constructor(private http: HttpClient) {}

  getUsers(): Observable<Gebruiker[]> {
    return this.http.get<Gebruiker[]>(this.apiUrl);
  }
  // addToReadingList(userId: number, bookId: number): Observable<any> {
  //   return this.http.post(`${this.apiUrl}/${userId}/books/${bookId}`, {});
}
