import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Schooljaar } from '../models/schooljaar.model';
import { RolloverPreview, RolloverResultaat, RolloverParams } from '../models/rollover.model';

@Injectable({
  providedIn: 'root'
})
export class SchooljaarService {

  private apiUrl =
    window.location.hostname === 'localhost'
      ? 'http://localhost:8081/api/schooljaar'
      : '/api/schooljaar';

  constructor(private http: HttpClient) { }

  getActiefSchooljaar(): Observable<Schooljaar> {
    return this.http.get<Schooljaar>(`${this.apiUrl}/actief`);
  }

  getAllSchooljaren(): Observable<Schooljaar[]> {
    return this.http.get<Schooljaar[]>(this.apiUrl);
  }

  previewRollover(schoolId: number): Observable<RolloverPreview> {
    return this.http.get<RolloverPreview>(`${this.apiUrl}/${schoolId}/rollover/preview`);
  }

  voerRolloverUit(schoolId: number, params: RolloverParams): Observable<RolloverResultaat> {
    const httpParams = new HttpParams()
      .set('forceerVerloren', String(params.forceerVerloren))
      .set('nieuwNaam', params.nieuwNaam)
      .set('nieuwStartDatum', params.nieuwStartDatum)
      .set('nieuwEindDatum', params.nieuwEindDatum);

    return this.http.post<RolloverResultaat>(`${this.apiUrl}/${schoolId}/rollover`, null, { params: httpParams });
  }
}
