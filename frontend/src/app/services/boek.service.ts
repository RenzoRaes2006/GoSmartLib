import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Boek } from '../models/Boek';
import { NieuwBoek } from '../models/NieuwBoek';
import { PageResponse } from '../models/PageResponse';
import { FilterOptions } from '../models/FilterOptions';

@Injectable({
  providedIn: 'root',
})
export class BoekService {
  // private readonly gebruikTraefik = true; // Zet op true als je Traefik gebruikt, anders false
  // private apiUrl = this.gebruikTraefik
  //   ? 'http://frontend.localhost/api/boeken'
  //   : 'http://localhost:8081/api/boeken';
  private apiUrl = 'http://frontend.localhost/api/boeken';

  private http = inject(HttpClient);

  getBoeken(zoekterm?: string): Observable<Boek[]> {
    let params = new HttpParams();

    if (zoekterm && zoekterm.trim() !== '') {
      params = params.set('zoekterm', zoekterm);
    }

    return this.http.get<Boek[]>(this.apiUrl, { params });
  }

  getBoekenGefilterd(filters: FilterOptions): Observable<Boek[]> {
    let params = new HttpParams();

    if (filters.genres?.length) {
      filters.genres.forEach(g => params = params.append('genre', g));
    }

    Object.entries(filters).forEach(([key, value]) => {
      if (key === 'genres') return;
      if (value !== undefined && value !== null && value !== '') {
        const paramKey = key === 'beschikbaarheid' ? 'beschikbaar' : key;
        params = params.set(paramKey, value.toString());
      }
    });

    return this.http.get<Boek[]>(this.apiUrl, { params });
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
