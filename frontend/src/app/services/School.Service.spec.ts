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

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getAlleScholen stuurt GET op /api/scholen', () => {
    service.getAlleScholen().subscribe((res) => expect(res.length).toBe(2));

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/scholen'));
    expect(req.request.method).toBe('GET');
    req.flush([{ id: 1 }, { id: 2 }]);
  });

  it('maakSchoolAan stuurt POST met body', () => {
    service.maakSchoolAan({ naam: 'Test' }).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/scholen'));
    expect(req.request.method).toBe('POST');
    expect((req.request.body as any).naam).toBe('Test');
    req.flush({ id: 1, naam: 'Test' });
  });

  it('verwijderSchool stuurt DELETE met id', () => {
    service.verwijderSchool(7).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/scholen/7'));
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });
});
