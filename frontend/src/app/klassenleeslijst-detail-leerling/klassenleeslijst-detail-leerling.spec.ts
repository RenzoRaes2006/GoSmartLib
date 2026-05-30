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

  it('givenKlassenleeslijstDetailLeerling_whenAangemaakt_thenComponentBestaat', () => {
    expect(component).toBeTruthy();
  });

  it('givenRouteMetId_whenInit_thenLeeslijstDetailsOpgehaald', () => {
    expect(serviceSpy.getLeeslijstDetails).toHaveBeenCalledWith(5);
  });

  it('givenServiceResponse_whenInit_thenLijstSignalGevuld', () => {
    expect(component.lijst()?.naam).toBe('Top 10');
  });
});
