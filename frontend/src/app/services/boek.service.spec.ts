import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { BoekService } from './boek.service';

describe('BoekService', () => {
  let service: BoekService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BoekService],
    });
    service = TestBed.inject(BoekService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('givenBoekService_whenAangemaakt_thenServiceBestaat', () => {
    expect(service).toBeTruthy();
  });

  it('givenBoekId_whenGetBoekOpId_thenGetNaarApiBoekId', () => {
    service.getBoekOpId(7).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/boeken/7'));
    expect(req.request.method).toBe('GET');
    req.flush({ id: 7, titel: 'X' });
  });

  it('givenBoekId_whenVerwijderen_thenDeleteEnTekstrespons', () => {
    service.verwijderen(3).subscribe((r) => expect(r).toContain('verwijderd'));

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/boeken/3') && r.method === 'DELETE');
    req.flush('Boek is verwijderd.');
  });

  it('givenBoekId_whenLeenBoek_thenPostNaarGebruikerLeenEndpoint', () => {
    service.leenBoek(5).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/gebruiker/me/lenen/5'));
    expect(req.request.method).toBe('POST');
    req.flush('OK');
  });
});
