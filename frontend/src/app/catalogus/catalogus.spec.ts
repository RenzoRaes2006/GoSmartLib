import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
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
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: BoekService, useValue: boekSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Catalogus);
    component = fixture.componentInstance;
    fixture.detectChanges();

    // TagService laadt 3 keer dropdowns bij init — flush ze
    const http = TestBed.inject(HttpTestingController);
    http.match((r) => r.url.includes('/api/tags')).forEach((r) => r.flush([]));
  });

  afterEach(() => sessionStorage.clear());

  it('givenCatalogus_whenAangemaakt_thenComponentBestaat', () => {
    expect(component).toBeTruthy();
  });

  it('givenBoekService_whenInit_thenGetBoekenGefilterdAangeropenEnBoekenlijstGezet', () => {
    expect(boekSpy.getBoekenGefilterd).toHaveBeenCalled();
    expect(component.boekenlijst().length).toBe(1);
    expect(component.boekenlijst()[0].titel).toBe('Test Boek');
  });

  it('givenFilters_whenUpdateFilters_thenHuidigePaginaNulEnServiceOpnieuwAangeroepen', () => {
    component.huidigePagina.set(3);
    boekSpy.getBoekenGefilterd.calls.reset();

    component.updateFilters();

    expect(component.huidigePagina()).toBe(0);
    expect(boekSpy.getBoekenGefilterd).toHaveBeenCalled();
  });
});
