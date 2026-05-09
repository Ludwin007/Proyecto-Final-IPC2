import { Component, signal, inject, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-contratos-cliente',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="topbar">
      <div class="topbar-titulo">
        <h2>Mis contratos</h2>
        <p>Seguimiento de todos tus contratos activos y finalizados</p>
      </div>
    </div>

    @if (cargando()) {
      <div class="cargando-contenedor"><div class="spinner"></div><p>Cargando contratos...</p></div>
    } @else if (contratos().length === 0) {
      <div class="card estado-vacio">
        <span class="icono-vacio">📝</span>
        <h3>No tienes contratos aun</h3>
        <p>Acepta una propuesta en alguno de tus proyectos para generar un contrato</p>
        <a routerLink="/cliente/proyectos" class="btn btn-primario" style="margin-top:16px">Ver mis proyectos</a>
      </div>
    } @else {
      <div style="display:flex;flex-direction:column;gap:16px">
        @for (c of contratos(); track c.idContrato) {
          <a routerLink="/cliente/contratos/{{ c.idContrato }}" style="display:block">
            <div class="proyecto-card">
              <div class="proyecto-card-header">
                <div>
                  <div class="proyecto-card-titulo">{{ c.tituloProyecto }}</div>
                  <p style="font-size:13px;color:var(--texto-suave);margin-top:4px">Freelancer: <strong>{{ c.nombreFreelancer }}</strong></p>
                </div>
                <span class="badge" [class]="badgeContrato(c.estado)">{{ c.estado }}</span>
              </div>
              <div class="proyecto-card-meta">
                <span>💰 Monto: Q {{ c.monto | number:'1.2-2' }}</span>
                <span>📊 Comision: {{ c.porcComision }}%</span>
                <span>📅 Inicio: {{ c.fechaInicio | date:'dd/MM/yyyy' }}</span>
                @if (c.fechaFin) {
                  <span>✅ Fin: {{ c.fechaFin | date:'dd/MM/yyyy' }}</span>
                }
              </div>
              @if (c.estado === 'ENTREGA_PENDIENTE') {
                <div class="alerta alerta-alerta" style="margin-top:12px;margin-bottom:0">
                  Hay una entrega pendiente de revision
                </div>
              }
            </div>
          </a>
        }
      </div>
    }
  `
})
export class ContratosClienteComponent implements OnInit {
  private api = inject(ApiService);
  cargando = signal(true);
  contratos = signal<any[]>([]);

  ngOnInit(): void {
    this.api.get<any>('/contratos').subscribe({
      next: res => { this.contratos.set(res.datos?.contratos ?? []); this.cargando.set(false); },
      error: () => this.cargando.set(false)
    });
  }

  badgeContrato(e: string): string {
    const m: Record<string, string> = { EN_PROGRESO:'badge-azul', COMPLETADO:'badge-verde', CANCELADO:'badge-rojo', ENTREGA_PENDIENTE:'badge-naranja' };
    return m[e] ?? 'badge-gris';
  }
}
