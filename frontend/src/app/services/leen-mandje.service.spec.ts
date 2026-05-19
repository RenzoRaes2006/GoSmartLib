import { TestBed } from '@angular/core/testing';

import { LeenMandjeService } from './leen-mandje.service';
import { Boek } from '../models/Boek';

describe('LeenMandjeService', () => {
  let service: LeenMandjeService;

  const boek = (id: number, aantal = 1): Boek =>
    ({ id, titel: 'B' + id, auteur: 'A', uitgever: '', tags: [], pagina: 0, uitgaveJaar: '', beschrijving: '', afbeelding: '', isbn: '', aantalExemplaren: aantal, locatie: '' }) as unknown as Boek;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LeenMandjeService);
  });

  it('should be created met leeg mandje', () => {
    expect(service).toBeTruthy();
    expect(service.getMandje().length).toBe(0);
  });

  it('voegBoekToe weigert een vierde boek', () => {
    service.voegBoekToe(boek(1));
    service.voegBoekToe(boek(2));
    service.voegBoekToe(boek(3));

    const result = service.voegBoekToe(boek(4));

    expect(result).toContain('maximaal 3');
    expect(service.getMandje().length).toBe(3);
  });

  it('voegBoekToe weigert dubbel boek', () => {
    service.voegBoekToe(boek(1));

    const result = service.voegBoekToe(boek(1));

    expect(result).toContain('al in je reserveringen');
  });

  it('voegBoekToe weigert boek zonder voorraad', () => {
    const result = service.voegBoekToe(boek(99, 0));

    expect(result).toContain('niet beschikbaar');
    expect(service.getMandje().length).toBe(0);
  });

  it('verwijderBoek haalt het juiste boek weg', () => {
    service.voegBoekToe(boek(1));
    service.voegBoekToe(boek(2));

    service.verwijderBoek(1);

    expect(service.getMandje().map((b) => b.id)).toEqual([2]);
  });

  it('maakMandjeLeeg leegt het mandje', () => {
    service.voegBoekToe(boek(1));

    service.maakMandjeLeeg();

    expect(service.getMandje()).toEqual([]);
  });
});
