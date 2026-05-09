import { Component, signal, inject, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { AuthService } from '../../core/services/auth.service';
import { PdfExportService } from '../../core/services/pdf-export.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-reportes-fl',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="topbar">
      <div class="topbar-titulo">
        <h2>Mis reportes</h2>
        <p>Revisa tu actividad y ganancias en la plataforma</p>
      </div>
    </div>

    <div class="stats-grid" style="margin-bottom:24px">
      <div class="stat-card">
        <div class="stat-icono" style="background:#f0fdf4">💰</div>
        <div class="stat-info">
          <strong>Q {{ auth.usuario()?.saldo | number:'1.2-2' }}</strong>
          <span>Saldo actual</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icono" style="background:#fdf4ff">⭐</div>
        <div class="stat-info">
          <strong>{{ calificacion() | number:'1.1-1' }}</strong>
          <span>Calificacion promedio</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icono" style="background:#eff6ff">✅</div>
        <div class="stat-info">
          <strong>{{ totalContratos() }}</strong>
          <span>Contratos completados</span>
        </div>
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
        <button class="btn btn-primario" (click)="generar()">Generar reportes</button>
      </div>
    </div>

    <div style="display:flex;flex-direction:column;gap:20px">
      <div class="card">
        <div class="card-header">
          <span class="card-titulo">Contratos completados en el periodo</span>
          @if (contratos().length > 0) {
            <button class="btn btn-primario btn-sm" (click)="exportar('freelancer-contratos','reporte-contratos.pdf')">↓ PDF</button>
          }
        </div>
        @if (contratos().length === 0) {
          <p style="color:var(--texto-suave)">Selecciona un intervalo y genera el reporte</p>
        } @else {
          <div class="tabla-contenedor">
            <table class="tabla">
              <thead>
                <tr>
                  <th>Proyecto</th>
                  <th>Cliente</th>
                  <th>Monto neto</th>
                  <th>Calificacion</th>
                  <th>Fecha</th>
                </tr>
              </thead>
              <tbody>
                @for (c of contratos(); track $index) {
                  <tr>
                    <td>{{ c.proyecto }}</td>
                    <td>{{ c.cliente }}</td>
                    <td><strong style="color:var(--verde)">Q {{ c.montoNeto | number:'1.2-2' }}</strong></td>
                    <td>
                      @if (c.puntuacion) {
                        <span style="color:var(--naranja)">{{ '★'.repeat(c.puntuacion) }}{{ '☆'.repeat(5 - c.puntuacion) }}</span>
                      } @else {
                        <span style="color:var(--texto-suave)">Sin calificacion</span>
                      }
                    </td>
                    <td>{{ c.fechaFin | date:'dd/MM/yyyy' }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </div>

      <div class="grid-2">
        <div class="card">
          <div class="card-header">
            <span class="card-titulo">Top categorias trabajadas</span>
            @if (topCategorias().length > 0) {
              <button class="btn btn-primario btn-sm" (click)="exportarCategorias()">↓ PDF</button>
            }
          </div>
          <button class="btn btn-contorno btn-sm" style="margin-bottom:14px" (click)="cargarTopCategorias()">Cargar top categorias</button>
          @if (topCategorias().length === 0) {
            <p style="color:var(--texto-suave)">Sin datos disponibles</p>
          } @else {
            <div class="tabla-contenedor">
              <table class="tabla">
                <thead><tr><th>#</th><th>Categoria</th><th>Contratos</th><th>Ingresos</th></tr></thead>
                <tbody>
                  @for (c of topCategorias(); track $index) {
                    <tr>
                      <td><strong style="color:var(--destacado)">{{ $index + 1 }}</strong></td>
                      <td>{{ c.categoria }}</td>
                      <td>{{ c.totalContratos }}</td>
                      <td>Q {{ c.totalIngresos | number:'1.2-2' }}</td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </div>

        <div class="card">
          <div class="card-header">
            <span class="card-titulo">Propuestas enviadas en el periodo</span>
            @if (propuestas().length > 0) {
              <button class="btn btn-primario btn-sm" (click)="exportar('freelancer-propuestas','reporte-propuestas.pdf')">↓ PDF</button>
            }
          </div>
          @if (propuestas().length === 0) {
            <p style="color:var(--texto-suave)">Selecciona un intervalo y genera el reporte</p>
          } @else {
            <div class="tabla-contenedor">
              <table class="tabla">
                <thead><tr><th>Proyecto</th><th>Monto</th><th>Estado</th></tr></thead>
                <tbody>
                  @for (p of propuestas(); track $index) {
                    <tr>
                      <td>{{ p.proyecto }}</td>
                      <td>Q {{ p.montoOfertado | number:'1.2-2' }}</td>
                      <td><span class="badge" [class]="badgeProp(p.estado)">{{ p.estado }}</span></td>
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
export class ReportesFlComponent implements OnInit {
  private api = inject(ApiService);
  private toast = inject(ToastService);
  private pdfExport = inject(PdfExportService);
  auth = inject(AuthService);

  desde = '';
  hasta = '';
  calificacion = signal(0);
  totalContratos = signal(0);
  contratos = signal<any[]>([]);
  topCategorias = signal<any[]>([]);
  propuestas = signal<any[]>([]);

  ngOnInit(): void {
    this.api.get<any>('/freelancer/perfil').subscribe(res => {
      this.calificacion.set(res.datos?.perfil?.calificacionProm ?? 0);
      this.totalContratos.set(res.datos?.perfil?.totalContratos ?? 0);
    });
  }

  generar(): void {
    if (!this.desde || !this.hasta) { this.toast.error('Selecciona el intervalo de fechas'); return; }
    const p = { desde: this.desde, hasta: this.hasta };
    this.api.get<any>('/freelancer/reportes/contratos', p).subscribe(res => this.contratos.set(res.datos?.contratos ?? []));
    this.api.get<any>('/freelancer/reportes/propuestas', p).subscribe(res => this.propuestas.set(res.datos?.propuestas ?? []));
  }

  exportar(tipo: string, nombre: string): void {
    if (!this.desde || !this.hasta) { this.toast.error('Primero genera el reporte'); return; }
    this.pdfExport.descargar(tipo, this.desde, this.hasta, nombre);
  }

  exportarCategorias(): void {
    this.pdfExport.descargar('freelancer-categorias', undefined, undefined, 'reporte-top-categorias.pdf');
  }

  cargarTopCategorias(): void {
    this.api.get<any>('/freelancer/reportes/categorias').subscribe(res => this.topCategorias.set(res.datos?.topCategorias ?? []));
  }

  badgeProp(e: string): string {
    const m: Record<string, string> = { PENDIENTE: 'badge-naranja', ACEPTADA: 'badge-verde', RECHAZADA: 'badge-rojo', RETIRADA: 'badge-gris' };
    return m[e] ?? 'badge-gris';
  }
}
