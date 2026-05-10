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

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getAdminStatistieken stuurt GET op /admin', () => {
    service.getAdminStatistieken().subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/statistieken/admin'));
    expect(req.request.method).toBe('GET');
    req.flush({ percentageUitgeleend: 25, teLaatIngeleverd: 4, populairsteGenre: 'Fantasy', uitleningenDezeMaand: 12 });
  });

  it('getLeerkrachtStatistieken zonder klasId stuurt GET zonder query parameter', () => {
    service.getLeerkrachtStatistieken().subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/statistieken/leerkracht'));
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('getLeerkrachtStatistieken met klasId voegt query parameter toe', () => {
    service.getLeerkrachtStatistieken('5A').subscribe();

    const req = httpMock.expectOne((r) => r.url.includes('klasId=5A'));
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('getLeerlingStatistieken stuurt GET met schoolId in path', () => {
    service.getLeerlingStatistieken('123').subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/statistieken/leerling/123'));
    expect(req.request.method).toBe('GET');
    req.flush({});
  });
});
