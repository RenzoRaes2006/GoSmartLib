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
  private apiUrl =
    window.location.hostname === 'localhost' ? 'http://localhost:8081/api/boeken' : '/api/boeken';

  private afbeeldingUrl =
    window.location.hostname === 'localhost' ? 'http://localhost:8081/api/images' : '/api/images';

  private apiGebruikerUrl =
    window.location.hostname === 'localhost'
      ? 'http://localhost:8081/api/gebruiker'
      : '/api/gebruiker';

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
      filters.genres.forEach((g) => (params = params.append('genre', g)));
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

  getBeheerBoekenGefilterd(filters: FilterOptions): Observable<Boek[]> {
    let params = new HttpParams();

    if (filters.zoekterm && filters.zoekterm.trim() !== '') {
      params = params.set('zoekterm', filters.zoekterm);
    }

    return this.http.get<Boek[]>(`${this.apiUrl}`, { params });
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

  getPopulaireBoeken() {
    return this.http.get<Boek[]>(`${this.apiUrl}/top/populair`);
  }

  checkLeenStatus(gebruikerId: number, boekId: number) {
    return this.http.get<{ magLenen: boolean; reden: string }>(
      `${this.apiGebruikerUrl}/${gebruikerId}/lenen/${boekId}/status`,
    );
  }

  leenBoek(gebruikerId: number, boekId: number) {
    return this.http.post(
      `${this.apiGebruikerUrl}/${gebruikerId}/lenen/${boekId}`,
      {},
      { responseType: 'text' },
    );
  }

  getActieveLeningen(gebruikerId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiGebruikerUrl}/${gebruikerId}/leningen`);
  }

  uploadAfbeelding(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(this.afbeeldingUrl + '/upload', formData, { responseType: 'text' });
  }

  importeerBoeken(file: File): Observable<string> {
    const formData: FormData = new FormData();
    formData.append('file', file, file.name);

    return this.http.post(`${this.apiUrl}/import`, formData, { responseType: 'text' });
  }
}
