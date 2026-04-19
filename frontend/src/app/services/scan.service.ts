import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Gebruiker } from '../models/Gebruiker.mode';
import { Boek } from '../models/Boek';
import { ScanBatchRequest, ScanBatchResponse } from '../models/scan.model';

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

  zoekBoekOpIsbn(isbn: string): Observable<Boek> {
    return this.http.get<Boek>(`${this.apiBase}/boeken/isbn/${isbn}`);
  }

  verwerkBatch(request: ScanBatchRequest): Observable<ScanBatchResponse> {
    return this.http.post<ScanBatchResponse>(`${this.apiBase}/scan/batch`, request);
  }
}
