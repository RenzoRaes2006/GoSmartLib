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

  it('givenRapportageService_whenAangemaakt_thenServiceBestaat', () => {
    expect(service).toBeTruthy();
  });

  it('givenBeoordelingIdEnReden_whenRapporteerBeoordeling_thenPostMetRedenInBody', () => {
    service.rapporteerBeoordeling(7, 'spoiler').subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/reviews/7/report'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toBe('spoiler');
    req.flush({});
  });

  it('givenRapportageService_whenGetRapportages_thenGetNaarApiReports', () => {
    service.getRapportages().subscribe((res) => expect(res.length).toBe(1));

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/reports'));
    expect(req.request.method).toBe('GET');
    req.flush([{ id: 1 }]);
  });

  it('givenRapportId_whenNegeerRapportage_thenDeleteMetRapportIdVerstuurd', () => {
    service.negeerRapportage(11).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/reports/11'));
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });
});
