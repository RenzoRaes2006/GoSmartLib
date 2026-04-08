import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AanvragenBeheer } from './aanvragen-beheer';

describe('AanvragenBeheer', () => {
  let component: AanvragenBeheer;
  let fixture: ComponentFixture<AanvragenBeheer>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AanvragenBeheer]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AanvragenBeheer);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
