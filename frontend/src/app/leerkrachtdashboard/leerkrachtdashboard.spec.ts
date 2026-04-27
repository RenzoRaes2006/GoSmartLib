import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Leerkrachtdashboard } from './leerkrachtdashboard';

describe('Leerkrachtdashboard', () => {
  let component: Leerkrachtdashboard;
  let fixture: ComponentFixture<Leerkrachtdashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Leerkrachtdashboard],
    }).compileComponents();

    fixture = TestBed.createComponent(Leerkrachtdashboard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
