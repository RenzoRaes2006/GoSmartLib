import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { KlassLeeslijstService } from './klassLeeslijstService';

describe('KlassLeeslijstService', () => {
  let service: KlassLeeslijstService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [KlassLeeslijstService],
    });
    service = TestBed.inject(KlassLeeslijstService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getLeeslijstenByLeerkracht stuurt GET met leerkracht-id in URL', () => {
    service.getLeeslijstenByLeerkracht(7).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/leeslijsten/leerkracht/7'));
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('createLeeslijst stuurt POST met body', () => {
    service.createLeeslijst(1, { naam: 'Mijn lijst' } as any).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/leeslijsten/leerkracht/1'));
    expect(req.request.method).toBe('POST');
    expect((req.request.body as any).naam).toBe('Mijn lijst');
    req.flush({ id: 1, naam: 'Mijn lijst', boeken: [] });
  });

  it('deleteLeeslijst stuurt DELETE op /api/leeslijsten/{id}', () => {
    service.deleteLeeslijst(9).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/leeslijsten/9'));
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
