import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KlassenleeslijstDetailLeerling } from './klassenleeslijst-detail-leerling';

describe('KlassenleeslijstDetailLeerling', () => {
  let component: KlassenleeslijstDetailLeerling;
  let fixture: ComponentFixture<KlassenleeslijstDetailLeerling>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KlassenleeslijstDetailLeerling]
    })
    .compileComponents();

    fixture = TestBed.createComponent(KlassenleeslijstDetailLeerling);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
