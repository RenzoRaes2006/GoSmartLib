import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BoekBeheer } from './boek-beheer';

describe('BoekBeheer', () => {
  let component: BoekBeheer;
  let fixture: ComponentFixture<BoekBeheer>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BoekBeheer],
    }).compileComponents();

    fixture = TestBed.createComponent(BoekBeheer);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
