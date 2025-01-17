import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { environment } from '../../environments/environment';

interface AuthResponse
{
    access_token: string;
    refresh_token: string;
    message: string;
}

@Component({
    selector: 'app-login',
    imports: [CommonModule, FormsModule],
    templateUrl: './login.component.html',
    styleUrl: './login.component.css'
})
export class LoginComponent {
    username = '';
    password = '';

    private apiUrl = environment.apiUrl;

    constructor(private http: HttpClient) {}

    login()
    {
        const body = {
            password: this.password, username: this.username
        }
        console.log(body)
        this.http.post<AuthResponse>(`${this.apiUrl}/auth/login`, body).subscribe(
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