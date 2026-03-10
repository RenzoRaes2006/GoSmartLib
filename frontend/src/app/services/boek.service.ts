import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Boek } from '../models/Boek';
import { NieuwBoek } from '../models/NieuwBoek';
import { PageResponse } from '../models/PageResponse';

@Injectable({
  providedIn: 'root',
})
export class BoekService {
  private readonly gebruikTraefik = true; // Zet op true als je Traefik gebruikt, anders false
  private apiUrl = this.gebruikTraefik
    ? 'http://frontend.localhost/api/boeken'
    : 'http://localhost:8081/api/boeken';

  constructor(private http: HttpClient) {}

  getBoeken(): Observable<Boek[]> {
    return this.http.get<Boek[]>(this.apiUrl);
  }
  getBoekOpId(id: number): Observable<Boek> {
    return this.http.get<Boek>(`${this.apiUrl}/${id}`);
  }
  getBoekOpIsbn(isbn: string): Observable<Boek> {
    return this.http.get<Boek>(`${this.apiUrl}/isbn/${isbn}`);
  }
  toevoegen(boek: NieuwBoek): Observable<Boek> {
    return this.http.post<Boek>(this.apiUrl, boek);
  }
  wijzigen(id: number, boek: Boek): Observable<Boek> {
    return this.http.put<Boek>(`${this.apiUrl}/${id}`, boek);
  }
  verwijderen(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`, { responseType: 'text' });
  }
  getBoekenPaged(page: number, size: number): Observable<PageResponse<Boek>> {
    return this.http.get<PageResponse<Boek>>(`${this.apiUrl}/paged?page=${page}&size=${size}`);
  }
}
