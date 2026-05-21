import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Tag, TagType } from '../models/Tag';

@Injectable({ providedIn: 'root' })
export class TagService {
  private http = inject(HttpClient);
  private apiUrl =
    window.location.hostname === 'localhost'
      ? 'http://localhost:8081/api/tags'
      : 'https://api-sp09.cloud-ap.be/api/tags';

  list(type?: TagType): Observable<Tag[]> {
    const params = type ? new HttpParams().set('type', type) : undefined;
    return this.http.get<Tag[]>(this.apiUrl, { params });
  }

  tree(type: TagType): Observable<Tag[]> {
    const params = new HttpParams().set('type', type).set('tree', 'true');
    return this.http.get<Tag[]>(this.apiUrl, { params });
  }

  create(naam: string, type: TagType, parentId?: number | null): Observable<Tag> {
    return this.http.post<Tag>(this.apiUrl, { naam, type, parentId: parentId ?? null });
  }

  rename(id: number, naam: string): Observable<Tag> {
    return this.http.put<Tag>(`${this.apiUrl}/${id}`, { naam });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
