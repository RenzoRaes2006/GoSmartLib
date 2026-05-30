import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { AanvragenBeheer } from './aanvragen-beheer';
import { BoekService } from '../services/boek.service';
import { InstellingService } from '../services/instelling-service';

describe('AanvragenBeheer', () => {
  let component: AanvragenBeheer;
  let fixture: ComponentFixture<AanvragenBeheer>;
  let boekSpy: jasmine.SpyObj<BoekService>;
  let instellingSpy: jasmine.SpyObj<InstellingService>;

  beforeEach(async () => {
    boekSpy = jasmine.createSpyObj('BoekService', [
      'getAlleAanvragen',
      'getAlleVerlengingAanvragen',
      'getActieveLeningen',
      'keurAanvraagGoed',
      'weigerAanvraag',
      'keurVerlengingGoed',
      'weigerVerlenging',
    ]);
    boekSpy.getAlleAanvragen.and.returnValue(of([]));
    boekSpy.getAlleVerlengingAanvragen.and.returnValue(of([]));
    boekSpy.getActieveLeningen.and.returnValue(of([]));
    boekSpy.keurAanvraagGoed.and.returnValue(of('ok'));
    boekSpy.weigerAanvraag.and.returnValue(of('ok'));

    instellingSpy = jasmine.createSpyObj('InstellingService', ['getLeenLimiet']);
    instellingSpy.getLeenLimiet.and.returnValue(of(3));

    await TestBed.configureTestingModule({
      imports: [AanvragenBeheer],
      providers: [
        { provide: BoekService, useValue: boekSpy },
        { provide: InstellingService, useValue: instellingSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AanvragenBeheer);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('givenAanvragenBeheer_whenAangemaakt_thenComponentBestaat', () => {
    expect(component).toBeTruthy();
  });

  it('givenServices_whenInit_thenAanvragenVerlengingEnLimietOpgehaald', () => {
    expect(boekSpy.getAlleAanvragen).toHaveBeenCalled();
    expect(boekSpy.getAlleVerlengingAanvragen).toHaveBeenCalled();
    expect(instellingSpy.getLeenLimiet).toHaveBeenCalled();
    expect(component.maxLeenLimiet()).toBe(3);
  });

  it('givenAanvraagId_whenKeurGoed_thenBoekServiceKeurAanvraagGoedAangeroepen', () => {
    component.keurGoed(42);

    expect(boekSpy.keurAanvraagGoed).toHaveBeenCalledWith(42);
  });

  it('givenAanvraagId_whenWijsAf_thenBoekServiceWeigerAanvraagAangeroepen', () => {
    component.wijsAf(99);

    expect(boekSpy.weigerAanvraag).toHaveBeenCalledWith(99);
  });
});
