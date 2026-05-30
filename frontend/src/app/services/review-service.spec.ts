import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { ReviewService } from './review-service';

describe('ReviewService', () => {
  let service: ReviewService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ReviewService],
    });
    service = TestBed.inject(ReviewService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('givenReviewService_whenAangemaakt_thenServiceBestaat', () => {
    expect(service).toBeTruthy();
  });

  it('givenBoekId_whenGetBeoordelingenOpBoekId_thenGetMetBoekIdVerstuurd', () => {
    service.getBeoordelingenOpBoekId(3).subscribe((res) => expect(res.length).toBe(1));

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/books/3/reviews'));
    expect(req.request.method).toBe('GET');
    req.flush([{ id: 1 }]);
  });

  it('givenBeoordelingData_whenVoegBeoordelingToe_thenPostMetBodyVerstuurd', () => {
    service.voegBeoordelingToe({ inhoud: 'top' } as any).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/reviews'));
    expect(req.request.method).toBe('POST');
    expect((req.request.body as any).inhoud).toBe('top');
    req.flush({ id: 1, inhoud: 'top' });
  });

  it('givenBeoordelingId_whenVerwijderRapporteerdeBeoordeling_thenDeleteVerstuurd', () => {
    service.verwijderRapporteerdeBeoordeling(9).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/reviews/9'));
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });
});
