import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BeheerderService {
  private http = inject(HttpClient);
  private apiUrl = true ? 'http://localhost:8081/api/leerlingen' : 'https://api-sp09.cloud-ap.be/api/leerlingen';

  startSchoolOp(schoolData: any, file: File | null): Observable<any> {
    const formData = new FormData();
    
    // Voeg alle tekst-velden toe
    formData.append('schoolNaam', schoolData.naam);
    formData.append('adres', schoolData.adres);
    formData.append('hoofdAdminNaam', schoolData.hoofdAdminNaam);
    formData.append('hoofdAdminEmail', schoolData.hoofdAdminEmail);
    
    // Voeg klassen toe als een lijst
    if (schoolData.klassen && schoolData.klassen.length > 0) {
      schoolData.klassen.forEach((klas: string) => {
        formData.append('klassen', klas);
      });
    }

    // Voeg het bestand toe (als het er is)
    if (file) {
      formData.append('gebruikersBestand', file);
    }

    return this.http.post(`${this.apiUrl}/school/onboard`, formData);
  }

  getAlleLeerlingen(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/leerlingen/all`);
  }

  getAlleKlassen(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/klassen/all`);
  }
}