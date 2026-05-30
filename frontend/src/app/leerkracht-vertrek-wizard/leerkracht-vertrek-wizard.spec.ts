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

  it('givenOpenUitleningen_whenNaarStap2_thenGeblokkeerd', () => {
    serviceSpy.getLeerkrachtVertrekPreview.and.returnValue(
      of(maakPreview({ aantalOpenLeningen: 2 }))
    );
    fixture.detectChanges();

    expect(component.geblokkeerd()).toBeTrue();
    component.naarStap2();
    expect(component.stap()).toBe(1);
  });

  it('givenGeenActieveLeeslijsten_whenNaarStap2_thenStap2Overgeslagen', () => {
    serviceSpy.getLeerkrachtVertrekPreview.and.returnValue(of(maakPreview()));
    fixture.detectChanges();

    component.naarStap2();

    expect(component.stap()).toBe(3);
  });

  it('givenBulkLeerkrachtId_whenPasBulkToe_thenKeuzeToegepastOpAlleLeeslijsten', () => {
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

  it('givenKeuzes_whenBevestig_thenOverdrachtenMapMeegestuurd', () => {
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

  it('givenPreviewCallFaalt_whenInit_thenFoutGetoond', () => {
    serviceSpy.getLeerkrachtVertrekPreview.and.returnValue(
      throwError(() => ({ error: 'Server down' }))
    );
    fixture.detectChanges();

    expect(component.fout()).toBe('Server down');
  });
});
