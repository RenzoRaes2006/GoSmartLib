import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Boek } from '../models/Boek';
import { NieuwBoek } from '../models/NieuwBoek';

@Injectable({
  providedIn: 'root',
})
export class BoekService {
  // private readonly gebruikTraefik = true; // Zet op true als je Traefik gebruikt, anders false
  // private apiUrl = this.gebruikTraefik
  //   ? 'http://frontend.localhost/api/boeken'
  //   : 'http://localhost:8081/api/boeken';
  private apiUrl = '/api';

  constructor(private http: HttpClient) {}

  getBoeken(): Observable<Boek[]> {
    return this.http.get<Boek[]>(`${this.apiUrl}/boeken`);
  }
  getBoekOpId(id: number): Observable<Boek> {
    return this.http.get<Boek>(`${this.apiUrl}/boeken/${id}`);
  }
  toevoegen(boek: NieuwBoek): Observable<Boek> {
    return this.http.post<Boek>(`${this.apiUrl}/boeken`, boek);
  }
}
