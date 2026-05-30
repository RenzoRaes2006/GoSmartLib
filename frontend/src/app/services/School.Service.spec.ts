import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { SchoolService } from './School.Service';

describe('SchoolService', () => {
  let service: SchoolService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SchoolService],
    });
    service = TestBed.inject(SchoolService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('givenSchoolService_whenAangemaakt_thenServiceBestaat', () => {
    expect(service).toBeTruthy();
  });

  it('givenSchoolService_whenGetAlleScholen_thenGetNaarApiScholen', () => {
    service.getAlleScholen().subscribe((res) => expect(res.length).toBe(2));

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/scholen'));
    expect(req.request.method).toBe('GET');
    req.flush([{ id: 1 }, { id: 2 }]);
  });

  it('givenSchoolData_whenMaakSchoolAan_thenPostMetBodyVerstuurd', () => {
    service.maakSchoolAan({ naam: 'Test' }).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/scholen'));
    expect(req.request.method).toBe('POST');
    expect((req.request.body as any).naam).toBe('Test');
    req.flush({ id: 1, naam: 'Test' });
  });

  it('givenSchoolId_whenVerwijderSchool_thenDeleteMetIdVerstuurd', () => {
    service.verwijderSchool(7).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/scholen/7'));
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });
});
