import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Leengeschiedenis } from './leengeschiedenis';

describe('Leengeschiedenis', () => {
  let component: Leengeschiedenis;
  let fixture: ComponentFixture<Leengeschiedenis>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Leengeschiedenis]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Leengeschiedenis);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
