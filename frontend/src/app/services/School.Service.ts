import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SchoolService {
  private http = inject(HttpClient);
  
  private apiUrl = window.location.hostname === 'localhost'
    ? 'http://localhost:8081/api/scholen'
    : 'https://api-sp09.cloud-ap.be/api/scholen';

  // fetch the school "coat hooks" (basic info)
  getAlleScholen(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  // save a new school to the database
  maakSchoolAan(school: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, school);
  }

  // fetch live data via spring boot (which talks to smartschool api)
  getLiveSmartschoolData(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}/live-data`);
  }

  // delete a school by id
  verwijderSchool(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
}