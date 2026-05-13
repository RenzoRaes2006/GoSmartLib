import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { AdminDashboard } from './admin-dashboard';
import { BoekService } from '../services/boek.service';
import { InstellingService } from '../services/instelling-service';
import { StatistiekService } from '../services/statistiek.service';

describe('AdminDashboard', () => {
  let component: AdminDashboard;
  let fixture: ComponentFixture<AdminDashboard>;
  let boekSpy: jasmine.SpyObj<BoekService>;
  let instellingSpy: jasmine.SpyObj<InstellingService>;
  let statSpy: jasmine.SpyObj<StatistiekService>;

  beforeEach(async () => {
    boekSpy = jasmine.createSpyObj('BoekService', ['getAlleActieveLeningenSysteem']);
    boekSpy.getAlleActieveLeningenSysteem.and.returnValue(of([]));

    instellingSpy = jasmine.createSpyObj('InstellingService', ['getLeenLimiet', 'updateLeenLimiet']);
    instellingSpy.getLeenLimiet.and.returnValue(of(3));
    instellingSpy.updateLeenLimiet.and.returnValue(of('ok'));

    statSpy = jasmine.createSpyObj('StatistiekService', ['getAdminStatistieken']);
    statSpy.getAdminStatistieken.and.returnValue(
      of({ percentageUitgeleend: 25, teLaatIngeleverd: 4, populairsteGenre: 'Fantasy', uitleningenDezeMaand: 12 } as any),
    );

    await TestBed.configureTestingModule({
      imports: [AdminDashboard],
      providers: [
        provideRouter([]),
        { provide: BoekService, useValue: boekSpy },
        { provide: InstellingService, useValue: instellingSpy },
        { provide: StatistiekService, useValue: statSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminDashboard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('moet aangemaakt kunnen worden', () => {
    expect(component).toBeTruthy();
  });

  it('laadt limiet en statistieken bij init', () => {
    expect(instellingSpy.getLeenLimiet).toHaveBeenCalled();
    expect(statSpy.getAdminStatistieken).toHaveBeenCalled();
    expect(component.huidigeLimiet()).toBe(3);
    expect(component.populairsteGenre()).toBe('Fantasy');
  });

  it('opslaanLimiet stuurt nieuwe waarde door en toont succes-toast', () => {
    component.opslaanLimiet('5');

    expect(instellingSpy.updateLeenLimiet).toHaveBeenCalledWith(5);
    expect(component.toastActief()).toBeTrue();
    expect(component.toastType()).toBe('succes');
    expect(component.huidigeLimiet()).toBe(5);
  });

  it('opslaanLimiet weigert negatieve waarden', () => {
    instellingSpy.updateLeenLimiet.calls.reset();

    component.opslaanLimiet('-1');

    expect(instellingSpy.updateLeenLimiet).not.toHaveBeenCalled();
    expect(component.toastType()).toBe('fout');
  });
});
