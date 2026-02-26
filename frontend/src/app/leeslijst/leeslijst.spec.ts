import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Leeslijst } from './leeslijst';

describe('Leeslijst', () => {
  let component: Leeslijst;
  let fixture: ComponentFixture<Leeslijst>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Leeslijst]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Leeslijst);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
