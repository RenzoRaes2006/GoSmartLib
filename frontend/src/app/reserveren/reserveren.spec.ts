import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Reserveren } from './reserveren';

describe('Reserveren', () => {
  let component: Reserveren;
  let fixture: ComponentFixture<Reserveren>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Reserveren]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Reserveren);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
