import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { LeningenBeheerder } from './leningen-beheerder';
import { BoekService } from '../services/boek.service';

describe('LeningenBeheerder', () => {
  let component: LeningenBeheerder;
  let fixture: ComponentFixture<LeningenBeheerder>;
  let boekSpy: jasmine.SpyObj<BoekService>;

  beforeEach(async () => {
    boekSpy = jasmine.createSpyObj('BoekService', ['getAlleActieveLeningenSysteem']);

    const overTweeDagen = new Date();
    overTweeDagen.setDate(overTweeDagen.getDate() + 2);
    const overTwintigDagen = new Date();
    overTwintigDagen.setDate(overTwintigDagen.getDate() + 20);

    boekSpy.getAlleActieveLeningenSysteem.and.returnValue(
      of([
        { id: 1, inleverDatum: overTweeDagen.toISOString(), boek: { titel: 'Java' }, gebruiker: { voorNaam: 'Jan' } },
        { id: 2, inleverDatum: overTwintigDagen.toISOString(), boek: { titel: 'Verre datum' }, gebruiker: {} },
      ]),
    );

    await TestBed.configureTestingModule({
      imports: [LeningenBeheerder],
      providers: [{ provide: BoekService, useValue: boekSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(LeningenBeheerder);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('givenLeningenBeheerder_whenAangemaakt_thenComponentBestaat', () => {
    expect(component).toBeTruthy();
  });

  it('givenBoekService_whenInit_thenActieveLeningenOpgehaald', () => {
    expect(boekSpy.getAlleActieveLeningenSysteem).toHaveBeenCalled();
    expect(component.alleLeningen().length).toBe(2);
  });

  it('givenActieveLeningen_whenDringendeLeningen_thenAlleenLeningenBinnenZevenDagenGetoond', () => {
    const dringend = component.dringendeLeningen();
    expect(dringend.length).toBe(1);
    expect(dringend[0].id).toBe(1);
  });

  it('givenZoekterm_whenUpdateZoekterm_thenGefilterdOpBoektitelEnPaginatieGereset', () => {
    component.huidigePagina.set(3);

    component.updateZoekterm('java');

    expect(component.zoekterm()).toBe('java');
    expect(component.huidigePagina()).toBe(1);
    expect(component.gefilterdeLeningen().length).toBe(1);
  });

  it('givenLaatstePagina_whenVolgendePagina_thenPaginaBlijftBinnenTotaal', () => {
    component.huidigePagina.set(component.totaalAantalPaginas());

    component.volgendePagina();

    expect(component.huidigePagina()).toBe(component.totaalAantalPaginas());
  });
});
