import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ToastService } from './toast.service';

@Injectable({ providedIn: 'root' })
export class PdfExportService {
  private http = inject(HttpClient);
  private toast = inject(ToastService);
  private readonly base = '/api';

  descargar(tipo: string, desde?: string, hasta?: string, nombreArchivo = 'reporte.pdf'): void {
    let params = new HttpParams().set('tipo', tipo);
    if (desde) params = params.set('desde', desde);
    if (hasta) params = params.set('hasta', hasta);

    this.http.get(`${this.base}/pdf`, {
      params,
      responseType: 'blob'
    }).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = nombreArchivo;
        a.click();
        URL.revokeObjectURL(url);
        this.toast.exito('Reporte descargado exitosamente');
      },
      error: () => {
        this.toast.error('Error al generar el PDF');
      }
    });
  }
}
