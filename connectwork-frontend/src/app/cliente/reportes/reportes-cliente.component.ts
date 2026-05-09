import { Component, signal, inject } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { PdfExportService } from '../../core/services/pdf-export.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-reportes-cliente',
  standalone: true,
  imports: [FormsModule, CommonModule],
  template: `
    <div class="topbar">
      <div class="topbar-titulo">
        <h2>Reportes</h2>
        <p>Revisa tu actividad y gasto en la plataforma</p>
      </div>
    </div>

    <div class="card" style="margin-bottom:24px">
      <div style="display:flex;gap:16px;flex-wrap:wrap;align-items:flex-end">
        <div class="campo" style="flex:1;min-width:150px;margin-bottom:0">
          <label>Desde</label>
          <input type="date" [(ngModel)]="desde" />
        </div>
        <div class="campo" style="flex:1;min-width:150px;margin-bottom:0">
          <label>Hasta</label>
          <input type="date" [(ngModel)]="hasta" />
        </div>
        <button class="btn btn-primario" (click)="generar()">Generar</button>
      </div>
    </div>

    <div style="display:flex;flex-direction:column;gap:20px">
      <div class="card">
        <div class="card-header">
          <span class="card-titulo">Historial de proyectos</span>
          @if (proyectos().length > 0) {
            <button class="btn btn-primario btn-sm" (click)="exportar('cliente-proyectos','reporte-proyectos.pdf')">↓ PDF</button>
          }
        </div>
        @if (proyectos().length === 0) {
          <p style="color:var(--texto-suave)">Selecciona un intervalo y genera el reporte</p>
        } @else {
          <div class="tabla-contenedor">
            <table class="tabla">
              <thead><tr><th>Proyecto</th><th>Estado</th><th>Monto</th><th>Freelancer</th><th>Fecha</th></tr></thead>
              <tbody>
                @for (p of proyectos(); track $index) {
                  <tr>
                    <td>{{ p.titulo }}</td>
                    <td><span class="badge" [class]="badge(p.estado)">{{ p.estado }}</span></td>
                    <td>{{ p.monto ? ('Q ' + (p.monto | number:'1.2-2')) : '—' }}</td>
                    <td>{{ p.freelancer ?? '—' }}</td>
                    <td>{{ p.fechaPublicacion | date:'dd/MM/yyyy' }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </div>

      <div class="grid-2">
        <div class="card">
          <div class="card-header"><span class="card-titulo">Gasto por categoria</span></div>
          @if (gastoCategoria().length === 0) {
            <p style="color:var(--texto-suave)">Sin datos para el periodo</p>
          } @else {
            <div class="tabla-contenedor">
              <table class="tabla">
                <thead><tr><th>Categoria</th><th>Contratos</th><th>Total gastado</th></tr></thead>
                <tbody>
                  @for (g of gastoCategoria(); track $index) {
                    <tr>
                      <td>{{ g.categoria }}</td>
                      <td>{{ g.totalContratos }}</td>
                      <td><strong>Q {{ g.totalGastado | number:'1.2-2' }}</strong></td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </div>

        <div class="card">
          <div class="card-header">
            <span class="card-titulo">Historial de recargas</span>
            @if (recargas().length > 0) {
              <button class="btn btn-primario btn-sm" (click)="exportarRecargas()">↓ PDF</button>
            }
          </div>
          <button class="btn btn-contorno btn-sm" style="margin-bottom:16px" (click)="cargarRecargas()">Cargar recargas</button>
          @if (recargas().length === 0) {
            <p style="color:var(--texto-suave)">Sin recargas registradas</p>
          } @else {
            <div class="tabla-contenedor">
              <table class="tabla">
                <thead><tr><th>Monto</th><th>Descripcion</th><th>Fecha</th></tr></thead>
                <tbody>
                  @for (r of recargas(); track r.idRecarga) {
                    <tr>
                      <td><strong style="color:var(--verde)">Q {{ r.monto | number:'1.2-2' }}</strong></td>
                      <td>{{ r.descripcion ?? '—' }}</td>
                      <td>{{ r.fecha | date:'dd/MM/yyyy HH:mm' }}</td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </div>
      </div>
    </div>
  `
})
export class ReportesClienteComponent {
  private api = inject(ApiService);
  private toast = inject(ToastService);
  private pdfExport = inject(PdfExportService);

  desde = '';
  hasta = '';
  proyectos = signal<any[]>([]);
  gastoCategoria = signal<any[]>([]);
  recargas = signal<any[]>([]);

  generar(): void {
    if (!this.desde || !this.hasta) { this.toast.error('Selecciona el intervalo de fechas'); return; }
    const p = { desde: this.desde, hasta: this.hasta };
    this.api.get<any>('/cliente/reportes/proyectos', p).subscribe(res => this.proyectos.set(res.datos?.proyectos ?? []));
    this.api.get<any>('/cliente/reportes/categorias', p).subscribe(res => this.gastoCategoria.set(res.datos?.gastoCategoria ?? []));
  }

  exportar(tipo: string, nombre: string): void {
    if (!this.desde || !this.hasta) { this.toast.error('Primero genera el reporte'); return; }
    this.pdfExport.descargar(tipo, this.desde, this.hasta, nombre);
  }

  exportarRecargas(): void {
    this.pdfExport.descargar('cliente-recargas', undefined, undefined, 'reporte-recargas.pdf');
  }

  cargarRecargas(): void {
    this.api.get<any>('/cliente/reportes/recargas').subscribe(res => this.recargas.set(res.datos?.recargas ?? []));
  }

  badge(e: string): string {
    const m: Record<string, string> = { ABIERTO:'badge-verde', EN_PROGRESO:'badge-azul', COMPLETADO:'badge-gris', CANCELADO:'badge-rojo', ENTREGA_PENDIENTE:'badge-naranja' };
    return m[e] ?? 'badge-gris';
  }
}
