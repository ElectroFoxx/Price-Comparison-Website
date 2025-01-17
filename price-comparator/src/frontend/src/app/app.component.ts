import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { environment } from '../environments/environment';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})

export class AppComponent {
  title = 'price-comparator';

  searchValue = ''

  isLoggedIn = localStorage.getItem('access_token') !== null;

  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient, private router: Router) { }

  logout() {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    window.location.href = '/';
  }

  async search(): Promise<void> {
    console.log("Search function triggered", this.searchValue);

    let productId: number = await firstValueFrom(
      this.http.get<number>(`${this.apiUrl}/product/find/${this.searchValue}`)
    );

    let shouldAdd: boolean = false;

    if (productId == -1) {
      shouldAdd = confirm(`${this.searchValue} not found in database. Would you like to add it?`);
    }
    else {
      this.router.navigate([`/product/${productId}`]);
      return;
    }

    if (shouldAdd) {
      let body = {
        "manufacturer_code": this.searchValue
      }

      this.http.post<number>(`${this.apiUrl}/product/add`, body).subscribe(
        (response: number) => {
          this.router.navigate([`/product/${response}`]);
        },
        (error) => {
          console.error('Error:', error);
        }
      );
    }
  }
}
