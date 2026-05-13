import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { GoogleBookService } from './googleBook.service';

describe('GoogleBookService', () => {
  let service: GoogleBookService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [GoogleBookService],
    });
    service = TestBed.inject(GoogleBookService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('zoekOpIsbn mapt google books response naar nieuw boek', () => {
    let result: any;
    service.zoekOpIsbn('9780132350884').subscribe((r) => (result = r));

    const req = httpMock.expectOne((r) => r.url.includes('isbn:9780132350884'));
    expect(req.request.method).toBe('GET');
    req.flush({
      items: [
        {
          volumeInfo: {
            title: 'Clean Code',
            authors: ['Robert Martin'],
            publisher: 'Prentice Hall',
            publishedDate: '2008-08-01',
            pageCount: 464,
            categories: ['Software'],
            description: 'A handbook',
            imageLinks: { thumbnail: 'http://img/x.jpg' },
          },
        },
      ],
    });

    expect(result.titel).toBe('Clean Code');
    expect(result.auteur).toBe('Robert Martin');
    expect(result.uitgaveJaar).toBe('2008');
    expect(result.afbeelding.startsWith('https://')).toBeTrue();
  });

  it('zoekOpIsbn geeft null terug wanneer er geen items zijn', () => {
    let result: any = 'sentinel';
    service.zoekOpIsbn('0000000000').subscribe((r) => (result = r));

    httpMock.expectOne((r) => r.url.includes('isbn:0000000000')).flush({});

    expect(result).toBeNull();
  });
});
