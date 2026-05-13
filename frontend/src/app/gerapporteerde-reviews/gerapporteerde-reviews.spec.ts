import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { GerapporteerdeReviews } from './gerapporteerde-reviews';
import { ReviewService } from '../services/review-service';
import { RapportageService } from '../services/rapportage-service';

describe('GerapporteerdeReviews', () => {
  let component: GerapporteerdeReviews;
  let fixture: ComponentFixture<GerapporteerdeReviews>;
  let reviewSpy: jasmine.SpyObj<ReviewService>;
  let rapportSpy: jasmine.SpyObj<RapportageService>;

  beforeEach(async () => {
    reviewSpy = jasmine.createSpyObj('ReviewService', ['verwijderRapporteerdeBeoordeling']);
    reviewSpy.verwijderRapporteerdeBeoordeling.and.returnValue(of({}));

    rapportSpy = jasmine.createSpyObj('RapportageService', ['getRapportages', 'negeerRapportage']);
    rapportSpy.getRapportages.and.returnValue(
      of([
        { id: 1, reviewId: 10, boekTitel: 'X' },
        { id: 2, reviewId: 11, boekTitel: 'Y' },
      ] as any),
    );
    rapportSpy.negeerRapportage.and.returnValue(of({}));

    await TestBed.configureTestingModule({
      imports: [GerapporteerdeReviews],
      providers: [
        { provide: ReviewService, useValue: reviewSpy },
        { provide: RapportageService, useValue: rapportSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(GerapporteerdeReviews);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('moet aangemaakt kunnen worden', () => {
    expect(component).toBeTruthy();
  });

  it('haalt rapportages op via service bij init', () => {
    expect(rapportSpy.getRapportages).toHaveBeenCalled();
    expect(component.rapportages.length).toBe(2);
  });

  it('negeerRapportage roept service aan en filtert het rapport eruit', () => {
    component.negeerRapportage(1);

    expect(rapportSpy.negeerRapportage).toHaveBeenCalledWith(1);
    expect(component.rapportages.find((r: any) => r.id === 1)).toBeUndefined();
  });

  it('verwijderReview filtert alle rapportages voor dezelfde reviewId weg', () => {
    component.verwijderReview(10);

    expect(reviewSpy.verwijderRapporteerdeBeoordeling).toHaveBeenCalledWith(10);
    expect(component.rapportages.every((r: any) => r.reviewId !== 10)).toBeTrue();
  });
});
