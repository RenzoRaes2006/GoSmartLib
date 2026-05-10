import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { KlassenleeslijstDetailLeerlingComponent } from './klassenleeslijst-detail-leerling';
import { KlassLeeslijstService } from '../services/klassLeeslijstService';

describe('KlassenleeslijstDetailLeerlingComponent', () => {
  let component: KlassenleeslijstDetailLeerlingComponent;
  let fixture: ComponentFixture<KlassenleeslijstDetailLeerlingComponent>;
  let serviceSpy: jasmine.SpyObj<KlassLeeslijstService>;

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('KlassLeeslijstService', ['getLeeslijstDetails']);
    serviceSpy.getLeeslijstDetails.and.returnValue(of({ id: 5, naam: 'Top 10', boeken: [] } as any));

    await TestBed.configureTestingModule({
      imports: [KlassenleeslijstDetailLeerlingComponent],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id: '5' }) } },
        },
        { provide: KlassLeeslijstService, useValue: serviceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(KlassenleeslijstDetailLeerlingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('moet aangemaakt kunnen worden', () => {
    expect(component).toBeTruthy();
  });

  it('haalt leeslijst-details op met id uit de route', () => {
    expect(serviceSpy.getLeeslijstDetails).toHaveBeenCalledWith(5);
  });

  it('zet de lijst-signal op het response van de service', () => {
    expect(component.lijst()?.naam).toBe('Top 10');
  });
});
