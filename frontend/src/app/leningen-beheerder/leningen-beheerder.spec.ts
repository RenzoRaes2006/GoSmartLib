import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LeningenBeheerder } from './leningen-beheerder';

describe('LeningenBeheerder', () => {
  let component: LeningenBeheerder;
  let fixture: ComponentFixture<LeningenBeheerder>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LeningenBeheerder]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LeningenBeheerder);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
