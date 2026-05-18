import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { KlassLeeslijstBeheerComponent } from './klass-leeslijst-beheer';
import { KlassLeeslijstService } from '../services/klassLeeslijstService';
import { BoekService } from '../services/boek.service';
import { LeerlingService } from '../services/Leerling.service';

describe('KlassLeeslijstBeheerComponent', () => {
  let component: KlassLeeslijstBeheerComponent;
  let fixture: ComponentFixture<KlassLeeslijstBeheerComponent>;
  let leeslijstSpy: jasmine.SpyObj<KlassLeeslijstService>;
  let boekSpy: jasmine.SpyObj<BoekService>;
  let leerlingSpy: jasmine.SpyObj<LeerlingService>;
  let router: Router;

  beforeEach(async () => {
    leeslijstSpy = jasmine.createSpyObj('KlassLeeslijstService', [
      'getLeeslijstDetails',
      'addBoekToLeeslijst',
      'verwijderBoekUitLeeslijst',
      'updateLeeslijstNaam',
      'updateBeschrijving',
      'updateZichtbaar',
      'updateKoppeling',
      'deleteLeeslijst',
    ]);
    leeslijstSpy.getLeeslijstDetails.and.returnValue(
      of({ id: 5, naam: 'Top', boeken: [{ id: 1 }], zichtbaar: false } as any),
    );
    leeslijstSpy.addBoekToLeeslijst.and.returnValue(
      of({ id: 5, naam: 'Top', boeken: [{ id: 1 }, { id: 2 }], zichtbaar: false } as any),
    );
    leeslijstSpy.updateBeschrijving.and.returnValue(
      of({ id: 5, naam: 'Top', beschrijving: 'B', boeken: [{ id: 1 }], zichtbaar: false } as any),
    );
    leeslijstSpy.updateZichtbaar.and.returnValue(
      of({ id: 5, naam: 'Top', boeken: [{ id: 1 }], zichtbaar: true } as any),
    );
    leeslijstSpy.deleteLeeslijst.and.returnValue(of(undefined));

    boekSpy = jasmine.createSpyObj('BoekService', ['getBoekenGefilterd']);
    boekSpy.getBoekenGefilterd.and.returnValue(of({ content: [] } as any));

    leerlingSpy = jasmine.createSpyObj('LeerlingService', ['getKlassen', 'getLeerlingen']);
    leerlingSpy.getKlassen.and.returnValue(of(['5INF1']));
    leerlingSpy.getLeerlingen.and.returnValue(of({ content: [{ id: 42, smartschoolId: 'leerling42' }] } as any));

    leeslijstSpy.updateKoppeling.and.returnValue(
      of({ id: 5, naam: 'Top', klas: null, leerlingId: 42, leerlingNaam: 'leerling42', boeken: [{ id: 1 }], zichtbaar: false } as any),
    );

    await TestBed.configureTestingModule({
      imports: [KlassLeeslijstBeheerComponent],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id: '5' }) } },
        },
        { provide: KlassLeeslijstService, useValue: leeslijstSpy },
        { provide: BoekService, useValue: boekSpy },
        { provide: LeerlingService, useValue: leerlingSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(KlassLeeslijstBeheerComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('moet aangemaakt kunnen worden', () => {
    expect(component).toBeTruthy();
  });

  it('laadt lijst en zoekresultaten bij init', () => {
    expect(leeslijstSpy.getLeeslijstDetails).toHaveBeenCalledWith(5);
    expect(boekSpy.getBoekenGefilterd).toHaveBeenCalled();
    expect(component.lijst()?.id).toBe(5);
  });

  it('isAlToegevoegd geeft true voor boek dat al in de lijst staat', () => {
    expect(component.isAlToegevoegd(1)).toBeTrue();
    expect(component.isAlToegevoegd(99)).toBeFalse();
  });

  it('voegBoekToe roept addBoekToLeeslijst aan met juiste id', () => {
    component.voegBoekToe(2);

    expect(leeslijstSpy.addBoekToLeeslijst).toHaveBeenCalledWith(5, 2);
  });

  it('toggleZichtbaar roept service aan met geinverteerde waarde', () => {
    component.toggleZichtbaar();

    expect(leeslijstSpy.updateZichtbaar).toHaveBeenCalledWith(5, true);
  });

  it('slaBeschrijvingOp roept updateBeschrijving aan', () => {
    component.bewerkBeschrijving.set('Nieuwe tekst');

    component.slaBeschrijvingOp();

    expect(leeslijstSpy.updateBeschrijving).toHaveBeenCalledWith(5, 'Nieuwe tekst');
  });

  it('slaKoppelingOp wisselt klas-koppeling om naar leerling-koppeling', () => {
    component.koppelType.set('LEERLING');
    component.koppelLeerlingId.set(42);

    component.slaKoppelingOp();

    expect(leeslijstSpy.updateKoppeling).toHaveBeenCalledWith(5, { klas: null, leerlingId: 42 });
    expect(component.showKoppelingModal()).toBeFalse();
  });

  it('bevestigVerwijderen roept service aan en navigeert naar /leeslijsten', () => {
    const navSpy = spyOn(router, 'navigate').and.resolveTo(true);

    component.bevestigVerwijderen();

    expect(leeslijstSpy.deleteLeeslijst).toHaveBeenCalledWith(5);
    expect(navSpy).toHaveBeenCalledWith(['/leeslijsten']);
  });

  it('updateFilters zet paginaError bij negatieve minPagina', () => {
    component.minPagina.set(-1);

    component.updateFilters();

    expect(component.paginaError()).toContain('Minimum');
  });

  it('updateFilters zet paginaError bij negatieve maxPagina', () => {
    component.minPagina.set(0);
    component.maxPagina.set(-5);

    component.updateFilters();

    expect(component.paginaError()).toContain('Maximum');
  });

  it('updateFilters zet paginaError als min > max', () => {
    component.minPagina.set(200);
    component.maxPagina.set(100);

    component.updateFilters();

    expect(component.paginaError()).toContain('Min');
  });

  it('onMinPaginaSlider clampt waarde naar maxPagina', () => {
    component.maxPagina.set(100);
    const input = document.createElement('input');
    input.value = '300';

    component.onMinPaginaSlider({ target: input } as unknown as Event);

    expect(component.minPagina()).toBe(100);
    expect(input.value).toBe('100');
  });

  it('onMaxPaginaSlider clampt waarde naar minPagina', () => {
    component.minPagina.set(200);
    const input = document.createElement('input');
    input.value = '50';

    component.onMaxPaginaSlider({ target: input } as unknown as Event);

    expect(component.maxPagina()).toBe(200);
    expect(input.value).toBe('200');
  });
});
