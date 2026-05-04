import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AdminStatistiek {
  percentageUitgeleend: number | null;
  teLaatIngeleverd: number | null;
  populairsteGenre: string | null;
  uitleningenDezeMaand: number | null;
}

export interface LeerkrachtStatistiek {
  aantalLeerlingen: number | null;
  aantalActieveLeningen: number | null;
  aantalTeLaat: number | null;
  boekenAfgelopenWeek: number | null;
  topBoeken: string[];
}

export interface LeerlingStatistiek {
  gelezenBoeken: number | null;
  leesDoel: number | null;
  aantalActieveLeningen: number | null;
  aantalTeLaat: number | null;
}

@Injectable({
  providedIn: 'root',
})
export class StatistiekService {
  private http = inject(HttpClient);
  private apiUrl =
    window.location.hostname === 'localhost'
      ? 'http://localhost:8081/api/statistieken'
      : '/api/statistieken';

  getAdminStatistieken(): Observable<AdminStatistiek> {
    return this.http.get<AdminStatistiek>(`${this.apiUrl}/admin`);
  }

  getLeerkrachtStatistieken(klasId?: string): Observable<LeerkrachtStatistiek> {
    const url = klasId ? `${this.apiUrl}/leerkracht?klasId=${klasId}` : `${this.apiUrl}/leerkracht`;
    return this.http.get<LeerkrachtStatistiek>(url);
  }

  getLeerlingStatistieken(schoolId: string): Observable<LeerlingStatistiek> {
    return this.http.get<LeerlingStatistiek>(`${this.apiUrl}/leerling/${schoolId}`);
  }
}
