import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { environment } from '../../environments/environment';

interface AuthResponse {
  access_token: string;
  refresh_token: string;
  message: string;
}

@Component({
  selector: 'app-register',
  imports: [CommonModule, FormsModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  username = '';
  email = '';
  password = '';

  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  register() {
    const body = {
      email: this.email, password: this.password, username: this.username
    }
    this.http.post<AuthResponse>(`${this.apiUrl}/auth/register`, body).subscribe(
      (response: AuthResponse) => {
        console.log('JWT Token:', response.access_token);
        localStorage.setItem('access_token', response.access_token);
        localStorage.setItem('refresh_token', response.refresh_token);
        window.location.href = '/';
      },
      (error) => {
        console.error('Error:', error);
      }
    );
  }
}
