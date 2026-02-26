import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Catalogus } from './catalogus';

describe('Catalogus', () => {
  let component: Catalogus;
  let fixture: ComponentFixture<Catalogus>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Catalogus]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Catalogus);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
