import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HomePageLeerling } from './home-page-leerling';

describe('HomePageLeerling', () => {
  let component: HomePageLeerling;
  let fixture: ComponentFixture<HomePageLeerling>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomePageLeerling]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HomePageLeerling);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
