import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { HomePage } from './home-page';
import { BoekService } from '../services/boek.service';
import { LeeslijstService } from '../services/leeslijstService';
import { StatistiekService } from '../services/statistiek.service';
import { AuthService } from '../services/AuthService';

describe('HomePage', () => {
  let component: HomePage;
  let fixture: ComponentFixture<HomePage>;
  let boekSpy: jasmine.SpyObj<BoekService>;
  let leeslijstSpy: jasmine.SpyObj<LeeslijstService>;
  let statSpy: jasmine.SpyObj<StatistiekService>;
  let authSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    boekSpy = jasmine.createSpyObj('BoekService', ['getPopulaireBoeken', 'getActieveLeningen', 'verlengLening']);
    boekSpy.getPopulaireBoeken.and.returnValue(of([{ id: 1, titel: 'Boek1' }] as any));
    boekSpy.getActieveLeningen.and.returnValue(of([]));
    boekSpy.verlengLening.and.returnValue(of('verlengd'));

    leeslijstSpy = jasmine.createSpyObj('LeeslijstService', ['getMappenVanGebruiker']);
    leeslijstSpy.getMappenVanGebruiker.and.returnValue(of([{ naam: 'Favorieten' }]));

    statSpy = jasmine.createSpyObj('StatistiekService', ['getLeerlingStatistieken', 'updateLeesdoel']);
    statSpy.getLeerlingStatistieken.and.returnValue(
      of({ gelezenBoeken: 5, leesDoel: 10, aantalActieveLeningen: 2, aantalTeLaat: 0 } as any),
    );
    statSpy.updateLeesdoel.and.callFake((_id: string, leesdoel: number) =>
      of({ gelezenBoeken: 5, leesDoel: leesdoel, aantalActieveLeningen: 2, aantalTeLaat: 0 } as any),
    );

    authSpy = jasmine.createSpyObj('AuthService', ['currentUser', 'logout', 'setUserFromToken']);
    authSpy.currentUser.and.returnValue({ id: '1', name: 'Test', role: 'student', klas: '1A' });

    await TestBed.configureTestingModule({
      imports: [HomePage],
      providers: [
        provideRouter([]),
        { provide: BoekService, useValue: boekSpy },
        { provide: LeeslijstService, useValue: leeslijstSpy },
        { provide: StatistiekService, useValue: statSpy },
        { provide: AuthService, useValue: authSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(HomePage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('givenHomePage_whenAangemaakt_thenComponentBestaat', () => {
    expect(component).toBeTruthy();
  });

  it('givenServices_whenInit_thenPopulaireBoekendLeeslijstenEnStatistiekenGeladen', () => {
    expect(boekSpy.getPopulaireBoeken).toHaveBeenCalled();
    expect(leeslijstSpy.getMappenVanGebruiker).toHaveBeenCalledWith(1);
    expect(statSpy.getLeerlingStatistieken).toHaveBeenCalledWith('1');
    expect(component.gelezenBoeken()).toBe(5);
  });

  it('givenGelezenEnLeesDoel_whenProgressPercentage_thenPercentageBerekend', () => {
    component.gelezenBoeken.set(5);
    component.leesDoel.set(10);

    expect(component.progressPercentage).toBe(50);
  });

  it('givenLeesdoelBoven10_whenSlaLeesdoelOp_thenWaardeGeclampedOpMaximaal10', () => {
    component.nieuwLeesdoel.set(50);
    component.openEditModal();

    component.slaLeesdoelOp();

    expect(component.leesDoel()).toBe(10);
    expect(component.isEditModalOpen()).toBeFalse();
  });

  it('givenLeningId_whenBevestigVerlengen_thenBoekServiceVerlengLeningAangeroepen', () => {
    component.teVerlengenLeningId.set(7);

    component.bevestigVerlengen();

    expect(boekSpy.verlengLening).toHaveBeenCalledWith(7);
  });
});
