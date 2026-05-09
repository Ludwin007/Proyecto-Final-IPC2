import { Component, signal, inject, OnInit, computed } from '@angular/core';
import { ApiService } from '../core/services/api.service';
import { AuthService } from '../core/services/auth.service';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-freelancer-dashboard',
  standalone: true,
  imports: [RouterLink, CommonModule],
  template: `
    <div class="topbar">
      <div class="topbar-titulo">
        <h2>Hola, {{ nombreCorto() }}</h2>
        <p>Aqui tienes un resumen de tu actividad en ConnectWork</p>
      </div>
      <a routerLink="/freelancer/explorar" class="btn btn-primario">🔍 Explorar proyectos</a>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icono" style="background:#f0fdf4">💰</div>
        <div class="stat-info">
          <strong>Q {{ (auth.usuario()?.saldo || 0) | number:'1.2-2' }}</strong>
          <span>Saldo disponible</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icono" style="background:#eff6ff">📝</div>
        <div class="stat-info">
          <strong>{{ stats().contratosActivos }}</strong>
          <span>Contratos activos</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icono" style="background:#fff7ed">📤</div>
        <div class="stat-info">
          <strong>{{ stats().propuestasPendientes }}</strong>
          <span>Propuestas pendientes</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icono" style="background:#fdf4ff">⭐</div>
        <div class="stat-info">
          <strong>{{ stats().calificacion | number:'1.1-1' }}</strong>
          <span>Calificacion promedio</span>
        </div>
      </div>
    </div>

    <div class="grid-2">
      <div class="card">
        <div class="card-header">
          <span class="card-titulo">Contratos activos</span>
          <a routerLink="/freelancer/contratos" class="btn btn-contorno btn-sm">Ver todos</a>
        </div>
        @if (cargando()) {
          <div class="cargando-contenedor" style="padding:24px"><div class="spinner"></div></div>
        } @else if (contratos().length === 0) {
          <div class="estado-vacio" style="padding:24px">
            <span class="icono-vacio">📭</span>
            <h3>Sin contratos activos</h3>
            <a routerLink="/freelancer/explorar" class="btn btn-primario btn-sm" style="margin-top:12px">Explorar proyectos</a>
          </div>
        } @else {
          <div style="display:flex;flex-direction:column;gap:12px">
            @for (c of contratos().slice(0,4); track c.idContrato) {
              <a routerLink="/freelancer/contratos/{{ c.idContrato }}" style="display:block">
                <div class="proyecto-card" style="padding:14px">
                  <div class="proyecto-card-header">
                    <strong style="font-size:14px">{{ c.tituloProyecto }}</strong>
                    <span class="badge" [class]="badge(c.estado)">{{ c.estado }}</span>
                  </div>
                  <div class="proyecto-card-meta">
                    <span>Q {{ c.monto | number:'1.2-2' }}</span>
                    <span>{{ c.nombreCliente }}</span>
                  </div>
                  @if (c.estado === 'ENTREGA_PENDIENTE') {
                    <div class="alerta alerta-alerta" style="margin-top:8px;margin-bottom:0;font-size:12px">Entrega en revision por el cliente</div>
                  }
                </div>
              </a>
            }
          </div>
        }
      </div>

      <div class="card">
        <div class="card-header">
          <span class="card-titulo">Acciones rapidas</span>
        </div>
        <div style="display:flex;flex-direction:column;gap:12px">
          <a routerLink="/freelancer/explorar" class="btn btn-primario">🔍 Explorar proyectos abiertos</a>
          <a routerLink="/freelancer/propuestas" class="btn btn-secundario">📤 Ver mis propuestas</a>
          <a routerLink="/freelancer/contratos" class="btn btn-contorno">📝 Ver mis contratos</a>
          <a routerLink="/freelancer/reportes" class="btn btn-contorno">📊 Ver mis reportes</a>
        </div>
      </div>
    </div>
  `
})
export class FreelancerDashboardComponent implements OnInit {
  auth = inject(AuthService);
  private api = inject(ApiService);

  nombreCorto = computed(() => {
    const nombre = this.auth.usuario()?.nombreCompleto;
    return nombre ? nombre.split(' ')[0] : 'Freelancer';
  });

  cargando = signal(true);
  contratos = signal<any[]>([]);
  stats = signal({ contratosActivos: 0, propuestasPendientes: 0, calificacion: 0 });

  ngOnInit(): void {
    this.api.get<any>('/contratos').subscribe({
      next: res => {
        const lista = res.datos?.contratos ?? [];
        this.contratos.set(lista.filter((c: any) => ['EN_PROGRESO', 'ENTREGA_PENDIENTE'].includes(c.estado)));
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false)
    });
    this.api.get<any>('/propuestas').subscribe(res => {
      const pendientes = (res.datos?.propuestas ?? []).filter((p: any) => p.estado === 'PENDIENTE').length;
      this.stats.update(s => ({ ...s, propuestasPendientes: pendientes }));
    });
    this.api.get<any>('/freelancer/perfil').subscribe(res => {
      const prom = res.datos?.perfil?.calificacionProm ?? 0;
      const activos = this.contratos().length;
      this.stats.update(s => ({ ...s, calificacion: prom, contratosActivos: activos }));
    });
  }

  badge(e: string): string {
    const m: Record<string, string> = { EN_PROGRESO:'badge-azul', COMPLETADO:'badge-verde', CANCELADO:'badge-rojo', ENTREGA_PENDIENTE:'badge-naranja' };
    return m[e] ?? 'badge-gris';
  }
}
