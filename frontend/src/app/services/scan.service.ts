import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Gebruiker } from '../models/Gebruiker.mode';
import { Boek } from '../models/Boek';
import { ScanBatchRequest, ScanBatchResponse } from '../models/scan.model';

export interface LeerlingenPage {
  content: Gebruiker[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}

@Injectable({
  providedIn: 'root',
})
export class ScanService {
  private apiBase =
    window.location.hostname === 'localhost' ? 'http://localhost:8081/api' : '/api';

  private http = inject(HttpClient);

  zoekLeerlingOpSchoolId(schoolId: number): Observable<Gebruiker> {
    return this.http.get<Gebruiker>(`${this.apiBase}/gebruiker/schoolid/${schoolId}`);
  }

  zoekLeerlingen(zoekterm: string, page: number, size: number): Observable<LeerlingenPage> {
    const params: Record<string, string> = {
      page: String(page),
      size: String(size),
    };
    if (zoekterm) params['zoekterm'] = zoekterm;
    return this.http.get<LeerlingenPage>(`${this.apiBase}/leerlingen`, { params });
  }

  zoekBoekOpIsbn(isbn: string): Observable<Boek> {
    return this.http.get<Boek>(`${this.apiBase}/boeken/isbn/${isbn}`);
  }

  getActieveLeningenVoorLeerling(gebruikerId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiBase}/gebruiker/${gebruikerId}/leningen`);
  }

  verwerkBatch(request: ScanBatchRequest): Observable<ScanBatchResponse> {
    return this.http.post<ScanBatchResponse>(`${this.apiBase}/scan/batch`, request);
  }

  verwerkRetourMetSchade(
    gebruikerId: number,
    items: { isbn: string; beschadigd: boolean; opmerking: string | null }[],
  ): Observable<ScanBatchResponse> {
    return this.http.post<ScanBatchResponse>(`${this.apiBase}/scan/retour-met-schade`, {
      gebruikerId,
      items,
    });
  }
}
