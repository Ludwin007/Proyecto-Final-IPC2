import { Component, signal, inject, OnInit, OnDestroy } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { PollingService } from '../../core/services/polling.service';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-contratos-fl',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="topbar">
      <div class="topbar-titulo">
        <h2>Mis contratos</h2>
        <p>Gestiona tu trabajo activo y finalizado</p>
      </div>
    </div>

    @if (cargando()) {
      <div class="cargando-contenedor"><div class="spinner"></div><p>Cargando contratos...</p></div>
    } @else if (contratos().length === 0) {
      <div class="card estado-vacio">
        <span class="icono-vacio">📝</span>
        <h3>Sin contratos aun</h3>
        <p>Cuando un cliente acepte una de tus propuestas se creara un contrato aqui</p>
        <a routerLink="/freelancer/explorar" class="btn btn-primario" style="margin-top:16px">Explorar proyectos</a>
      </div>
    } @else {
      <div style="display:flex;flex-direction:column;gap:14px">
        @for (c of contratos(); track c.idContrato) {
          <a routerLink="/freelancer/contratos/{{ c.idContrato }}" style="display:block">
            <div class="proyecto-card" [style.borderLeft]="'4px solid ' + colorContrato(c.estado)">
              <div class="proyecto-card-header">
                <div>
                  <div class="proyecto-card-titulo">{{ c.tituloProyecto }}</div>
                  <p style="font-size:13px;color:var(--texto-suave);margin-top:4px">Cliente: <strong>{{ c.nombreCliente }}</strong></p>
                </div>
                <span class="badge" [class]="badgeContrato(c.estado)">{{ c.estado }}</span>
              </div>
              <div class="proyecto-card-meta">
                <span>💰 Monto: Q {{ c.monto | number:'1.2-2' }}</span>
                <span>📊 Comision: {{ c.porcComision }}%</span>
                <span style="color:var(--verde)">Neto: Q {{ montoNeto(c) | number:'1.2-2' }}</span>
                <span>📅 Inicio: {{ c.fechaInicio | date:'dd/MM/yyyy' }}</span>
              </div>
              @if (c.estado === 'ENTREGA_PENDIENTE') {
                <div class="alerta alerta-alerta" style="margin-top:12px;margin-bottom:0">
                  Tu entrega esta siendo revisada por el cliente
                </div>
              }
              @if (c.estado === 'EN_PROGRESO') {
                <div class="alerta alerta-info" style="margin-top:12px;margin-bottom:0">
                  Contrato activo - puedes subir una entrega cuando estes listo
                </div>
              }
            </div>
          </a>
        }
      </div>
    }
  `
})
export class ContratosFlComponent implements OnInit, OnDestroy {
  private api = inject(ApiService);
  private polling = inject(PollingService);
  cargando = signal(true);
  contratos = signal<any[]>([]);

  ngOnInit(): void {
    this.polling.iniciar('lista-contratos-fl', '/contratos', 15000, (res: any) => {
      this.contratos.set(res.datos?.contratos ?? []);
      this.cargando.set(false);
    });
  }

  ngOnDestroy(): void {
    this.polling.detener('lista-contratos-fl');
  }

  montoNeto(c: any): number {
    return c.monto - (c.monto * c.porcComision / 100);
  }

  badgeContrato(e: string): string {
    const m: Record<string, string> = { EN_PROGRESO: 'badge-azul', COMPLETADO: 'badge-verde', CANCELADO: 'badge-rojo', ENTREGA_PENDIENTE: 'badge-naranja' };
    return m[e] ?? 'badge-gris';
  }

  colorContrato(e: string): string {
    const m: Record<string, string> = { EN_PROGRESO: '#3b82f6', COMPLETADO: '#10b981', CANCELADO: '#ef4444', ENTREGA_PENDIENTE: '#f59e0b' };
    return m[e] ?? '#e2e8f0';
  }
}
