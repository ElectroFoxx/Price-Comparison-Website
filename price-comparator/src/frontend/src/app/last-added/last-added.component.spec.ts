import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LastAddedComponent } from './last-added.component';

describe('LastAddedComponent', () => {
  let component: LastAddedComponent;
  let fixture: ComponentFixture<LastAddedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LastAddedComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LastAddedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
