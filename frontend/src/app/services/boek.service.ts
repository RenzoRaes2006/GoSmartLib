import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Boek } from '../models/Boek';
@Injectable({
  providedIn: 'root'
})
export class BoekService {
  private apiUrl = 'http://frontend.localhost/api/boeken';

  constructor(private http: HttpClient) { }

  getBoeken(): Observable<Boek[]> {
    return this.http.get<Boek[]>(this.apiUrl);
  }
}