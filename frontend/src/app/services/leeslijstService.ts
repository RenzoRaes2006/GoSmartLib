import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class LeeslijstService {
  private readonly gebruikTraefik = false;
  private apiUrl = this.gebruikTraefik
    ? 'http://frontend.localhost/api/gebruiker'
    : 'http://localhost:8081/api/gebruiker';

  constructor(private http: HttpClient) {}

  voegBoekToeAanMap(gebruikerId: number, mapNaam: string, boekId: number) {
    return this.http.post(
      `${this.apiUrl}/${gebruikerId}/mappen/${mapNaam}/boeken/${boekId}`,
      {},
      { responseType: 'text' },
    );
  }
}
