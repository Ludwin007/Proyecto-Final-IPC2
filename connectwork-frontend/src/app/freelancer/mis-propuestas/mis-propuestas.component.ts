import { Component, signal, inject, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-mis-propuestas',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="topbar">
      <div class="topbar-titulo">
        <h2>Mis propuestas</h2>
        <p>Seguimiento de todas las propuestas que has enviado</p>
      </div>
    </div>

    <div class="barra-filtros">
      <select [(ngModel)]="filtro" (ngModelChange)="aplicarFiltro()">
        <option value="">Todos los estados</option>
        <option value="PENDIENTE">Pendientes</option>
        <option value="ACEPTADA">Aceptadas</option>
        <option value="RECHAZADA">Rechazadas</option>
        <option value="RETIRADA">Retiradas</option>
      </select>
    </div>

    @if (cargando()) {
      <div class="cargando-contenedor"><div class="spinner"></div><p>Cargando propuestas...</p></div>
    } @else if (propuestasFiltradas().length === 0) {
      <div class="card estado-vacio">
        <span class="icono-vacio">📤</span>
        <h3>Sin propuestas</h3>
        <p>Cuando envies propuestas a proyectos apareceran aqui</p>
        <a routerLink="/freelancer/explorar" class="btn btn-primario" style="margin-top:16px">Explorar proyectos</a>
      </div>
    } @else {
      <div style="display:flex;flex-direction:column;gap:14px">
        @for (p of propuestasFiltradas(); track p.idPropuesta) {
          <div class="card" [style.borderLeft]="'4px solid ' + colorEstado(p.estado)">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:12px">
              <div>
                <strong style="font-size:16px">{{ p.tituloProyecto }}</strong>
                <p style="font-size:12px;color:var(--texto-suave);margin-top:4px">{{ p.fechaEnvio | date:'dd/MM/yyyy HH:mm' }}</p>
              </div>
              <span class="badge" [class]="badgePropuesta(p.estado)">{{ p.estado }}</span>
            </div>
            <div style="display:flex;gap:24px;font-size:14px;margin-bottom:12px">
              <span><strong style="color:var(--verde)">Q {{ p.montoOfertado | number:'1.2-2' }}</strong></span>
              <span style="color:var(--texto-suave)">📅 {{ p.plazoDias }} dias</span>
            </div>
            <p style="font-size:13px;color:var(--texto-suave);line-height:1.5">
              {{ p.cartaPresentacion | slice:0:200 }}{{ p.cartaPresentacion?.length > 200 ? '...' : '' }}
            </p>
            @if (p.estado === 'PENDIENTE') {
              <div style="margin-top:12px">
                <button class="btn btn-peligro btn-sm" [disabled]="procesando()" (click)="retirar(p)">Retirar propuesta</button>
              </div>
            }
          </div>
        }
      </div>
    }
  `
})
export class MisPropuestasComponent implements OnInit {
  private api = inject(ApiService);
  private toast = inject(ToastService);

  cargando = signal(true);
  procesando = signal(false);
  propuestas = signal<any[]>([]);
  propuestasFiltradas = signal<any[]>([]);
  filtro = '';

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.cargando.set(true);
    this.api.get<any>('/propuestas').subscribe({
      next: res => {
        this.propuestas.set(res.datos?.propuestas ?? []);
        this.aplicarFiltro();
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false)
    });
  }

  aplicarFiltro(): void {
    const lista = this.propuestas();
    this.propuestasFiltradas.set(this.filtro ? lista.filter(p => p.estado === this.filtro) : lista);
  }

  retirar(p: any): void {
    if (!confirm('¿Retirar esta propuesta?')) return;
    this.procesando.set(true);
    this.api.put<any>(`/propuestas/${p.idPropuesta}/retirar`).subscribe({
      next: res => {
        this.procesando.set(false);
        if (res.exito) {
          this.toast.exito('Propuesta retirada');
          this.propuestas.update(l => l.map(x => x.idPropuesta === p.idPropuesta ? { ...x, estado: 'RETIRADA' } : x));
          this.aplicarFiltro();
        } else this.toast.error(res.mensaje);
      },
      error: () => { this.procesando.set(false); this.toast.error('Error al retirar'); }
    });
  }

  badgePropuesta(e: string): string {
    const m: Record<string, string> = { PENDIENTE: 'badge-naranja', ACEPTADA: 'badge-verde', RECHAZADA: 'badge-rojo', RETIRADA: 'badge-gris' };
    return m[e] ?? 'badge-gris';
  }

  colorEstado(e: string): string {
    const m: Record<string, string> = { PENDIENTE: '#f59e0b', ACEPTADA: '#10b981', RECHAZADA: '#ef4444', RETIRADA: '#94a3b8' };
    return m[e] ?? '#e2e8f0';
  }
}
