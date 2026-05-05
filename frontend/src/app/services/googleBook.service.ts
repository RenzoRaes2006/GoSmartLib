import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class GoogleBookService {
  private apiUrl = 'https://www.googleapis.com/books/v1/volumes';

  constructor(private http: HttpClient) {}

  zoekOpIsbn(isbn: string): Observable<any> {
    return this.http.get(`${this.apiUrl}?q=isbn:${isbn}`).pipe(
      map((response: any) => {
        if (response.items && response.items.length > 0) {
          const boekInfo = response.items[0].volumeInfo;
          return {
            titel: boekInfo.title || '',
            auteur: boekInfo.authors ? boekInfo.authors.join(', ') : '',
            isbn: isbn,
            uitgever: boekInfo.publisher || '',
            uitgaveJaar: boekInfo.publishedDate ? boekInfo.publishedDate.substring(0, 4) : '',
            pagina: boekInfo.pageCount ? boekInfo.pageCount.toString() : '',
            genre: boekInfo.categories ? boekInfo.categories.join(', ') : '',
            beschrijving: boekInfo.description || '',
            afbeelding: boekInfo.imageLinks ? boekInfo.imageLinks.thumbnail.replace('http://', 'https://') : '',
          };
        }
        return null;
      }),
    );
  }
}
