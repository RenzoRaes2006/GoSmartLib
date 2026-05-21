import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class LeeslijstService {
  private apiUrl =
    window.location.hostname === 'localhost'
      ? 'http://localhost:8081/api/gebruiker'
      : '/api/gebruiker';

  constructor(private http: HttpClient) {}

  voegBoekToeAanMijnMap(mapNaam: string, boekId: number, emoji: string = '📁') {
    return this.http.post(
      `${this.apiUrl}/me/mappen/${mapNaam}/boeken/${boekId}?emoji=${emoji}`,
      {},
      { responseType: 'text' },
    );
  }

  getMijnMappen() {
    return this.http.get<any[]>(`${this.apiUrl}/me/mappen`);
  }

  verwijderBoekUitMijnMap(mapNaam: string, boekId: number) {
    return this.http.delete(`${this.apiUrl}/me/mappen/${mapNaam}/boeken/${boekId}`, {
      responseType: 'text',
    });
  }
}
