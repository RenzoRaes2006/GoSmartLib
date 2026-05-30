import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { GebruikerService } from './GebruikerService';
import { Gebruiker } from '../models/Gebruiker.mode';
import { Boek } from '../models/Boek';

describe('GebruikerService', () => {
  let service: GebruikerService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [GebruikerService],
    });
    service = TestBed.inject(GebruikerService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('givenGebruikerService_whenAangemaakt_thenServiceBestaat', () => {
    expect(service).toBeTruthy();
  });

  it('givenGebruikerService_whenGetUsers_thenGetNaarApiGebruikers', () => {
    const mock: Gebruiker[] = [
      { id: 1, smartschoolId: 'a.b', rol: 'LEERLING' },
    ];
    service.getUsers().subscribe((result) => expect(result).toEqual(mock));

    const req = httpMock.expectOne('/api/gebruikers');
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('givenGebruikerId_whenGetLeeslijst_thenGetMetGebruikerIdInPath', () => {
    const mock: Boek[] = [];
    service.getLeeslijst(42).subscribe((result) => expect(result).toEqual(mock));

    const req = httpMock.expectOne('/api/gebruikers/42/leeslijst');
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });
});
