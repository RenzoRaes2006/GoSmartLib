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

  it('givenGoogleBookService_whenAangemaakt_thenServiceBestaat', () => {
    expect(service).toBeTruthy();
  });

  it('givenGeldigIsbn_whenZoekOpIsbn_thenBackendAangeropenEnBoekTeruggegeven', () => {
    let result: any;
    service.zoekOpIsbn('9780132350884').subscribe((r) => (result = r));

    const req = httpMock.expectOne((r) => r.url.includes('/isbn/9780132350884'));
    expect(req.request.method).toBe('GET');
    req.flush({
      titel: 'Clean Code',
      auteur: 'Robert Martin',
      isbn: '9780132350884',
      uitgever: 'Prentice Hall',
      uitgaveJaar: '2008',
      pagina: '464',
      beschrijving: 'A handbook',
      afbeelding: 'https://img/x.jpg',
    });

    expect(result.titel).toBe('Clean Code');
    expect(result.auteur).toBe('Robert Martin');
    expect(result.uitgaveJaar).toBe('2008');
    expect(result.afbeelding.startsWith('https://')).toBeTrue();
  });

  it('givenOnbekendIsbn_whenZoekOpIsbn_thenNullTeruggegeven', () => {
    let result: any = 'sentinel';
    service.zoekOpIsbn('0000000000').subscribe((r) => (result = r));

    httpMock.expectOne((r) => r.url.includes('/isbn/0000000000')).flush(null);

    expect(result).toBeNull();
  });
});
