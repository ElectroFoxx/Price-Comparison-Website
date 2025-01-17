import { Component } from '@angular/core';
import { LastAddedComponent } from '../last-added/last-added.component';

@Component({
  selector: 'app-home',
  imports: [LastAddedComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {

}
