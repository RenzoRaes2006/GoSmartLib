import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class LeeslijstService {
  private apiUrl = 'http://frontend.localhost/api/gebruiker';

  constructor(private http: HttpClient) {}

  voegBoekToeAanMap(gebruikerId: number, mapNaam: string, boekId: number) {
    return this.http.post(
      `${this.apiUrl}/${gebruikerId}/mappen/${mapNaam}/boeken/${boekId}`,
      {},
      { responseType: 'text' },
    );
  }

  getMappenVanGebruiker(gebruikerId: number) {
    return this.http.get<any[]>(`${this.apiUrl}/${gebruikerId}/mappen`);
  }

  verwijderBoekUitMap(gebruikerId: number, mapNaam: string, boekId: number) {
    return this.http.delete(`${this.apiUrl}/${gebruikerId}/mappen/${mapNaam}/boeken/${boekId}`, {
      responseType: 'text',
    });
  }
}
