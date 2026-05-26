import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { AuditLogPage, AuditLogService } from './audit-log.service';

describe('AuditLogService', () => {
  let service: AuditLogService;
  let httpMock: HttpTestingController;

  const lege: AuditLogPage = {
    content: [],
    totalElements: 0,
    totalPages: 1,
    number: 0,
    size: 25,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuditLogService],
    });
    service = TestBed.inject(AuditLogService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('zoek zonder filters stuurt enkel page+size', () => {
    service.zoek({}).subscribe();
    const req = httpMock.expectOne((r) => r.url.endsWith('/api/admin/audit-log'));
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('25');
    expect(req.request.params.has('actie')).toBeFalse();
    expect(req.request.params.has('entiteitType')).toBeFalse();
    expect(req.request.params.has('actorId')).toBeFalse();
    expect(req.request.params.has('vanaf')).toBeFalse();
    expect(req.request.params.has('tot')).toBeFalse();
    req.flush(lege);
  });

  it('zoek bouwt alle filter-params correct', () => {
    service
      .zoek({
        actie: 'GEBRUIKER_VERTROKKEN',
        entiteitType: 'Gebruiker',
        actorId: 7,
        vanaf: '2026-01-01T00:00:00',
        tot: '2026-02-01T00:00:00',
        page: 2,
        size: 50,
      })
      .subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/admin/audit-log'));
    expect(req.request.params.get('actie')).toBe('GEBRUIKER_VERTROKKEN');
    expect(req.request.params.get('entiteitType')).toBe('Gebruiker');
    expect(req.request.params.get('actorId')).toBe('7');
    expect(req.request.params.get('vanaf')).toBe('2026-01-01T00:00:00');
    expect(req.request.params.get('tot')).toBe('2026-02-01T00:00:00');
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('size')).toBe('50');
    req.flush(lege);
  });

  it('zoek met null-filters laat ze weg', () => {
    service
      .zoek({ actie: null, entiteitType: null, actorId: null, vanaf: null, tot: null })
      .subscribe();
    const req = httpMock.expectOne((r) => r.url.endsWith('/api/admin/audit-log'));
    expect(req.request.params.has('actie')).toBeFalse();
    expect(req.request.params.has('actorId')).toBeFalse();
    req.flush(lege);
  });

  it('zoek geeft de page-respons door', () => {
    const data: AuditLogPage = {
      content: [
        {
          id: 1,
          tijdstip: '2026-05-25T10:00:00',
          actorGebruikerId: 5,
          actorSmartschoolId: 'admin-1',
          actie: 'GEBRUIKER_TOEGEVOEGD',
          entiteitType: 'Gebruiker',
          entiteitId: 9,
          detailsJson: '{"smartschoolId":"ll-9"}',
          onderwerpLabel: 'll-9',
        },
      ],
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 25,
    };
    let ontvangen: AuditLogPage | undefined;
    service.zoek({}).subscribe((res) => (ontvangen = res));
    httpMock.expectOne((r) => r.url.endsWith('/api/admin/audit-log')).flush(data);
    expect(ontvangen).toEqual(data);
  });
});
