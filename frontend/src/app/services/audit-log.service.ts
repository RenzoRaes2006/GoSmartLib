import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export type AuditActie =
  | 'GEBRUIKER_TOEGEVOEGD'
  | 'GEBRUIKER_TOEGEVOEGD_SYNC'
  | 'GEBRUIKER_VERTROKKEN'
  | 'GEBRUIKER_VERTROKKEN_SYNC'
  | 'GEBRUIKER_GEREACTIVEERD'
  | 'KLAS_GEWIJZIGD'
  | 'KLAS_GEWIJZIGD_SYNC'
  | 'ROLLOVER_UITGEVOERD'
  | 'SCHOOL_ONBOARDED';

export interface AuditLogEntry {
  id: number;
  tijdstip: string;
  actorGebruikerId: number | null;
  actorSmartschoolId: string | null;
  actie: AuditActie;
  entiteitType: string;
  entiteitId: number | null;
  onderwerpLabel: string;
  detailsJson: string | null;
}

export interface AuditLogPage {
  content: AuditLogEntry[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface AuditLogFilter {
  actie?: AuditActie | null;
  entiteitType?: string | null;
  actorId?: number | null;
  vanaf?: string | null;
  tot?: string | null;
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class AuditLogService {
  private http = inject(HttpClient);
  private apiUrl =
    window.location.hostname === 'localhost'
      ? 'http://localhost:8081/api/admin/audit-log'
      : 'https://api-sp09.cloud-ap.be/api/admin/audit-log';

  zoek(filter: AuditLogFilter): Observable<AuditLogPage> {
    let params = new HttpParams()
      .set('page', String(filter.page ?? 0))
      .set('size', String(filter.size ?? 25));
    if (filter.actie) params = params.set('actie', filter.actie);
    if (filter.entiteitType) params = params.set('entiteitType', filter.entiteitType);
    if (filter.actorId != null) params = params.set('actorId', String(filter.actorId));
    if (filter.vanaf) params = params.set('vanaf', filter.vanaf);
    if (filter.tot) params = params.set('tot', filter.tot);
    return this.http.get<AuditLogPage>(this.apiUrl, { params });
  }
}
