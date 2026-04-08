import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GerapporteerdeReviews } from './gerapporteerde-reviews';

describe('GerapporteerdeReviews', () => {
  let component: GerapporteerdeReviews;
  let fixture: ComponentFixture<GerapporteerdeReviews>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GerapporteerdeReviews]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GerapporteerdeReviews);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
