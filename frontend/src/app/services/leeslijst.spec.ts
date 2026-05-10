import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { LeeslijstService } from './leeslijstService';

describe('LeeslijstService', () => {
  let service: LeeslijstService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [LeeslijstService],
    });
    service = TestBed.inject(LeeslijstService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getMappenVanGebruiker stuurt GET met gebruiker-id', () => {
    service.getMappenVanGebruiker(1).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/gebruiker/1/mappen'));
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('voegBoekToeAanMap stuurt POST met emoji query parameter', () => {
    service.voegBoekToeAanMap(2, 'Favorieten', 5, '⭐').subscribe();

    const req = httpMock.expectOne(
      (r) => r.url.includes('/api/gebruiker/2/mappen/Favorieten/boeken/5') && r.url.includes('emoji='),
    );
    expect(req.request.method).toBe('POST');
    req.flush('OK');
  });

  it('verwijderBoekUitMap stuurt DELETE op de juiste route', () => {
    service.verwijderBoekUitMap(1, 'School', 9).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/gebruiker/1/mappen/School/boeken/9'));
    expect(req.request.method).toBe('DELETE');
    req.flush('OK');
  });
});
