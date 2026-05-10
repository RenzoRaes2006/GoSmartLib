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

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getBeoordelingenOpBoekId stuurt GET met boek-id', () => {
    service.getBeoordelingenOpBoekId(3).subscribe((res) => expect(res.length).toBe(1));

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/books/3/reviews'));
    expect(req.request.method).toBe('GET');
    req.flush([{ id: 1 }]);
  });

  it('voegBeoordelingToe stuurt POST met body', () => {
    service.voegBeoordelingToe({ inhoud: 'top' } as any).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/reviews'));
    expect(req.request.method).toBe('POST');
    expect((req.request.body as any).inhoud).toBe('top');
    req.flush({ id: 1, inhoud: 'top' });
  });

  it('verwijderRapporteerdeBeoordeling stuurt DELETE', () => {
    service.verwijderRapporteerdeBeoordeling(9).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/reviews/9'));
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });
});
