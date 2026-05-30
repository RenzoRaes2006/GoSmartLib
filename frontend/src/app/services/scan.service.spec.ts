import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { ScanService } from './scan.service';

describe('ScanService', () => {
  let service: ScanService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ScanService],
    });
    service = TestBed.inject(ScanService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('givenScanService_whenAangemaakt_thenServiceBestaat', () => {
    expect(service).toBeTruthy();
  });

  it('givenSchoolId_whenZoekLeerlingOpSchoolId_thenGetMetSchoolIdInPath', () => {
    service.zoekLeerlingOpSchoolId(12345).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/gebruiker/schoolid/12345'));
    expect(req.request.method).toBe('GET');
    req.flush({ gebruikerID: 1 });
  });

  it('givenIsbn_whenZoekBoekOpIsbn_thenGetNaarApiBoekIsbn', () => {
    service.zoekBoekOpIsbn('9780132350884').subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/boeken/isbn/9780132350884'));
    expect(req.request.method).toBe('GET');
    req.flush({ id: 1, titel: 'Clean Code' });
  });

  it('givenBatchData_whenVerwerkBatch_thenPostNaarApiScanBatch', () => {
    service.verwerkBatch({ gebruikerId: 1, actie: 'UITLENING', isbns: ['x'] }).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/scan/batch'));
    expect(req.request.method).toBe('POST');
    expect((req.request.body as any).actie).toBe('UITLENING');
    req.flush({ actie: 'UITLENING', resultaten: [] });
  });
});
