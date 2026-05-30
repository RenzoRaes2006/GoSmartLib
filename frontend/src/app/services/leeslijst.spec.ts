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

  it('givenLeeslijstService_whenAangemaakt_thenServiceBestaat', () => {
    expect(service).toBeTruthy();
  });

  it('givenLeeslijstService_whenGetMijnMappen_thenGetNaarMeMappen', () => {
    service.getMijnMappen().subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/gebruiker/me/mappen'));
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('givenMapNaamEnBoekId_whenVoegBoekToeAanMijnMap_thenPostMetEmojiQueryParameter', () => {
    service.voegBoekToeAanMijnMap('Favorieten', 5, '⭐').subscribe();

    const req = httpMock.expectOne(
      (r) => r.url.includes('/api/gebruiker/me/mappen/Favorieten/boeken/5') && r.url.includes('emoji='),
    );
    expect(req.request.method).toBe('POST');
    req.flush('OK');
  });

  it('givenMapNaamEnBoekId_whenVerwijderBoekUitMijnMap_thenDeleteOpJuisteRoute', () => {
    service.verwijderBoekUitMijnMap('School', 9).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/gebruiker/me/mappen/School/boeken/9'));
    expect(req.request.method).toBe('DELETE');
    req.flush('OK');
  });
});
