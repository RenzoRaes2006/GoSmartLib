import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
  TestRequest,
} from '@angular/common/http/testing';
import { AdminAuditLog } from './admin-audit-log';
import { AuditLogPage } from '../services/audit-log.service';

describe('AdminAuditLog', () => {
  let component: AdminAuditLog;
  let fixture: ComponentFixture<AdminAuditLog>;
  let httpMock: HttpTestingController;

  const pagina: AuditLogPage = {
    content: [
      {
        id: 1,
        tijdstip: '2026-05-25T10:00:00',
        actorGebruikerId: 5,
        actorSmartschoolId: 'admin-1',
        actie: 'KLAS_GEWIJZIGD',
        entiteitType: 'Gebruiker',
        entiteitId: 9,
        detailsJson: '{"van":"1A","naar":"1B"}',
        onderwerpLabel: 'll-9',
      },
      {
        id: 2,
        tijdstip: '2026-05-25T09:00:00',
        actorGebruikerId: null,
        actorSmartschoolId: null,
        actie: 'GEBRUIKER_VERTROKKEN_SYNC',
        entiteitType: 'Gebruiker',
        entiteitId: 11,
        detailsJson: null,
        onderwerpLabel: 'll-11',
      },
    ],
    totalElements: 2,
    totalPages: 1,
    number: 0,
    size: 25,
  };

  function pakLaatsteRequest(): TestRequest {
    return httpMock.expectOne((r) => r.url.endsWith('/api/admin/audit-log'));
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminAuditLog, HttpClientTestingModule],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminAuditLog);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('givenAuditLog_whenInit_thenInitielePaginaGeladen', () => {
    fixture.detectChanges();
    pakLaatsteRequest().flush(pagina);
    expect(component.rijen().length).toBe(2);
    expect(component.totalElements()).toBe(2);
  });

  it('givenGeldigJson_whenParseDetails_thenKeyValueParenTeruggegeven', () => {
    fixture.detectChanges();
    pakLaatsteRequest().flush(pagina);
    const kv = component.parseDetails('{"van":"1A","naar":"1B"}');
    expect(kv).toEqual([
      { k: 'van', v: '1A' },
      { k: 'naar', v: '1B' },
    ]);
  });

  it('givenNull_whenParseDetails_thenLegeLijstTeruggegeven', () => {
    fixture.detectChanges();
    pakLaatsteRequest().flush(pagina);
    expect(component.parseDetails(null)).toEqual([]);
  });

  it('givenOngeldigeJson_whenParseDetails_thenRawWaardeTeruggegeven', () => {
    fixture.detectChanges();
    pakLaatsteRequest().flush(pagina);
    expect(component.parseDetails('niet json')).toEqual([{ k: 'raw', v: 'niet json' }]);
  });

  it('givenFilters_whenToepassen_thenPaginatieResetEnFiltersMeegestuurd', () => {
    fixture.detectChanges();
    pakLaatsteRequest().flush(pagina);

    component.pagina.set(3);
    component.actie = 'GEBRUIKER_VERTROKKEN';
    component.entiteitType = 'Gebruiker';
    component.vanaf = '2026-01-01';
    component.tot = '2026-02-01';
    component.toepassen();

    const req = pakLaatsteRequest();
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('actie')).toBe('GEBRUIKER_VERTROKKEN');
    expect(req.request.params.get('entiteitType')).toBe('Gebruiker');
    expect(req.request.params.get('vanaf')).toBe('2026-01-01T00:00:00');
    expect(req.request.params.get('tot')).toBe('2026-02-01T23:59:59');
    req.flush({ ...pagina, content: [] });
    expect(component.pagina()).toBe(0);
  });

  it('givenActieveFilters_whenWis_thenFilterveldenLeegEnHerladen', () => {
    fixture.detectChanges();
    pakLaatsteRequest().flush(pagina);

    component.actie = 'KLAS_GEWIJZIGD';
    component.entiteitType = 'Gebruiker';
    component.vanaf = '2026-01-01';
    component.tot = '2026-02-01';

    component.wis();
    const req = pakLaatsteRequest();
    expect(req.request.params.has('actie')).toBeFalse();
    expect(req.request.params.has('entiteitType')).toBeFalse();
    expect(req.request.params.has('vanaf')).toBeFalse();
    expect(req.request.params.has('tot')).toBeFalse();
    req.flush({ ...pagina, content: [] });

    expect(component.actie).toBe('');
    expect(component.entiteitType).toBe('');
    expect(component.vanaf).toBe('');
    expect(component.tot).toBe('');
  });

  it('givenMeerderePaginas_whenVolgende_thenPaginaVerhoogdEnGeladen', () => {
    fixture.detectChanges();
    pakLaatsteRequest().flush({ ...pagina, totalPages: 3 });

    component.volgende();
    const req = pakLaatsteRequest();
    expect(req.request.params.get('page')).toBe('1');
    req.flush({ ...pagina, totalPages: 3 });
    expect(component.pagina()).toBe(1);
  });

  it('givenEerstePagina_whenVorige_thenGeenVerandering', () => {
    fixture.detectChanges();
    pakLaatsteRequest().flush(pagina);

    component.vorige();
    httpMock.expectNone((r) => r.url.endsWith('/api/admin/audit-log'));
    expect(component.pagina()).toBe(0);
  });

  it('givenHttpFout_whenLaden_thenFoutmeldingGetoond', () => {
    fixture.detectChanges();
    pakLaatsteRequest().flush('Server stuk', {
      status: 500,
      statusText: 'Internal Server Error',
    });
    expect(component.fout()).toBeTruthy();
    expect(component.laden()).toBeFalse();
  });

  it('givenEnumCode_whenActieLabel_thenMenselijkeTekstTeruggegeven', () => {
    expect(component.actieLabel('KLAS_GEWIJZIGD')).toBe('Klas gewijzigd');
    expect(component.actieLabel('GEBRUIKER_VERTROKKEN_SYNC')).toBe('Gebruiker vertrokken (sync)');
  });
});
