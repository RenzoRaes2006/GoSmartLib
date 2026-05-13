import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { LeerlingService } from './Leerling.service';

describe('LeerlingService', () => {
  let service: LeerlingService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [LeerlingService],
    });
    service = TestBed.inject(LeerlingService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getLeerlingen geeft pagineerquery + filters mee', () => {
    service.getLeerlingen(2, 5, '5A', 'jan').subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/leerlingen'));
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('size')).toBe('5');
    expect(req.request.params.get('klas')).toBe('5A');
    expect(req.request.params.get('zoekterm')).toBe('jan');
    req.flush({ content: [], totalElements: 0 });
  });

  it('getLeerlingStats stuurt GET /{id}/stats', () => {
    service.getLeerlingStats(42).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/leerlingen/42/stats'));
    expect(req.request.method).toBe('GET');
    req.flush({ totaalGeleend: 5, opTijdTerug: 4, teLaat: 1 });
  });
});
