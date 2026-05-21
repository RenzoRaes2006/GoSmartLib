import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { KlassLeeslijst } from '../models/klass-leeslijst.model';
import { LeeslijstRequest } from '../models/leeslijst-request.model';

@Injectable({
  providedIn: 'root'
})
export class KlassLeeslijstService {

  private apiUrl =
    window.location.hostname === 'localhost'
      ? 'http://localhost:8081/api/leeslijsten'
      : '/api/leeslijsten';

  constructor(private http: HttpClient) { }

  createLeeslijst(leerkrachtId: number, request: LeeslijstRequest): Observable<KlassLeeslijst> {
    return this.http.post<KlassLeeslijst>(`${this.apiUrl}/leerkracht/${leerkrachtId}`, request);
  }

  /**
   * Maakt een nieuwe leeslijst aan voor de huidige ingelogde leerkracht.
   * Geen leerkracht-ID nodig — wordt afgeleid uit de JWT-token op de backend.
   */
  createLeeslijstVoorMij(request: LeeslijstRequest): Observable<KlassLeeslijst> {
    return this.http.post<KlassLeeslijst>(`${this.apiUrl}`, request);
  }

  getLeeslijstenByLeerkracht(leerkrachtId: number): Observable<KlassLeeslijst[]> {
    return this.http.get<KlassLeeslijst[]>(`${this.apiUrl}/leerkracht/${leerkrachtId}`);
  }

  /**
   * Haalt alle leeslijsten van de huidige ingelogde leerkracht op.
   */
  getMijnLeerkrachtLijsten(): Observable<KlassLeeslijst[]> {
    return this.http.get<KlassLeeslijst[]>(`${this.apiUrl}/mijn-leerkracht-lijsten`);
  }

  /**
   * Haalt alle voor de huidige leerling zichtbare leeslijsten op
   * (klas-leeslijsten + persoonlijk toegewezen).
   */
  getLeeslijstenVoorMij(): Observable<KlassLeeslijst[]> {
    return this.http.get<KlassLeeslijst[]>(`${this.apiUrl}/mijn-leerling-lijsten`);
  }

  addBoekToLeeslijst(leeslijstId: number, boekId: number): Observable<KlassLeeslijst> {
    return this.http.post<KlassLeeslijst>(`${this.apiUrl}/${leeslijstId}/boeken/${boekId}`, {});
  }

  getLeeslijstDetails(id: number): Observable<KlassLeeslijst> {
    return this.http.get<KlassLeeslijst>(`${this.apiUrl}/${id}`);
  }

  verwijderBoekUitLeeslijst(leeslijstId: number, boekId: number): Observable<KlassLeeslijst> {
    return this.http.delete<KlassLeeslijst>(`${this.apiUrl}/${leeslijstId}/boeken/${boekId}`);
  }

  updateLeeslijstNaam(id: number, nieuweNaam: string): Observable<KlassLeeslijst> {
    return this.http.put<KlassLeeslijst>(`${this.apiUrl}/${id}/naam`, nieuweNaam);
  }

  updateBeschrijving(id: number, nieuweBeschrijving: string): Observable<KlassLeeslijst> {
    return this.http.put<KlassLeeslijst>(`${this.apiUrl}/${id}/beschrijving`, nieuweBeschrijving, {
      headers: { 'Content-Type': 'text/plain' }
    });
  }

  updateZichtbaar(id: number, zichtbaar: boolean): Observable<KlassLeeslijst> {
    return this.http.put<KlassLeeslijst>(`${this.apiUrl}/${id}/zichtbaar`, zichtbaar);
  }

  updateKoppeling(id: number, koppeling: { klas?: string | null; leerlingIds?: number[] | null }): Observable<KlassLeeslijst> {
    return this.http.put<KlassLeeslijst>(`${this.apiUrl}/${id}/koppeling`, koppeling);
  }

  deleteLeeslijst(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getAllKlassenLeeslijsten(): Observable<KlassLeeslijst[]> {
    return this.http.get<KlassLeeslijst[]>(`${this.apiUrl}/alle`);
  }

  /**
   * Haalt het archief op: voor een leerkracht zijn eigen gearchiveerde lijsten,
   * voor de bibbeheerder alle gearchiveerde lijsten. Backend bepaalt op basis van rol.
   */
  getArchief(): Observable<KlassLeeslijst[]> {
    return this.http.get<KlassLeeslijst[]>(`${this.apiUrl}/archief`);
  }

  /**
   * Dearchiveert een leeslijst: zet gearchiveerd=false en zichtbaar=false zodat
   * de leerkracht hem als concept terugkrijgt en bewust moet publiceren.
   */
  dearchiveer(id: number): Observable<KlassLeeslijst> {
    return this.http.patch<KlassLeeslijst>(`${this.apiUrl}/${id}/dearchiveer`, {});
  }
}
