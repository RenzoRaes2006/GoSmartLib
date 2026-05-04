import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KlassLeeslijstBeheerComponent } from './klass-leeslijst-beheer';

describe('KlassLeeslijstBeheer', () => {
  let component: KlassLeeslijstBeheerComponent;
  let fixture: ComponentFixture<KlassLeeslijstBeheerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KlassLeeslijstBeheerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(KlassLeeslijstBeheerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
