import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { Leerkrachtdashboard } from './leerkrachtdashboard';
import { LeerlingService } from '../services/Leerling.service';
import { BoekService } from '../services/boek.service';
import { StatistiekService } from '../services/statistiek.service';

describe('Leerkrachtdashboard', () => {
  let component: Leerkrachtdashboard;
  let fixture: ComponentFixture<Leerkrachtdashboard>;
  let leerlingSpy: jasmine.SpyObj<LeerlingService>;
  let boekSpy: jasmine.SpyObj<BoekService>;
  let statSpy: jasmine.SpyObj<StatistiekService>;

  beforeEach(async () => {
    leerlingSpy = jasmine.createSpyObj('LeerlingService', ['getLeerlingen', 'getLeerlingStats']);
    leerlingSpy.getLeerlingen.and.returnValue(of({ content: [{ id: 1 }], totalPages: 2 } as any));
    leerlingSpy.getLeerlingStats.and.returnValue(of({ totaalGeleend: 5 } as any));

    boekSpy = jasmine.createSpyObj('BoekService', ['getActieveLeningen']);
    boekSpy.getActieveLeningen.and.returnValue(of([]));

    statSpy = jasmine.createSpyObj('StatistiekService', ['getLeerkrachtStatistieken']);
    statSpy.getLeerkrachtStatistieken.and.returnValue(
      of({ boekenAfgelopenWeek: 10, topBoeken: ['A'], aantalActieveLeningen: 2, aantalTeLaat: 1, aantalLeerlingen: 25 } as any),
    );

    await TestBed.configureTestingModule({
      imports: [Leerkrachtdashboard],
      providers: [
        provideRouter([]),
        { provide: LeerlingService, useValue: leerlingSpy },
        { provide: BoekService, useValue: boekSpy },
        { provide: StatistiekService, useValue: statSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Leerkrachtdashboard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('givenLeerkrachtdashboard_whenAangemaakt_thenComponentBestaat', () => {
    expect(component).toBeTruthy();
  });

  it('givenServices_whenInit_thenLeerlingenLeningenEnStatistiekenOpgehaald', () => {
    expect(leerlingSpy.getLeerlingen).toHaveBeenCalled();
    expect(boekSpy.getActieveLeningen).toHaveBeenCalled();
    expect(statSpy.getLeerkrachtStatistieken).toHaveBeenCalled();
    expect(component.aantalLeerlingen()).toBe(25);
  });

  it('givenMeerderePaginas_whenVolgendePagina_thenHuidigePaginaVerhoogdEnLeerlingenHerladen', () => {
    component.totaalAantalPaginas.set(3);
    leerlingSpy.getLeerlingen.calls.reset();

    component.volgendePagina();

    expect(component.huidigePagina()).toBe(1);
    expect(leerlingSpy.getLeerlingen).toHaveBeenCalled();
  });

  it('givenStudent_whenOpenHistory_thenGeselecteerdeStudentGezettEnStatsOpgehaald', () => {
    component.openHistory({ id: 42, naam: 'Jan' });

    expect(component.selectedStudent()?.id).toBe(42);
    expect(leerlingSpy.getLeerlingStats).toHaveBeenCalledWith(42);
    expect(component.leerlingStats()?.totaalGeleend).toBe(5);
  });
});
