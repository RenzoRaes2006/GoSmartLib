import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Campus {
  id: number;
  naam: string;
}

@Injectable({ providedIn: 'root' })
export class CampusService {
  private apiUrl =
    window.location.hostname === 'localhost'
      ? 'http://localhost:8081/api/gebruiker'
      : '/api/gebruiker';

  private http = inject(HttpClient);

  getMijnCampussen(): Observable<Campus[]> {
    return this.http.get<Campus[]>(`${this.apiUrl}/me/campussen`);
  }
}
