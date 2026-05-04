import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KlassenleeslijstenLeerling } from './klassenleeslijsten-leerling';

describe('KlassenleeslijstenLeerling', () => {
  let component: KlassenleeslijstenLeerling;
  let fixture: ComponentFixture<KlassenleeslijstenLeerling>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KlassenleeslijstenLeerling]
    })
    .compileComponents();

    fixture = TestBed.createComponent(KlassenleeslijstenLeerling);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
