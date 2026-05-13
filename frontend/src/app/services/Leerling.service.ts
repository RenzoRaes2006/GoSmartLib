import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LeerlingService {
  private http = inject(HttpClient);

  // use the dynamic api url from the environment
  //private baseUrl = true ? 'http://localhost:8081/api/leerlingen' : 'https://api-sp09.cloud-ap.be/api/leerlingen';

  private baseUrl =
    window.location.hostname === 'localhost'
      ? 'http://localhost:8081/api/leerlingen'
      : 'https://api-sp09.cloud-ap.be/api/leerlingen';
  ;

  getLeerlingen(page: number, size: number, klas: string, zoekterm: string): Observable<any> {
    // HttpParams is de veiligste manier om query parameters toe te voegen
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (klas) {
      params = params.set('klas', klas);
    }
    if (zoekterm) {
      params = params.set('zoekterm', zoekterm);
    }

    return this.http.get<any>(this.baseUrl, { params });
  }

  getLeerlingStats(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}/stats`);
  }
}