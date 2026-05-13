import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { KlassLeeslijstBeheerComponent } from './klass-leeslijst-beheer';
import { KlassLeeslijstService } from '../services/klassLeeslijstService';
import { BoekService } from '../services/boek.service';

describe('KlassLeeslijstBeheerComponent', () => {
  let component: KlassLeeslijstBeheerComponent;
  let fixture: ComponentFixture<KlassLeeslijstBeheerComponent>;
  let leeslijstSpy: jasmine.SpyObj<KlassLeeslijstService>;
  let boekSpy: jasmine.SpyObj<BoekService>;
  let router: Router;

  beforeEach(async () => {
    leeslijstSpy = jasmine.createSpyObj('KlassLeeslijstService', [
      'getLeeslijstDetails',
      'addBoekToLeeslijst',
      'verwijderBoekUitLeeslijst',
      'updateLeeslijstNaam',
      'deleteLeeslijst',
    ]);
    leeslijstSpy.getLeeslijstDetails.and.returnValue(of({ id: 5, naam: 'Top', boeken: [{ id: 1 }] } as any));
    leeslijstSpy.addBoekToLeeslijst.and.returnValue(of({ id: 5, naam: 'Top', boeken: [{ id: 1 }, { id: 2 }] } as any));
    leeslijstSpy.deleteLeeslijst.and.returnValue(of(undefined));

    boekSpy = jasmine.createSpyObj('BoekService', ['getBoekenGefilterd']);
    boekSpy.getBoekenGefilterd.and.returnValue(of({ content: [] } as any));

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

  it('bevestigVerwijderen roept service aan en navigeert naar /leeslijsten', () => {
    const navSpy = spyOn(router, 'navigate').and.resolveTo(true);

    component.bevestigVerwijderen();

    expect(leeslijstSpy.deleteLeeslijst).toHaveBeenCalledWith(5);
    expect(navSpy).toHaveBeenCalledWith(['/leeslijsten']);
  });
});
