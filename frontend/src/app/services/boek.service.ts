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

  private apiBeheerUrl =
    window.location.hostname === 'localhost' ? 'http://localhost:8081/api/beheer' : '/api/beheer';

  private http = inject(HttpClient);

  getBoekenGefilterd(filters: FilterOptions): Observable<PageResponse<Boek>> {
    let params = new HttpParams();

    if (filters.tagIds?.length) {
      filters.tagIds.forEach((id) => (params = params.append('tagIds', id.toString())));
    }

    if (filters.campusIds?.length) {
      filters.campusIds.forEach((id) => (params = params.append('campusIds', id.toString())));
    }

    Object.entries(filters).forEach(([key, value]) => {
      if (key === 'tagIds' || key === 'campusIds') return;
      if (value !== undefined && value !== null && value !== '') {
        const paramKey = key === 'beschikbaarheid' ? 'beschikbaar' : key;
        params = params.set(paramKey, value.toString());
      }
    });

    return this.http.get<PageResponse<Boek>>(this.apiUrl, { params });
  }

  getDidactischeBoeken(page: number = 0, size: number = 12): Observable<PageResponse<Boek>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<Boek>>(`${this.apiUrl}/didactisch`, { params });
  }

  getBoekOpId(id: number): Observable<Boek> {
    return this.http.get<Boek>(`${this.apiUrl}/${id}`);
  }

  getBeheerBoekenGefilterd(
    filters: FilterOptions,
    page: number = 0,
    size: number = 6,
  ): Observable<PageResponse<Boek>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());

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

  checkLeenStatus(boekId: number) {
    return this.http.get<{ magLenen: boolean; reden: string }>(
      `${this.apiGebruikerUrl}/me/lenen/${boekId}/status`,
    );
  }

  leenBoek(boekId: number) {
    return this.http.post(
      `${this.apiGebruikerUrl}/me/lenen/${boekId}`,
      {},
      { responseType: 'text' },
    );
  }

  leenBoekenBulk(boekIds: number[]) {
    return this.http.post(`${this.apiGebruikerUrl}/me/lenen/bulk`, boekIds, {
      responseType: 'text',
    });
  }

  getActieveLeningen(gebruikerId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiGebruikerUrl}/${gebruikerId}/leningen`);
  }

  /** Actieve leningen van de ingelogde gebruiker (server bepaalt user). */
  getMijnActieveLeningen(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiGebruikerUrl}/me/leningen`);
  }

  /** Volledige leengeschiedenis van de ingelogde gebruiker. */
  getMijnLeengeschiedenis(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiGebruikerUrl}/me/geschiedenis`);
  }

  // Fetch all pending loan requests
  getAlleAanvragen(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiGebruikerUrl}/aanvragen`);
  }

  // Approve a loan request
  keurAanvraagGoed(uitleningId: number): Observable<string> {
    return this.http.put(
      `${this.apiGebruikerUrl}/aanvragen/${uitleningId}/goedkeuren`,
      {},
      { responseType: 'text' as 'json' },
    ) as Observable<string>;
  }

  // Reject a loan request
  weigerAanvraag(uitleningId: number): Observable<string> {
    return this.http.put(
      `${this.apiGebruikerUrl}/aanvragen/${uitleningId}/weigeren`,
      {},
      { responseType: 'text' as 'json' },
    ) as Observable<string>;
  }

  // Delete a loan
  verwijderLening(uitleningId: number): Observable<string> {
    return this.http.delete(`${this.apiGebruikerUrl}/leningen/${uitleningId}`, {
      responseType: 'text' as 'json',
    }) as Observable<string>;
  }

  // Upload an image file
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

  getLeengeschiedenis(gebruikerId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiGebruikerUrl}/${gebruikerId}/geschiedenis`);
  }

  verlengLening(uitleningId: number): Observable<string> {
    return this.http.patch(
      `${this.apiGebruikerUrl}/leningen/${uitleningId}/verlengen`,
      {},
      { responseType: 'text' as 'json' },
    ) as Observable<string>;
  }

  getAlleActieveLeningenSysteem(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiBeheerUrl}/leningen/actief`);
  }
  getAlleVerlengingAanvragen() {
    return this.http.get<any[]>(`${this.apiBeheerUrl}/verlengingen/aanvragen`);
  }

  keurVerlengingGoed(id: number) {
    return this.http.put(`${this.apiBeheerUrl}/${id}/verlenging/goedkeuren`, {});
  }

  weigerVerlenging(id: number) {
    return this.http.put(`${this.apiBeheerUrl}/${id}/verlenging/weigeren`, {});
  }
  uploadVoorPreview(file: File): Observable<any[]> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<any[]>(`${this.apiUrl}/import/preview`, formData);
  }

  bevestigImport(boeken: any[]): Observable<string> {
    return this.http.post(`${this.apiUrl}/import/bevestig`, boeken, { responseType: 'text' });
  }
}
