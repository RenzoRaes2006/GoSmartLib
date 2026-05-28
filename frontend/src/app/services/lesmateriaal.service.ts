import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Lesmateriaal } from '../models/Lesmateriaal';

@Injectable({ providedIn: 'root' })
export class LesmateriaalService {
  private http = inject(HttpClient);

  private apiBase =
    window.location.hostname === 'localhost' ? 'http://localhost:8081' : '';

  list(boekId: number): Observable<Lesmateriaal[]> {
    return this.http.get<Lesmateriaal[]>(`${this.apiBase}/api/boeken/${boekId}/lesmateriaal`);
  }

  upload(boekId: number, file: File, beschrijving?: string): Observable<Lesmateriaal> {
    const formData = new FormData();
    formData.append('file', file);
    if (beschrijving) {
      formData.append('beschrijving', beschrijving);
    }
    return this.http.post<Lesmateriaal>(
      `${this.apiBase}/api/boeken/${boekId}/lesmateriaal`,
      formData,
    );
  }

  downloadUrl(lesmateriaalId: number): string {
    return `${this.apiBase}/api/lesmateriaal/${lesmateriaalId}/download`;
  }

  download(lesmateriaalId: number): Observable<Blob> {
    return this.http.get(this.downloadUrl(lesmateriaalId), { responseType: 'blob' });
  }

  updateBeschrijving(lesmateriaalId: number, beschrijving: string): Observable<Lesmateriaal> {
    return this.http.put<Lesmateriaal>(`${this.apiBase}/api/lesmateriaal/${lesmateriaalId}`, {
      beschrijving,
    });
  }

  delete(lesmateriaalId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBase}/api/lesmateriaal/${lesmateriaalId}`);
  }
}
