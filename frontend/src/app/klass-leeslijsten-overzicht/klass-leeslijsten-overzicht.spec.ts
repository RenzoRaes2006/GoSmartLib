import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KlassLeeslijstenOverzichtComponent } from './klass-leeslijsten-overzicht';

describe('KlassLeeslijstenOverzicht', () => {
  let component: KlassLeeslijstenOverzichtComponent;
  let fixture: ComponentFixture<KlassLeeslijstenOverzichtComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KlassLeeslijstenOverzichtComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(KlassLeeslijstenOverzichtComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
