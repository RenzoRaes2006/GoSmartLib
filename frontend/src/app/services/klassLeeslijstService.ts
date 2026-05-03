import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { KlassLeeslijst } from '../models/klass-leeslijst.model';
import { LeeslijstRequest } from '../models/leeslijst-request.model';

@Injectable({
  providedIn: 'root'
})
export class KlassLeeslijstService {

  // Vervang dit door de daadwerkelijke URL/poort van je Spring Boot backend
  private apiUrl = 'http://localhost:8081/api/leeslijsten';

  constructor(private http: HttpClient) { }

  /**
   * 1. Maakt een nieuwe leeslijst aan voor een specifieke leerkracht
   */
  createLeeslijst(leerkrachtId: number, request: LeeslijstRequest): Observable<KlassLeeslijst> {
    return this.http.post<KlassLeeslijst>(`${this.apiUrl}/leerkracht/${leerkrachtId}`, request);
  }

  /**
   * 2. Haalt alle leeslijsten op van een specifieke leerkracht
   */
  getLeeslijstenByLeerkracht(leerkrachtId: number): Observable<KlassLeeslijst[]> {
    return this.http.get<KlassLeeslijst[]>(`${this.apiUrl}/leerkracht/${leerkrachtId}`);
  }

  /**
   * 3. Voegt één specifiek boek toe aan een bestaande leeslijst
   */
  addBoekToLeeslijst(leeslijstId: number, boekId: number): Observable<KlassLeeslijst> {
    // We sturen een lege body {} mee, omdat het een POST request is, 
    // maar de ID's al in de URL (als PathVariables) zitten.
    return this.http.post<KlassLeeslijst>(`${this.apiUrl}/${leeslijstId}/boeken/${boekId}`, {});
  }

  /**
 * 4. Haalt de details van één specifieke leeslijst op (naam + boekenlijst)
 */
  getLeeslijstDetails(id: number): Observable<KlassLeeslijst> {
    return this.http.get<KlassLeeslijst>(`${this.apiUrl}/${id}`);
  }

  /**
   * Verwijdert een specifiek boek uit een bestaande leeslijst
   */
  verwijderBoekUitLeeslijst(leeslijstId: number, boekId: number): Observable<KlassLeeslijst> {
    return this.http.delete<KlassLeeslijst>(`${this.apiUrl}/${leeslijstId}/boeken/${boekId}`);
  }

  /**
 * Werkt de naam van de leeslijst bij
 */
  updateLeeslijstNaam(id: number, nieuweNaam: string): Observable<KlassLeeslijst> {
    return this.http.put<KlassLeeslijst>(`${this.apiUrl}/${id}/naam`, nieuweNaam);
  }

  /**
   * Verwijdert een volledige leeslijst
   */
  deleteLeeslijst(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}