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

  it('moet aangemaakt kunnen worden', () => {
    expect(component).toBeTruthy();
  });

  it('haalt aanvragen, verlengingen en limiet op bij init', () => {
    expect(boekSpy.getAlleAanvragen).toHaveBeenCalled();
    expect(boekSpy.getAlleVerlengingAanvragen).toHaveBeenCalled();
    expect(instellingSpy.getLeenLimiet).toHaveBeenCalled();
    expect(component.maxLeenLimiet()).toBe(3);
  });

  it('keurGoed roept BoekService.keurAanvraagGoed aan met juiste id', () => {
    component.keurGoed(42);

    expect(boekSpy.keurAanvraagGoed).toHaveBeenCalledWith(42);
  });

  it('wijsAf roept BoekService.weigerAanvraag aan met juiste id', () => {
    component.wijsAf(99);

    expect(boekSpy.weigerAanvraag).toHaveBeenCalledWith(99);
  });
});
