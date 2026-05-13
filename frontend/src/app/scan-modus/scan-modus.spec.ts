import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { ScanModus } from './scan-modus';
import { ScanService } from '../services/scan.service';

describe('ScanModus', () => {
  let component: ScanModus;
  let fixture: ComponentFixture<ScanModus>;
  let scanSpy: jasmine.SpyObj<ScanService>;

  beforeEach(async () => {
    scanSpy = jasmine.createSpyObj('ScanService', ['zoekLeerlingen', 'zoekBoekOpIsbn', 'verwerkBatch']);
    scanSpy.zoekLeerlingen.and.returnValue(of({ content: [{ gebruikerID: 1, voorNaam: 'Jan', achterNaam: 'P' } as any], totalPages: 1 } as any));
    scanSpy.zoekBoekOpIsbn.and.returnValue(of({ titel: 'Test Boek' } as any));
    scanSpy.verwerkBatch.and.returnValue(of({ actie: 'UITLENING', resultaten: [] } as any));

    await TestBed.configureTestingModule({
      imports: [ScanModus],
      providers: [{ provide: ScanService, useValue: scanSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(ScanModus);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('moet aangemaakt kunnen worden', () => {
    expect(component).toBeTruthy();
  });

  it('haalt leerlingen op via scanService bij init', () => {
    expect(scanSpy.zoekLeerlingen).toHaveBeenCalledWith('', 0, 4);
    expect(component.leerlingen().length).toBe(1);
  });

  it('kiesLeerling zet de geselecteerde leerling en stap naar MODUS', () => {
    const leerling = { gebruikerID: 7, voorNaam: 'Test', achterNaam: 'X' } as any;

    component.kiesLeerling(leerling);

    expect(component.geselecteerdeLeerling()?.gebruikerID).toBe(7);
    expect(component.stap()).toBe('MODUS');
  });

  it('scanIsbn weigert ongeldig ISBN met een foutmelding', () => {
    component.isbnInput.set('123');

    component.scanIsbn();

    expect(component.scanFout()).toContain('Ongeldig');
    expect(scanSpy.zoekBoekOpIsbn).not.toHaveBeenCalled();
  });

  it('scanIsbn met geldig ISBN voegt boek toe aan mandje', () => {
    component.isbnInput.set('9780132350884');

    component.scanIsbn();

    expect(scanSpy.zoekBoekOpIsbn).toHaveBeenCalledWith('9780132350884');
    expect(component.mandje().length).toBe(1);
    expect(component.mandje()[0].titel).toBe('Test Boek');
  });

  it('verwijderUitMandje haalt boek met juiste isbn weg', () => {
    component.mandje.set([
      { isbn: 'A', titel: 'X' } as any,
      { isbn: 'B', titel: 'Y' } as any,
    ]);

    component.verwijderUitMandje('A');

    expect(component.mandje().map((m) => m.isbn)).toEqual(['B']);
  });
});
