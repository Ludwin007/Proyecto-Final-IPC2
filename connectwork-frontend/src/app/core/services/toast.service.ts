import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: number;
  mensaje: string;
  tipo: 'exito' | 'error' | 'info';
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private contador = 0;
  toasts = signal<Toast[]>([]);

  exito(mensaje: string): void { this.agregar(mensaje, 'exito'); }
  error(mensaje: string): void { this.agregar(mensaje, 'error'); }
  info(mensaje: string): void  { this.agregar(mensaje, 'info'); }

  private agregar(mensaje: string, tipo: Toast['tipo']): void {
    const id = ++this.contador;
    this.toasts.update(t => [...t, { id, mensaje, tipo }]);
    setTimeout(() => this.toasts.update(t => t.filter(x => x.id !== id)), 4000);
  }
}
