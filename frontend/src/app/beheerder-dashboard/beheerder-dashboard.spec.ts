import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { BeheerderDashboard } from './beheerder-dashboard';
import { BoekService } from '../services/boek.service';
import { InstellingService } from '../services/instelling-service';
import { StatistiekService } from '../services/statistiek.service';

describe('BeheerderDashboard', () => {
  let component: BeheerderDashboard;
  let fixture: ComponentFixture<BeheerderDashboard>;
  let boekSpy: jasmine.SpyObj<BoekService>;
  let instellingSpy: jasmine.SpyObj<InstellingService>;
  let statSpy: jasmine.SpyObj<StatistiekService>;

  beforeEach(async () => {
    boekSpy = jasmine.createSpyObj('BoekService', ['getAlleActieveLeningenSysteem']);
    boekSpy.getAlleActieveLeningenSysteem.and.returnValue(of([]));

    instellingSpy = jasmine.createSpyObj('InstellingService', [
      'getLeenLimiet',
      'updateLeenLimiet',
      'getInlevertermijn',
      'updateInlevertermijn',
    ]);
    instellingSpy.getLeenLimiet.and.returnValue(of(3));
    instellingSpy.updateLeenLimiet.and.returnValue(of('ok'));
    instellingSpy.getInlevertermijn.and.returnValue(of(14));
    instellingSpy.updateInlevertermijn.and.returnValue(of('ok'));

    statSpy = jasmine.createSpyObj('StatistiekService', ['getAdminStatistieken']);
    statSpy.getAdminStatistieken.and.returnValue(
      of({ percentageUitgeleend: 25, teLaatIngeleverd: 4, populairsteGenre: 'Fantasy', uitleningenDezeMaand: 12 } as any),
    );

    await TestBed.configureTestingModule({
      imports: [BeheerderDashboard],
      providers: [
        provideRouter([]),
        { provide: BoekService, useValue: boekSpy },
        { provide: InstellingService, useValue: instellingSpy },
        { provide: StatistiekService, useValue: statSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BeheerderDashboard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('givenBeheerderDashboard_whenAangemaakt_thenComponentBestaat', () => {
    expect(component).toBeTruthy();
  });

  it('givenServices_whenInit_thenLimietEnStatistiekenGeladen', () => {
    expect(instellingSpy.getLeenLimiet).toHaveBeenCalled();
    expect(statSpy.getAdminStatistieken).toHaveBeenCalled();
    expect(component.huidigeLimiet()).toBe(3);
    expect(component.populairsteGenre()).toBe('Fantasy');
  });

  it('givenGeldigeLimiet_whenOpslaanLimiet_thenNieuweWaardeDoorgestuurdEnSuccesToastGetoond', () => {
    component.opslaanLimiet('5');

    expect(instellingSpy.updateLeenLimiet).toHaveBeenCalledWith(5);
    expect(component.toastActief()).toBeTrue();
    expect(component.toastType()).toBe('succes');
    expect(component.huidigeLimiet()).toBe(5);
  });

  it('givenNegatiefGetal_whenOpslaanLimiet_thenWeigeringEnFoutToast', () => {
    instellingSpy.updateLeenLimiet.calls.reset();

    component.opslaanLimiet('-1');

    expect(instellingSpy.updateLeenLimiet).not.toHaveBeenCalled();
    expect(component.toastType()).toBe('fout');
  });
});
