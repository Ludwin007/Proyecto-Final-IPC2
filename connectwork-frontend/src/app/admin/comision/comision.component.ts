import { Component, signal, inject, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-comision',
  standalone: true,
  imports: [FormsModule, CommonModule],
  template: `
    <div class="topbar">
      <div class="topbar-titulo">
        <h2>Comision de la plataforma</h2>
        <p>Configura el porcentaje que retiene ConnectWork por cada contrato completado</p>
      </div>
    </div>

    <div class="grid-2">
      <div class="card">
        <div class="card-header"><span class="card-titulo">Porcentaje vigente</span></div>
        @if (cargando()) {
          <div class="cargando-contenedor"><div class="spinner"></div></div>
        } @else {
          <div style="text-align:center;padding:24px 0">
            <div style="font-size:64px;font-weight:800;color:var(--destacado)">{{ vigente()?.porcentaje }}%</div>
            <p style="color:var(--texto-suave);margin-top:8px">Vigente desde {{ vigente()?.fechaInicio | date:'dd/MM/yyyy' }}</p>
          </div>
          @if (confirmarCambio()) {
            <div class="alerta alerta-alerta">
              <div>
                <strong>Confirmar cambio</strong>
                <p>¿Cambiar de {{ vigente()?.porcentaje }}% a {{ nuevoPct }}%? Esto aplica solo a nuevos contratos.</p>
                <div style="display:flex;gap:8px;margin-top:12px">
                  <button class="btn btn-exito btn-sm" (click)="guardar()">Confirmar</button>
                  <button class="btn btn-contorno btn-sm" (click)="confirmarCambio.set(false)">Cancelar</button>
                </div>
              </div>
            </div>
          } @else {
            <div class="campo">
              <label>Nuevo porcentaje (%)</label>
              <input type="number" [(ngModel)]="nuevoPct" min="0" max="100" step="0.01" placeholder="Ej: 15.00" />
            </div>
            <button class="btn btn-primario" style="width:100%;justify-content:center" [disabled]="guardando()" (click)="confirmarCambio.set(true)">
              Actualizar comision
            </button>
          }
        }
      </div>

      <div class="card">
        <div class="card-header"><span class="card-titulo">Historial de comisiones</span></div>
        @if (historial().length === 0) {
          <div class="estado-vacio" style="padding:32px"><span class="icono-vacio">📋</span><p>Sin historial</p></div>
        } @else {
          <div class="tabla-contenedor">
            <table class="tabla">
              <thead>
                <tr>
                  <th>Porcentaje</th>
                  <th>Inicio</th>
                  <th>Fin</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                @for (c of historial(); track c.idComision) {
                  <tr>
                    <td><strong>{{ c.porcentaje }}%</strong></td>
                    <td>{{ c.fechaInicio | date:'dd/MM/yyyy' }}</td>
                    <td>{{ c.fechaFin ? (c.fechaFin | date:'dd/MM/yyyy') : '—' }}</td>
                    <td>
                      <span class="badge" [class]="!c.fechaFin ? 'badge-verde' : 'badge-gris'">
                        {{ !c.fechaFin ? 'Vigente' : 'Finalizado' }}
                      </span>
                    </td>
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
export class ComisionComponent implements OnInit {
  private api = inject(ApiService);
  private toast = inject(ToastService);

  cargando = signal(true);
  guardando = signal(false);
  confirmarCambio = signal(false);
  vigente = signal<any>(null);
  historial = signal<any[]>([]);
  nuevoPct = 0;

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.api.get<any>('/admin/comision').subscribe({
      next: res => {
        this.vigente.set(res.datos?.vigente ?? null);
        this.historial.set(res.datos?.historial ?? []);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false)
    });
  }

  guardar(): void {
    if (this.nuevoPct < 0 || this.nuevoPct > 100) { this.toast.error('El porcentaje debe estar entre 0 y 100'); return; }
    this.guardando.set(true);
    this.api.put<any>('/admin/comision', { porcentaje: this.nuevoPct }).subscribe({
      next: res => {
        this.guardando.set(false);
        this.confirmarCambio.set(false);
        if (res.exito) { this.toast.exito(res.mensaje); this.cargar(); }
        else this.toast.error(res.mensaje);
      },
      error: () => { this.guardando.set(false); this.toast.error('Error al actualizar'); }
    });
  }
}
