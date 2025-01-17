import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ChartType, NgApexchartsModule } from 'ng-apexcharts';
import { environment } from '../../environments/environment';
import { CommonModule } from '@angular/common';

interface Price {
  id: number;
  created_at: string; // ISO date string
  price_xkom: number;
  price_morele: number;
  price_media_expert: number;
}

interface Product {
  id: number;
  prices: Price[];
  subscriptions: any[];
  manufacturer_code: string;
  created_at: string;
}



@Component({
  selector: 'app-product-page',
  imports: [NgApexchartsModule, FormsModule, CommonModule],
  templateUrl: './product-page.component.html',
  styleUrl: './product-page.component.css'
})
export class ProductPageComponent {
  id = -1;
  manufacturer_code = '';

  private apiUrl = environment.apiUrl;

  chart = {
    type: 'line' as ChartType,
  };

  series: ApexAxisChartSeries = [
    {
      name: "x-kom",
      data: []
    },
    {
      name: "Morele",
      data: []
    },
    {
      name: "Media Expert",
      data: []
    }
  ];

  xaxis: ApexXAxis = {
    categories: [],
    type: 'datetime'
  };

  legend: ApexLegend = {
    show: true,
    position: 'top'
  };

  constructor(private route: ActivatedRoute, private http: HttpClient) { }

  ngOnInit() {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.fetchProductData();
  }

  private fetchProductData() {
    this.http.get<Product>(`${this.apiUrl}/product/${this.id}`).subscribe(
      (response: Product) => {
        this.manufacturer_code = response.manufacturer_code;

        const categories: string[] = [];
        const s0: number[] = [];
        const s1: number[] = [];
        const s2: number[] = [];

        for (let price of response.prices) {
          const date = new Date(price.created_at).toISOString().slice(0, 10); // Format as YYYY-MM-DD
          categories.push(date);
          s0.push(price.price_xkom / 100);
          s1.push(price.price_morele / 100);
          s2.push(price.price_media_expert / 100);
        }

        this.series = [
          { name: 'x-kom', data: s0 },
          { name: 'Morele', data: s1 },
          { name: 'Media Expert', data: s2 }
        ];
        this.xaxis = {
          categories: categories,
          type: 'datetime'
        };
      },
      (error) => {
        console.error('Error:', error);
      }
    );
  }

  amount: string = '';

  subscribe() {
    const authHeader = 'Bearer ' + localStorage.getItem('access_token');
    const headers = { 'Authorization': authHeader };

    this.http.post<String>(`${this.apiUrl}/subscription/${this.id}`, {"price": this.amount}, { headers }).subscribe(
      (response: String) => {
        console.log(response);
      },
      (error) => {
        console.error('Error:', error);
      }
    );
    console.log('Subscribed with amount:', this.amount);
    alert(`Subscribed with amount: ${this.amount}`);
  }

  isLoggedIn = localStorage.getItem('access_token') !== null;
}
