import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Boek } from '../models/Boek';
import { NieuwBoek } from '../models/NieuwBoek';
import { PageResponse } from '../models/PageResponse';

@Injectable({
  providedIn: 'root',
})
export class BoekService {
  private apiUrl =
    window.location.hostname === 'localhost' ? 'http://localhost:8081/api/boeken' : '/api/boeken';

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
}