import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AdminGebruiker {
  id: number;
  smartschoolId: string;
  klas: string;
  rol: 'LEERLING' | 'LEERKRACHT' | 'BEHEERDER' | 'ADMIN' | null;
  isBeheerder: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AdminGebruikerService {
  private http = inject(HttpClient);

  private apiUrl = window.location.hostname === 'localhost'
    ? 'http://localhost:8081/api/admin'
    : 'https://api-sp09.cloud-ap.be/api/admin';

  getAlleLeerkrachten(): Observable<AdminGebruiker[]> {
    return this.http.get<AdminGebruiker[]>(`${this.apiUrl}/leerkrachten`);
  }

  setBeheerder(id: number, isBeheerder: boolean): Observable<AdminGebruiker> {
    return this.http.patch<AdminGebruiker>(
      `${this.apiUrl}/gebruikers/${id}/beheerder`,
      { isBeheerder }
    );
  }
}
