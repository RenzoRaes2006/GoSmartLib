import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { StatistiekService } from './statistiek.service';

describe('StatistiekService', () => {
  let service: StatistiekService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [StatistiekService],
    });
    service = TestBed.inject(StatistiekService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('givenStatistiekService_whenAangemaakt_thenServiceBestaat', () => {
    expect(service).toBeTruthy();
  });

  it('givenStatistiekService_whenGetAdminStatistieken_thenGetNaarApiStatistiekenAdmin', () => {
    service.getAdminStatistieken().subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/statistieken/admin'));
    expect(req.request.method).toBe('GET');
    req.flush({ percentageUitgeleend: 25, teLaatIngeleverd: 4, populairsteGenre: 'Fantasy', uitleningenDezeMaand: 12 });
  });

  it('givenGeenKlasId_whenGetLeerkrachtStatistieken_thenGetZonderQueryParameter', () => {
    service.getLeerkrachtStatistieken().subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/statistieken/leerkracht'));
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('givenKlasId_whenGetLeerkrachtStatistieken_thenKlasIdQueryParameterToegevoegd', () => {
    service.getLeerkrachtStatistieken('5A').subscribe();

    const req = httpMock.expectOne((r) => r.url.includes('klasId=5A'));
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('givenSchoolId_whenGetLeerlingStatistieken_thenGetMetSchoolIdInPath', () => {
    service.getLeerlingStatistieken('123').subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/statistieken/leerling/123'));
    expect(req.request.method).toBe('GET');
    req.flush({});
  });
});
