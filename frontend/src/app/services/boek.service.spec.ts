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

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getBoekOpId stuurt GET /api/boeken/{id}', () => {
    service.getBoekOpId(7).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/boeken/7'));
    expect(req.request.method).toBe('GET');
    req.flush({ id: 7, titel: 'X' });
  });

  it('verwijderen stuurt DELETE en accepteert tekstrespons', () => {
    service.verwijderen(3).subscribe((r) => expect(r).toContain('verwijderd'));

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/boeken/3') && r.method === 'DELETE');
    req.flush('Boek is verwijderd.');
  });

  it('leenBoek stuurt POST naar gebruiker-leen-endpoint', () => {
    service.leenBoek(2, 5).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/gebruiker/2/lenen/5'));
    expect(req.request.method).toBe('POST');
    req.flush('OK');
  });
});
