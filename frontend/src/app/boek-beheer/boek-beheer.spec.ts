import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { of } from 'rxjs';

import { BoekBeheer } from './boek-beheer';
import { BoekService } from '../services/boek.service';
import { GoogleBookService } from '../services/googleBook.service';

describe('BoekBeheer', () => {
  let component: BoekBeheer;
  let fixture: ComponentFixture<BoekBeheer>;
  let boekSpy: jasmine.SpyObj<BoekService>;
  let googleSpy: jasmine.SpyObj<GoogleBookService>;

  beforeEach(async () => {
    boekSpy = jasmine.createSpyObj('BoekService', ['getBoekenPaged', 'toevoegen', 'wijzigen', 'verwijderen']);
    boekSpy.getBoekenPaged.and.returnValue(of({ content: [], number: 0, totalPages: 0 } as any));

    googleSpy = jasmine.createSpyObj('GoogleBookService', ['zoekOpIsbn']);

    await TestBed.configureTestingModule({
      imports: [BoekBeheer],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: { queryParams: of({}) } },
        { provide: BoekService, useValue: boekSpy },
        { provide: GoogleBookService, useValue: googleSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BoekBeheer);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const http = TestBed.inject(HttpTestingController);
    http.match((r) => r.url.includes('/api/tags')).forEach((r) => r.flush([]));
  });

  it('givenBoekBeheer_whenAangemaakt_thenComponentBestaat', () => {
    expect(component).toBeTruthy();
  });

  it('givenWijzigmodus_whenInit_thenBoekServiceGetBoekenPagedAangeroepen', () => {
    expect(boekSpy.getBoekenPaged).toHaveBeenCalled();
  });

  it('givenToevoegenModus_whenSetMode_thenBoekenoverzichtLeeggemaakt', () => {
    component.boeknOverzicht.set([{ id: 1 } as any]);

    component.setMode('toevoegen');

    expect(component.mode()).toBe('toevoegen');
    expect(component.boeknOverzicht().length).toBe(0);
  });
});
