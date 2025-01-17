import { Component } from '@angular/core';
import { ProductCardComponent } from '../product-card/product-card.component';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

interface Price {
  id: number;
  created_at: string;
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

function timeAgo(timestamp: string): string {
  const now = new Date();
  const past = new Date(timestamp);
  const diff = now.getTime() - past.getTime() + 3600000;

  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);

  if (seconds < 60) {
      return `${seconds} seconds ago`;
  } else if (minutes < 60) {
      return `${minutes} minutes ago`;
  } else {
      return `${hours} hours ago`;
  }
}

@Component({
  selector: 'app-last-added',
  imports: [ProductCardComponent],
  templateUrl: './last-added.component.html',
  styleUrl: './last-added.component.css'
})

export class LastAddedComponent {
  productss: any[] = [];

  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  products = [] as { id: number, code: string, created_at: string }[];

  ngOnInit() {
    this.http.get<Product[]>(`${this.apiUrl}/product/latest`).subscribe(
      (response: Product[]) => {
        const newProducts = response.map(product => ({ id: product.id, code: product.manufacturer_code, created_at: timeAgo(product.created_at) }));
        this.products = newProducts;
      },
      (error) => {
        console.error('Error:', error);
      }
    );
  }
}
