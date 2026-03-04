import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Gebruiker } from '../models/Gebruiker.mode';
import { Boek } from '../models/Boek';
@Injectable({
  providedIn: 'root',
})
export class GebruikerService {
  private apiUrl = '/api/gebruikers';

  constructor(private http: HttpClient) {}

  getUsers(): Observable<Gebruiker[]> {
    return this.http.get<Gebruiker[]>(this.apiUrl);
  }
  getLeeslijst(userId: number): Observable<Boek[]> {
    return this.http.get<Boek[]>(`${this.apiUrl}/${userId}/leeslijst`);
  }
}
