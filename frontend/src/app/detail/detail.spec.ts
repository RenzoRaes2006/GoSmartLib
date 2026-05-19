import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { Detail } from './detail';
import { BoekService } from '../services/boek.service';
import { ReviewService } from '../services/review-service';
import { RapportageService } from '../services/rapportage-service';
import { LeeslijstService } from '../services/leeslijstService';
import { LeenMandjeService } from '../services/leen-mandje.service';

describe('Detail', () => {
  let component: Detail;
  let fixture: ComponentFixture<Detail>;
  let boekSpy: jasmine.SpyObj<BoekService>;
  let reviewSpy: jasmine.SpyObj<ReviewService>;
  let rapportSpy: jasmine.SpyObj<RapportageService>;
  let leeslijstSpy: jasmine.SpyObj<LeeslijstService>;

  beforeEach(async () => {
    boekSpy = jasmine.createSpyObj('BoekService', ['getBoekOpId', 'getBoekenGefilterd', 'checkLeenStatus']);
    boekSpy.getBoekOpId.and.returnValue(of({ id: 1, titel: 'Test', auteur: 'A', tags: [] } as any));
    boekSpy.getBoekenGefilterd.and.returnValue(of({ content: [] } as any));
    boekSpy.checkLeenStatus.and.returnValue(of({ magLenen: true, reden: 'Beschikbaar' } as any));

    reviewSpy = jasmine.createSpyObj('ReviewService', ['getBeoordelingenOpBoekId', 'voegBeoordelingToe']);
    reviewSpy.getBeoordelingenOpBoekId.and.returnValue(of([]));

    rapportSpy = jasmine.createSpyObj('RapportageService', ['rapporteerBeoordeling']);

    leeslijstSpy = jasmine.createSpyObj('LeeslijstService', ['getMappenVanGebruiker', 'voegBoekToeAanMap']);
    leeslijstSpy.getMappenVanGebruiker.and.returnValue(of([{ naam: 'School' }]));

    await TestBed.configureTestingModule({
      imports: [Detail],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { paramMap: of(convertToParamMap({ id: '1' })) } },
        { provide: BoekService, useValue: boekSpy },
        { provide: ReviewService, useValue: reviewSpy },
        { provide: RapportageService, useValue: rapportSpy },
        { provide: LeeslijstService, useValue: leeslijstSpy },
        { provide: LeenMandjeService, useValue: { mandje$: { subscribe: () => ({}) }, voegBoekToe: () => 'Succes' } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Detail);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('moet aangemaakt kunnen worden', () => {
    expect(component).toBeTruthy();
  });

  it('haalt boek op via BoekService.getBoekOpId met id uit de route', () => {
    expect(boekSpy.getBoekOpId).toHaveBeenCalledWith(1);
    expect(component.boek?.titel).toBe('Test');
  });

  it('filtert favorieten uit de mappenlijst', () => {
    expect(leeslijstSpy.getMappenVanGebruiker).toHaveBeenCalledWith(1);
    expect(component.mappen.every((m) => m.naam.toLowerCase() !== 'favorieten')).toBeTrue();
  });
});
