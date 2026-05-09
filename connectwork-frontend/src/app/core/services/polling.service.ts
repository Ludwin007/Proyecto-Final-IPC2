import { Injectable, inject, OnDestroy } from '@angular/core';
import { interval, Subscription, switchMap, startWith } from 'rxjs';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class PollingService implements OnDestroy {
  private api = inject(ApiService);
  private subscripciones = new Map<string, Subscription>();

  iniciar<T>(clave: string, ruta: string, intervaloMs: number, callback: (datos: T) => void): void {
    this.detener(clave);
    const sub = interval(intervaloMs).pipe(
      startWith(0),
      switchMap(() => this.api.get<T>(ruta))
    ).subscribe({
      next: datos => callback(datos),
      error: err => console.warn(`Error polling ${ruta}:`, err)
    });
    this.subscripciones.set(clave, sub);
  }

  detener(clave: string): void {
    const sub = this.subscripciones.get(clave);
    if (sub) {
      sub.unsubscribe();
      this.subscripciones.delete(clave);
    }
  }

  detenerTodo(): void {
    this.subscripciones.forEach(s => s.unsubscribe());
    this.subscripciones.clear();
  }

  ngOnDestroy(): void {
    this.detenerTodo();
  }
}
