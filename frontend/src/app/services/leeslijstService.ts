import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class LeeslijstService {
  // private apiUrl = window.location.hostname.includes('cloud-ap.be')
  //   ? 'http://frontend-sp09.cloud-ap.be/api/gebruiker' // De LIVE pipeline URL
  //   : 'http://localhost:8081/api/gebruiker';
  //private apiUrl = 'http://frontend.localhost/api/gebruiker';


  private apiUrl = window.location.hostname === 'localhost' 
  ? 'http://localhost:8081/api/gebruiker' 
  : '/api/gebruiker';
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
