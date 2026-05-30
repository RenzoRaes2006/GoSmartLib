import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { BehaviorSubject, of } from 'rxjs';

import { ReserverenComponent } from './reserveren';
import { LeenMandjeService } from '../services/leen-mandje.service';
import { BoekService } from '../services/boek.service';
import { InstellingService } from '../services/instelling-service';
import { Boek } from '../models/Boek';

describe('ReserverenComponent', () => {
  let component: ReserverenComponent;
  let fixture: ComponentFixture<ReserverenComponent>;
  let mandjeStub: { mandje$: BehaviorSubject<Boek[]>; verwijderBoek: jasmine.Spy; maakMandjeLeeg: jasmine.Spy };
  let boekSpy: jasmine.SpyObj<BoekService>;
  let instellingSpy: jasmine.SpyObj<InstellingService>;

  beforeEach(async () => {
    mandjeStub = {
      mandje$: new BehaviorSubject<Boek[]>([{ id: 1, titel: 'Test' } as Boek]),
      verwijderBoek: jasmine.createSpy('verwijderBoek'),
      maakMandjeLeeg: jasmine.createSpy('maakMandjeLeeg'),
    };

    boekSpy = jasmine.createSpyObj('BoekService', ['getActieveLeningen', 'leenBoek']);
    boekSpy.getActieveLeningen.and.returnValue(of([{ id: 99 }]));
    boekSpy.leenBoek.and.returnValue(of('OK'));

    instellingSpy = jasmine.createSpyObj('InstellingService', ['getLeenLimiet']);
    instellingSpy.getLeenLimiet.and.returnValue(of(3));

    await TestBed.configureTestingModule({
      imports: [ReserverenComponent],
      providers: [
        provideRouter([]),
        { provide: LeenMandjeService, useValue: mandjeStub },
        { provide: BoekService, useValue: boekSpy },
        { provide: InstellingService, useValue: instellingSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ReserverenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('givenReserveren_whenAangemaakt_thenComponentBestaat', () => {
    expect(component).toBeTruthy();
  });

  it('givenMandjeEnServices_whenInit_thenBoekenLeningenEnLimietGeladen', () => {
    expect(component.boekenInMandje.length).toBe(1);
    expect(component.actieveLeningen()).toBe(1);
    expect(component.maxLeenLimiet()).toBe(3);
  });

  it('givenBoekId_whenVerwijderBoek_thenMandjeServiceAangeroepen', () => {
    component.verwijderBoek(1);

    expect(mandjeStub.verwijderBoek).toHaveBeenCalledWith(1);
  });

  it('givenLeegMandje_whenBevestigReservering_thenMeldingZonderServiceCall', () => {
    component.boekenInMandje = [];
    boekSpy.leenBoek.calls.reset();

    component.bevestigReservering();

    expect(boekSpy.leenBoek).not.toHaveBeenCalled();
    expect(component.statusMelding).toContain('leeg');
  });

  it('givenBoekInMandje_whenBevestigReservering_thenLeenBoekAangeropenEnMandjeLeeggemaakt', () => {
    component.bevestigReservering();

    expect(boekSpy.leenBoek).toHaveBeenCalledWith(1, 1);
    expect(mandjeStub.maakMandjeLeeg).toHaveBeenCalled();
    expect(component.isSucces).toBeTrue();
  });
});
