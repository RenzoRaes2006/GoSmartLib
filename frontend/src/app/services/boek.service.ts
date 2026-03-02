import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Boek } from '../models/Boek';
import { NieuwBoek } from '../models/NieuwBoek';

@Injectable({
  providedIn: 'root',
})
export class BoekService {
  private apiUrl = 'http://frontend.localhost/api/boeken';

  constructor(private http: HttpClient) {}

  getBoeken(): Observable<Boek[]> {
    return this.http.get<Boek[]>(this.apiUrl);
  }

  toevoegen(boek: NieuwBoek): Observable<Boek> {
    return this.http.post<Boek>(this.apiUrl, boek);
  }
}
