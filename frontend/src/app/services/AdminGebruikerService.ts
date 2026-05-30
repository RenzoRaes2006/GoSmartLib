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

export interface VertrekKlassLeeslijst {
  id: number;
  naam: string;
  klas: string | null;
  leerkrachtId: number;
  leerkrachtNaam: string | null;
  leerlingIds: number[];
  leerlingNamen: string[];
  beschrijving: string | null;
  zichtbaar: boolean;
  gearchiveerd: boolean;
  boeken: unknown[];
}

export interface KandidaatLeerkracht {
  id: number;
  smartschoolId: string;
  voorNaam: string | null;
  achternaam: string | null;
}

export interface LeerkrachtVertrekPreview {
  leeslijsten: VertrekKlassLeeslijst[];
  kandidaten: KandidaatLeerkracht[];
  aantalOpenLeningen: number;
}

export interface LeerkrachtVertrekRequest {
  // map leeslijstId → nieuweLeerkrachtId; lijsten die hier niet in zitten,
  // worden automatisch gearchiveerd door de backend.
  overdrachten: Record<number, number>;
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

  private beheerApiUrl = window.location.hostname === 'localhost'
    ? 'http://localhost:8081/api/beheer'
    : 'https://api-sp09.cloud-ap.be/api/beheer';

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

  getLeerkrachtVertrekPreview(id: number): Observable<LeerkrachtVertrekPreview> {
    return this.http.get<LeerkrachtVertrekPreview>(
      `${this.apiUrl}/leerkrachten/${id}/vertrek-preview`
    );
  }

  /**
   * Markeert een leerkracht als vertrokken. Optionele body met overdrachten
   * (leeslijstId → nieuweLeerkrachtId). Lijsten die niet in de map staan,
   * worden automatisch gearchiveerd.
   */
  markeerLeerkrachtVertrokken(
    id: number,
    overdrachten: Record<number, number> = {}
  ): Observable<AdminGebruiker> {
    return this.http.patch<AdminGebruiker>(
      `${this.apiUrl}/leerkrachten/${id}/vertrek`,
      { overdrachten }
    );
  }

  reactiveerLeerkracht(id: number): Observable<AdminGebruiker> {
    return this.http.patch<AdminGebruiker>(`${this.apiUrl}/leerkrachten/${id}/reactiveer`, {});
  }

  // Beheerder-varianten: zelfde structuur als admin, maar endpoint is /api/beheer/...
  // De school wordt server-side bepaald o.b.v. de ingelogde beheerder.
  getLeerkrachtenVanMijnSchool(page = 0, size = 10000): Observable<PageResponse<AdminGebruiker>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<AdminGebruiker>>(`${this.beheerApiUrl}/leerkrachten`, { params });
  }

  setBeheerderVanMijnSchool(id: number, isBeheerder: boolean): Observable<AdminGebruiker> {
    return this.http.patch<AdminGebruiker>(
      `${this.beheerApiUrl}/gebruikers/${id}/beheerder`,
      { isBeheerder }
    );
  }
}
