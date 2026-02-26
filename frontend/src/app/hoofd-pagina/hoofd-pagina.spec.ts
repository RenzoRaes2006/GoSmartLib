import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HoofdPagina } from './hoofd-pagina';

describe('HoofdPagina', () => {
  let component: HoofdPagina;
  let fixture: ComponentFixture<HoofdPagina>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HoofdPagina]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HoofdPagina);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
