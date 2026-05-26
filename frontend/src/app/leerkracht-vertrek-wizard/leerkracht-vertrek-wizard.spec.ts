import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { LeerkrachtVertrekWizard } from './leerkracht-vertrek-wizard';
import {
  AdminGebruiker,
  AdminGebruikerService,
  LeerkrachtVertrekPreview,
} from '../services/AdminGebruikerService';

describe('LeerkrachtVertrekWizard', () => {
  let component: LeerkrachtVertrekWizard;
  let fixture: ComponentFixture<LeerkrachtVertrekWizard>;
  let serviceSpy: jasmine.SpyObj<AdminGebruikerService>;

  const leerkracht: AdminGebruiker = {
    id: 77,
    smartschoolId: 'lk-77',
    naam: 'Jan Janssens',
    klas: '',
    rol: 'LEERKRACHT',
    isBeheerder: false,
    actief: true,
  };

  function maakPreview(overrides: Partial<LeerkrachtVertrekPreview> = {}): LeerkrachtVertrekPreview {
    return {
      leeslijsten: [],
      kandidaten: [],
      aantalOpenLeningen: 0,
      ...overrides,
    };
  }

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('AdminGebruikerService', [
      'getLeerkrachtVertrekPreview',
      'markeerLeerkrachtVertrokken',
    ]);

    await TestBed.configureTestingModule({
      imports: [LeerkrachtVertrekWizard],
      providers: [{ provide: AdminGebruikerService, useValue: serviceSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(LeerkrachtVertrekWizard);
    component = fixture.componentInstance;
    component.leerkracht = leerkracht;
  });

  it('blokkeert doorgaan als er open uitleningen zijn', () => {
    serviceSpy.getLeerkrachtVertrekPreview.and.returnValue(
      of(maakPreview({ aantalOpenLeningen: 2 }))
    );
    fixture.detectChanges();

    expect(component.geblokkeerd()).toBeTrue();
    component.naarStap2();
    expect(component.stap()).toBe(1);
  });

  it('skipt stap 2 als de leerkracht geen actieve leeslijsten heeft', () => {
    serviceSpy.getLeerkrachtVertrekPreview.and.returnValue(of(maakPreview()));
    fixture.detectChanges();

    component.naarStap2();

    expect(component.stap()).toBe(3);
  });

  it('past bulk-keuze toe op alle leeslijsten', () => {
    serviceSpy.getLeerkrachtVertrekPreview.and.returnValue(
      of(maakPreview({
        leeslijsten: [
          { id: 1, naam: 'L1', klas: '1A', leerkrachtId: 77, leerkrachtNaam: null, leerlingIds: [], leerlingNamen: [], beschrijving: null, zichtbaar: false, gearchiveerd: false, boeken: [] },
          { id: 2, naam: 'L2', klas: null, leerkrachtId: 77, leerkrachtNaam: null, leerlingIds: [3], leerlingNamen: ['x'], beschrijving: null, zichtbaar: false, gearchiveerd: false, boeken: [] },
        ],
        kandidaten: [
          { id: 88, smartschoolId: 'lk-88', voorNaam: 'Piet', achternaam: 'P' },
        ],
      }))
    );
    fixture.detectChanges();

    component.bulkLeerkrachtId.set(88);
    component.pasBulkToe();

    expect(component.keuzes()).toEqual({ 1: 88, 2: 88 });
    expect(component.aantalOvergedragen()).toBe(2);
    expect(component.aantalGearchiveerd()).toBe(0);
  });

  it('stuurt overdrachten-map mee bij bevestigen', () => {
    serviceSpy.getLeerkrachtVertrekPreview.and.returnValue(
      of(maakPreview({
        leeslijsten: [
          { id: 1, naam: 'L1', klas: '1A', leerkrachtId: 77, leerkrachtNaam: null, leerlingIds: [], leerlingNamen: [], beschrijving: null, zichtbaar: false, gearchiveerd: false, boeken: [] },
          { id: 2, naam: 'L2', klas: '1B', leerkrachtId: 77, leerkrachtNaam: null, leerlingIds: [], leerlingNamen: [], beschrijving: null, zichtbaar: false, gearchiveerd: false, boeken: [] },
        ],
        kandidaten: [{ id: 88, smartschoolId: 'lk-88', voorNaam: null, achternaam: null }],
      }))
    );
    serviceSpy.markeerLeerkrachtVertrokken.and.returnValue(of({ ...leerkracht, actief: false }));
    fixture.detectChanges();

    component.zetKeuzeVoorLijst(1, '88');
    component.bevestig();

    expect(serviceSpy.markeerLeerkrachtVertrokken).toHaveBeenCalledWith(77, { 1: 88 });
    expect(component.stap()).toBe('klaar');
  });

  it('toont fout als preview-call faalt', () => {
    serviceSpy.getLeerkrachtVertrekPreview.and.returnValue(
      throwError(() => ({ error: 'Server down' }))
    );
    fixture.detectChanges();

    expect(component.fout()).toBe('Server down');
  });
});
