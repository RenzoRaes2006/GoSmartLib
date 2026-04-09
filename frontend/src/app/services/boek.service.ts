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

  getBoekenGefilterd(filters: FilterOptions): Observable<PageResponse<Boek>> {
    let params = new HttpParams();

    if (filters.genres?.length) {
      filters.genres.forEach((g) => (params = params.append('genre', g)));
    }

    if (filters.thematischeTags?.length) {
      filters.thematischeTags.forEach((t) => (params = params.append('thematischeTag', t)));
    }

    Object.entries(filters).forEach(([key, value]) => {
      if (key === 'genres' || key === 'thematischeTags') return;
      if (value !== undefined && value !== null && value !== '') {
        const paramKey = key === 'beschikbaarheid' ? 'beschikbaar' : key;
        params = params.set(paramKey, value.toString());
      }
    });

    return this.http.get<PageResponse<Boek>>(this.apiUrl, { params });
  }

  getBoekOpId(id: number): Observable<Boek> {
    return this.http.get<Boek>(`${this.apiUrl}/${id}`);
  }

  getBeheerBoekenGefilterd(filters: FilterOptions, page: number = 0, size: number = 6): Observable<PageResponse<Boek>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (filters.zoekterm && filters.zoekterm.trim() !== '') {
      params = params.set('zoekterm', filters.zoekterm);
    }

    return this.http.get<PageResponse<Boek>>(`${this.apiUrl}`, { params });
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

  // fetch all pending loan requests
  getAlleAanvragen(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiGebruikerUrl}/aanvragen`);
  }

  // approve a loan request
  keurAanvraagGoed(uitleningId: number): Observable<string> {
    return this.http.put(`${this.apiGebruikerUrl}/aanvragen/${uitleningId}/goedkeuren`, {}, { responseType: 'text' as 'json' }) as Observable<string>;
  }

  // reject a loan request
  weigerAanvraag(uitleningId: number): Observable<string> {
    return this.http.put(`${this.apiGebruikerUrl}/aanvragen/${uitleningId}/weigeren`, {}, { responseType: 'text' as 'json' }) as Observable<string>;
  }

  // delete a loan
  verwijderLening(uitleningId: number): Observable<string> {
    return this.http.delete(`${this.apiGebruikerUrl}/leningen/${uitleningId}`, { responseType: 'text' as 'json' }) as Observable<string>;
  }

  // upload an image file
  uploadAfbeelding(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(this.afbeeldingUrl + '/upload', formData, { responseType: 'text' });
  }
}