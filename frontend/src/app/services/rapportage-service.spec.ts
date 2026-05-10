import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { RapportageService } from './rapportage-service';

describe('RapportageService', () => {
  let service: RapportageService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [RapportageService],
    });
    service = TestBed.inject(RapportageService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('rapporteerBeoordeling stuurt POST met reden in body', () => {
    service.rapporteerBeoordeling(7, 'spoiler').subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/reviews/7/report'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toBe('spoiler');
    req.flush({});
  });

  it('getRapportages stuurt GET /api/reports', () => {
    service.getRapportages().subscribe((res) => expect(res.length).toBe(1));

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/reports'));
    expect(req.request.method).toBe('GET');
    req.flush([{ id: 1 }]);
  });

  it('negeerRapportage stuurt DELETE met rapport-id', () => {
    service.negeerRapportage(11).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/reports/11'));
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });
});
