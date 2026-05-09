import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  private readonly baseUrl = '/api';

  get<T>(ruta: string, params?: any): Observable<T> {
    const options = params ? { params: new HttpParams({ fromObject: params }) } : {};
    return this.http.get<T>(`${this.baseUrl}${ruta}`, options);
  }

  post<T>(ruta: string, cuerpo: any = {}): Observable<T> {
    return this.http.post<T>(`${this.baseUrl}${ruta}`, cuerpo);
  }

  put<T>(ruta: string, cuerpo: any = {}): Observable<T> {
    return this.http.put<T>(`${this.baseUrl}${ruta}`, cuerpo);
  }

  delete<T>(ruta: string): Observable<T> {
    return this.http.delete<T>(`${this.baseUrl}${ruta}`);
  }
}