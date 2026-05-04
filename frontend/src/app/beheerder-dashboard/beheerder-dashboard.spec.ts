import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BeheerderDashboard } from './beheerder-dashboard';

describe('BeheerderDashboard', () => {
  let component: BeheerderDashboard;
  let fixture: ComponentFixture<BeheerderDashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BeheerderDashboard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BeheerderDashboard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
