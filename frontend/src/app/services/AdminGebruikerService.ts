import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse } from '../models/PageResponse';

export interface AdminGebruiker {
  id: number;
  smartschoolId: string;
  naam: string | null;
  voorNaam?: string | null;
  achternaam?: string | null;
  klas: string;
  rol: 'LEERLING' | 'LEERKRACHT' | 'BEHEERDER' | 'ADMIN' | null;
  isBeheerder: boolean;
  actief?: boolean;
  aangekomenOp?: string | null;
  vertrokkenOp?: string | null;
}

export interface GebruikerCreatePayload {
  smartschoolId: string;
  schoolId: number;
  klas?: string | null;
}

export interface KlasWijziging {
  id: number;
  vanKlas: string | null;
  naarKlas: string | null;
  gewijzigdOp: string;
  doorWieSmartschoolId: string | null;
  bron: 'HANDMATIG' | 'ONEROSTER_SYNC' | string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminGebruikerService {
  private http = inject(HttpClient);

  private apiUrl = window.location.hostname === 'localhost'
    ? 'http://localhost:8081/api/admin'
    : 'https://api-sp09.cloud-ap.be/api/admin';

  private leerlingApiUrl = window.location.hostname === 'localhost'
    ? 'http://localhost:8081/api/leerlingen'
    : 'https://api-sp09.cloud-ap.be/api/leerlingen';

  getAlleLeerkrachten(
    schoolId?: number | null,
    inclVertrokken = false,
    page = 0,
    size = 5,
  ): Observable<PageResponse<AdminGebruiker>> {
    let params = new HttpParams()
      .set('inclVertrokken', inclVertrokken)
      .set('page', page)
      .set('size', size);
    if (schoolId != null) params = params.set('schoolId', schoolId);
    return this.http.get<PageResponse<AdminGebruiker>>(`${this.apiUrl}/leerkrachten`, { params });
  }

  setBeheerder(id: number, isBeheerder: boolean): Observable<AdminGebruiker> {
    return this.http.patch<AdminGebruiker>(
      `${this.apiUrl}/gebruikers/${id}/beheerder`,
      { isBeheerder }
    );
  }

  getLeerlingenInclVertrokken(
    schoolId: number,
    page = 0,
    size = 5,
  ): Observable<PageResponse<AdminGebruiker>> {
    const params = new HttpParams()
      .set('schoolId', schoolId)
      .set('page', page)
      .set('size', size);
    return this.http.get<PageResponse<AdminGebruiker>>(`${this.leerlingApiUrl}/admin`, { params });
  }

  getKlassenVoorSchool(schoolId: number): Observable<string[]> {
    return this.http.get<string[]>(`${this.leerlingApiUrl}/admin/klassen?schoolId=${schoolId}`);
  }

  voegLeerlingToe(payload: GebruikerCreatePayload): Observable<AdminGebruiker> {
    return this.http.post<AdminGebruiker>(this.leerlingApiUrl, payload);
  }

  markeerLeerlingVertrokken(id: number): Observable<AdminGebruiker> {
    return this.http.patch<AdminGebruiker>(`${this.leerlingApiUrl}/${id}/vertrek`, {});
  }

  reactiveerLeerling(id: number): Observable<AdminGebruiker> {
    return this.http.patch<AdminGebruiker>(`${this.leerlingApiUrl}/${id}/reactiveer`, {});
  }

  wijzigKlas(id: number, nieuweKlas: string): Observable<AdminGebruiker> {
    return this.http.patch<AdminGebruiker>(`${this.leerlingApiUrl}/${id}/klas`, { nieuweKlas });
  }

  getKlasHistoriek(id: number, page = 0, size = 10): Observable<PageResponse<KlasWijziging>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<KlasWijziging>>(
      `${this.leerlingApiUrl}/${id}/klas-historiek`, { params });
  }

  voegLeerkrachtToe(payload: GebruikerCreatePayload): Observable<AdminGebruiker> {
    return this.http.post<AdminGebruiker>(`${this.apiUrl}/leerkrachten`, payload);
  }

  markeerLeerkrachtVertrokken(id: number): Observable<AdminGebruiker> {
    return this.http.patch<AdminGebruiker>(`${this.apiUrl}/leerkrachten/${id}/vertrek`, {});
  }

  reactiveerLeerkracht(id: number): Observable<AdminGebruiker> {
    return this.http.patch<AdminGebruiker>(`${this.apiUrl}/leerkrachten/${id}/reactiveer`, {});
  }
}
