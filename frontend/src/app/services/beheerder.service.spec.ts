import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { BeheerderService } from './beheerder.service';

describe('BeheerderService', () => {
  let service: BeheerderService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BeheerderService],
    });
    service = TestBed.inject(BeheerderService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getAlleLeerlingen stuurt GET naar leerlingen/all endpoint', () => {
    service.getAlleLeerlingen().subscribe((res) => expect(res.length).toBe(2));

    const req = httpMock.expectOne((r) => r.url.endsWith('/leerlingen/all'));
    expect(req.request.method).toBe('GET');
    req.flush([{ id: 1 }, { id: 2 }]);
  });

  it('getAlleKlassen stuurt GET naar klassen/all endpoint', () => {
    service.getAlleKlassen().subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/klassen/all'));
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('startSchoolOp stuurt FormData via POST naar onboard endpoint', () => {
    const schoolData = { naam: 'Test', adres: 'A', hoofdAdminNaam: 'X', hoofdAdminEmail: 'x@y', klassen: ['1A'] };
    service.startSchoolOp(schoolData, null).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/school/onboard'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBeTrue();
    req.flush({ status: 'ok' });
  });
});
