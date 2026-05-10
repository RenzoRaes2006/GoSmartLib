import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { Catalogus } from './catalogus';
import { BoekService } from '../services/boek.service';

describe('Catalogus', () => {
  let component: Catalogus;
  let fixture: ComponentFixture<Catalogus>;
  let boekSpy: jasmine.SpyObj<BoekService>;

  beforeEach(async () => {
    sessionStorage.clear();
    boekSpy = jasmine.createSpyObj('BoekService', ['getBoekenGefilterd']);
    boekSpy.getBoekenGefilterd.and.returnValue(
      of({ content: [{ id: 1, titel: 'Test Boek', auteur: 'A' }], totalElements: 1, totalPages: 1 } as any),
    );

    await TestBed.configureTestingModule({
      imports: [Catalogus],
      providers: [provideRouter([]), { provide: BoekService, useValue: boekSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(Catalogus);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => sessionStorage.clear());

  it('moet aangemaakt kunnen worden', () => {
    expect(component).toBeTruthy();
  });

  it('roept BoekService.getBoekenGefilterd aan bij init en zet boekenlijst', () => {
    expect(boekSpy.getBoekenGefilterd).toHaveBeenCalled();
    expect(component.boekenlijst().length).toBe(1);
    expect(component.boekenlijst()[0].titel).toBe('Test Boek');
  });

  it('updateFilters zet huidigePagina terug op 0 en roept service opnieuw aan', () => {
    component.huidigePagina.set(3);
    boekSpy.getBoekenGefilterd.calls.reset();

    component.updateFilters();

    expect(component.huidigePagina()).toBe(0);
    expect(boekSpy.getBoekenGefilterd).toHaveBeenCalled();
  });
});
