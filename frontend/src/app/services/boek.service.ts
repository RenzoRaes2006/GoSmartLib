import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
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
  private apiUrl = '/api/boeken';

private http = inject(HttpClient);

  getBoeken(zoekterm?: string): Observable<Boek[]> {
    let params = new HttpParams();
    
    if (zoekterm && zoekterm.trim() !== '') {
      params = params.set('zoekterm', zoekterm);
    }

    return this.http.get<Boek[]>(this.apiUrl, { params });
  }
  getBoekOpId(id: number): Observable<Boek> {
    return this.http.get<Boek>(`${this.apiUrl}/${id}`);
  }
  toevoegen(boek: NieuwBoek): Observable<Boek> {
    return this.http.post<Boek>(this.apiUrl, boek);
  }
}
