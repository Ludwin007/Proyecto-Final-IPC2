import { Component, signal, inject } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { PdfExportService } from '../../core/services/pdf-export.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-reportes-admin',
  standalone: true,
  imports: [FormsModule, CommonModule],
  template: `
    <div class="topbar">
      <div class="topbar-titulo">
        <h2>Reportes</h2>
        <p>Analiza el rendimiento y los ingresos de la plataforma</p>
      </div>
    </div>

    <div class="card" style="margin-bottom:24px">
      <div style="display:flex;gap:16px;align-items:flex-end;flex-wrap:wrap">
        <div class="campo" style="flex:1;min-width:160px;margin-bottom:0">
          <label>Desde</label>
          <input type="date" [(ngModel)]="desde" />
        </div>
        <div class="campo" style="flex:1;min-width:160px;margin-bottom:0">
          <label>Hasta</label>
          <input type="date" [(ngModel)]="hasta" />
        </div>
        <button class="btn btn-primario" (click)="generarTodos()">Generar reportes</button>
      </div>
    </div>

    <div class="grid-2" style="margin-bottom:24px">
      <div class="card">
        <div class="card-header"><span class="card-titulo">Ingresos totales</span></div>
        @if (ingresos()) {
          <div style="text-align:center;padding:16px 0">
            <div style="font-size:40px;font-weight:800;color:var(--verde)">Q {{ ingresos().totalComisiones | number:'1.2-2' }}</div>
            <p style="color:var(--texto-suave);margin-top:8px">{{ ingresos().totalContratos }} contratos completados</p>
          </div>
        } @else {
          <div class="estado-vacio" style="padding:32px"><p>Selecciona un intervalo y genera el reporte</p></div>
        }
      </div>
      <div class="card">
        <div class="card-header"><span class="card-titulo">Comision actual</span></div>
        <div style="text-align:center;padding:16px 0">
          <div style="font-size:40px;font-weight:800;color:var(--destacado)">{{ comisionActual() }}%</div>
          <p style="color:var(--texto-suave);margin-top:8px">Porcentaje vigente</p>
        </div>
      </div>
    </div>

    <div class="grid-2">
      <div class="card">
        <div class="card-header">
          <span class="card-titulo">Top 5 Freelancers</span>
          @if (topFreelancers().length > 0) {
            <button class="btn btn-primario btn-sm" (click)="exportar('admin-freelancers','reporte-freelancers.pdf')">↓ PDF</button>
          }
        </div>
        @if (topFreelancers().length === 0) {
          <div class="estado-vacio" style="padding:32px"><p>Sin datos para el periodo</p></div>
        } @else {
          <div class="tabla-contenedor">
            <table class="tabla">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Freelancer</th>
                  <th>Contratos</th>
                  <th>Total generado</th>
                  <th>Comision</th>
                </tr>
              </thead>
              <tbody>
                @for (f of topFreelancers(); track $index) {
                  <tr>
                    <td><strong style="color:var(--destacado)">{{ $index + 1 }}</strong></td>
                    <td>{{ f.nombre }}</td>
                    <td>{{ f.totalContratos }}</td>
                    <td>Q {{ f.totalGenerado | number:'1.2-2' }}</td>
                    <td>Q {{ f.comisionPlataforma | number:'1.2-2' }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </div>
      <div class="card">
        <div class="card-header">
          <span class="card-titulo">Top 5 Categorias</span>
          @if (topCategorias().length > 0) {
            <button class="btn btn-primario btn-sm" (click)="exportar('admin-categorias','reporte-categorias.pdf')">↓ PDF</button>
          }
        </div>
        @if (topCategorias().length === 0) {
          <div class="estado-vacio" style="padding:32px"><p>Sin datos para el periodo</p></div>
        } @else {
          <div class="tabla-contenedor">
            <table class="tabla">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Categoria</th>
                  <th>Contratos</th>
                  <th>Comisiones</th>
                </tr>
              </thead>
              <tbody>
                @for (c of topCategorias(); track $index) {
                  <tr>
                    <td><strong style="color:var(--acento)">{{ $index + 1 }}</strong></td>
                    <td>{{ c.categoria }}</td>
                    <td>{{ c.totalContratos }}</td>
                    <td>Q {{ c.totalComisiones | number:'1.2-2' }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </div>
    </div>
  `
})
export class ReportesAdminComponent {
  private api = inject(ApiService);
  private toast = inject(ToastService);
  private pdfExport = inject(PdfExportService);

  desde = '';
  hasta = '';
  ingresos = signal<any>(null);
  topFreelancers = signal<any[]>([]);
  topCategorias = signal<any[]>([]);
  comisionActual = signal(0);

  constructor() {
    this.api.get<any>('/admin/comision').subscribe(res => this.comisionActual.set(res.datos?.vigente?.porcentaje ?? 0));
  }

  generarTodos(): void {
    if (!this.desde || !this.hasta) { this.toast.error('Selecciona el intervalo de fechas'); return; }
    const params = { desde: this.desde, hasta: this.hasta };
    this.api.get<any>('/admin/reportes/ingresos', params).subscribe(res => this.ingresos.set(res.datos));
    this.api.get<any>('/admin/reportes/freelancers', params).subscribe(res => this.topFreelancers.set(res.datos?.topFreelancers ?? []));
    this.api.get<any>('/admin/reportes/categorias', params).subscribe(res => this.topCategorias.set(res.datos?.topCategorias ?? []));
  }

  exportar(tipo: string, nombre: string): void {
    if (!this.desde || !this.hasta) { this.toast.error('Primero genera el reporte'); return; }
    this.pdfExport.descargar(tipo, this.desde, this.hasta, nombre);
  }
}
