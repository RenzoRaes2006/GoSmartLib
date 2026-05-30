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

  it('givenKlassLeeslijstService_whenAangemaakt_thenServiceBestaat', () => {
    expect(service).toBeTruthy();
  });

  it('givenLeerkrachtId_whenGetLeeslijstenByLeerkracht_thenGetMetLeerkrachtIdInUrl', () => {
    service.getLeeslijstenByLeerkracht(7).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/leeslijsten/leerkracht/7'));
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('givenKlassLeeslijstService_whenGetMijnLeerkrachtLijsten_thenGetNaarMijnLeerkrachtLijsten', () => {
    service.getMijnLeerkrachtLijsten().subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/leeslijsten/mijn-leerkracht-lijsten'));
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('givenKlassLeeslijstService_whenGetLeeslijstenVoorMij_thenGetNaarMijnLeerlingLijsten', () => {
    service.getLeeslijstenVoorMij().subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/leeslijsten/mijn-leerling-lijsten'));
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('givenLeeslijstData_whenCreateLeeslijst_thenPostMetBodyVerstuurd', () => {
    service.createLeeslijst(1, { naam: 'Mijn lijst' } as any).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/leeslijsten/leerkracht/1'));
    expect(req.request.method).toBe('POST');
    expect((req.request.body as any).naam).toBe('Mijn lijst');
    req.flush({ id: 1, naam: 'Mijn lijst', boeken: [] });
  });

  it('givenLeeslijstData_whenCreateLeeslijstVoorMij_thenPostNaarApiLeeslijstenMetBody', () => {
    service.createLeeslijstVoorMij({ naam: 'Klas-lijst', klas: '5INF1', zichtbaar: true } as any).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/leeslijsten') || r.url.endsWith('/leeslijsten'));
    expect(req.request.method).toBe('POST');
    expect((req.request.body as any).klas).toBe('5INF1');
    req.flush({ id: 1, naam: 'Klas-lijst', klas: '5INF1', zichtbaar: true, boeken: [] });
  });

  it('givenIdEnBeschrijving_whenUpdateBeschrijving_thenPutNaarIdBeschrijving', () => {
    service.updateBeschrijving(7, 'Nieuwe tekst').subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/leeslijsten/7/beschrijving'));
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toBe('Nieuwe tekst');
    req.flush({ id: 7, naam: 'X', beschrijving: 'Nieuwe tekst', boeken: [] });
  });

  it('givenIdEnZichtbaar_whenUpdateZichtbaar_thenPutNaarIdZichtbaar', () => {
    service.updateZichtbaar(7, true).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/leeslijsten/7/zichtbaar'));
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toBe(true);
    req.flush({ id: 7, naam: 'X', zichtbaar: true, boeken: [] });
  });

  it('givenIdEnKoppelingData_whenUpdateKoppeling_thenPutNaarIdKoppelingMetBody', () => {
    service.updateKoppeling(7, { klas: null, leerlingId: 42 }).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/leeslijsten/7/koppeling'));
    expect(req.request.method).toBe('PUT');
    expect((req.request.body as any).leerlingId).toBe(42);
    req.flush({ id: 7, naam: 'X', leerlingId: 42, boeken: [] });
  });

  it('givenLeeslijstId_whenDeleteLeeslijst_thenDeleteNaarApiLeeslijstenId', () => {
    service.deleteLeeslijst(9).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/leeslijsten/9'));
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
