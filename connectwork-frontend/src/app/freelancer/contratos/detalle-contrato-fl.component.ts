import { Component, signal, inject, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { PollingService } from '../../core/services/polling.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-detalle-contrato-fl',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="topbar">
      <div class="topbar-titulo">
        <h2>Detalle del contrato</h2>
        <p>{{ contrato()?.tituloProyecto }}</p>
      </div>
      <a routerLink="/freelancer/contratos" class="btn btn-contorno">← Volver</a>
    </div>

    @if (cargando()) {
      <div class="cargando-contenedor"><div class="spinner"></div></div>
    } @else {
      <div class="grid-2" style="align-items:start">

        <div style="display:flex;flex-direction:column;gap:16px">
          <div class="card">
            <div class="card-header"><span class="card-titulo">Informacion del contrato</span></div>
            <div style="display:flex;flex-direction:column;gap:12px">
              <div style="display:flex;justify-content:space-between">
                <span style="color:var(--texto-suave)">Estado</span>
                <span class="badge" [class]="badgeContrato(contrato()?.estado)">{{ contrato()?.estado }}</span>
              </div>
              <div style="display:flex;justify-content:space-between">
                <span style="color:var(--texto-suave)">Cliente</span>
                <strong>{{ contrato()?.nombreCliente }}</strong>
              </div>
              <div style="display:flex;justify-content:space-between">
                <span style="color:var(--texto-suave)">Monto pactado</span>
                <strong>Q {{ contrato()?.monto | number:'1.2-2' }}</strong>
              </div>
              <div style="display:flex;justify-content:space-between">
                <span style="color:var(--texto-suave)">Comision plataforma</span>
                <span>{{ contrato()?.porcComision }}%</span>
              </div>
              <div style="display:flex;justify-content:space-between;padding-top:8px;border-top:1px solid var(--borde)">
                <span style="color:var(--texto-suave)">Recibiras</span>
                <strong style="font-size:18px;color:var(--verde)">Q {{ montoNeto() | number:'1.2-2' }}</strong>
              </div>
              <div style="display:flex;justify-content:space-between">
                <span style="color:var(--texto-suave)">Fecha inicio</span>
                <span>{{ contrato()?.fechaInicio | date:'dd/MM/yyyy' }}</span>
              </div>
            </div>
          </div>

          @if (contrato()?.estado === 'EN_PROGRESO') {
            <div class="card">
              <div class="card-header"><span class="card-titulo">Subir nueva entrega</span></div>
              @if (error()) {
                <div class="alerta alerta-error">{{ error() }}</div>
              }
              <div class="campo">
                <label>Descripcion del trabajo realizado</label>
                <textarea [(ngModel)]="formEntrega.descripcion" rows="5"
                  placeholder="Describe detalladamente lo que realizaste, los cambios implementados, instrucciones de uso, etc..."></textarea>
              </div>
              <div class="campo">
                <label>Archivos adjuntos (URL o referencia)</label>
                <input type="text" [(ngModel)]="formEntrega.archivosUrl"
                  placeholder="https://drive.google.com/... o GitHub, Dropbox, etc." />
                <p style="font-size:11px;color:var(--texto-suave);margin-top:4px">Puedes incluir multiples URLs separadas por coma</p>
              </div>
              <button class="btn btn-primario" style="width:100%;justify-content:center" [disabled]="subiendo()" (click)="subirEntrega()">
                {{ subiendo() ? 'Subiendo entrega...' : 'Subir entrega' }}
              </button>
            </div>
          }

          @if (contrato()?.estado === 'ENTREGA_PENDIENTE') {
            <div class="alerta alerta-info">
              <div>
                <strong>Entrega en revision</strong>
                <p>El cliente esta revisando tu entrega. Recibiras una notificacion cuando tome una decision.</p>
              </div>
            </div>
          }

          @if (contrato()?.estado === 'COMPLETADO') {
            <div class="alerta alerta-exito">
              <div>
                <strong>Contrato completado</strong>
                <p>El cliente aprobo tu entrega. El pago de Q {{ montoNeto() | number:'1.2-2' }} fue acreditado a tu saldo.</p>
              </div>
            </div>
          }
        </div>

        <div class="card">
          <div class="card-header">
            <span class="card-titulo">Historial de entregas</span>
          </div>
          @if (entregas().length === 0) {
            <div class="estado-vacio" style="padding:24px">
              <span class="icono-vacio">📦</span>
              <h3>Sin entregas aun</h3>
              <p>Sube tu primera entrega cuando hayas completado el trabajo</p>
            </div>
          } @else {
            <div style="display:flex;flex-direction:column;gap:14px">
              @for (e of entregas(); track e.idEntrega) {
                <div [style.borderLeft]="'4px solid ' + colorEntrega(e.estado)"
                     style="padding:14px;background:var(--fondo);border-radius:var(--radio-sm)">
                  <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px">
                    <strong>Entrega #{{ e.idEntrega }}</strong>
                    <span class="badge" [class]="badgeEntrega(e.estado)">{{ e.estado }}</span>
                  </div>
                  <p style="font-size:13px;margin-bottom:8px;line-height:1.5">{{ e.descripcion }}</p>
                  @if (e.archivosUrl) {
                    <p style="font-size:12px;color:var(--azul-claro)">
                      <strong>Archivos:</strong> {{ e.archivosUrl }}
                    </p>
                  }
                  <p style="font-size:12px;color:var(--texto-suave);margin-top:6px">{{ e.fechaSubida | date:'dd/MM/yyyy HH:mm' }}</p>
                  @if (e.motivoRechazo) {
                    <div class="alerta alerta-error" style="margin-top:10px;margin-bottom:0">
                      <div>
                        <strong>Motivo de rechazo:</strong>
                        <p>{{ e.motivoRechazo }}</p>
                      </div>
                    </div>
                  }
                  @if (e.estado === 'PENDIENTE') {
                    <div style="margin-top:12px">
                      <button class="btn btn-peligro btn-sm" [disabled]="procesando()" (click)="retirarEntrega(e)">Retirar entrega</button>
                    </div>
                  }
                </div>
              }
            </div>
          }
        </div>

      </div>
    }
  `
})
export class DetalleContratoFlComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private api = inject(ApiService);
  private toast = inject(ToastService);
  private polling = inject(PollingService);

  cargando = signal(true);
  subiendo = signal(false);
  procesando = signal(false);
  contrato = signal<any>(null);
  entregas = signal<any[]>([]);
  error = signal('');
  formEntrega = { descripcion: '', archivosUrl: '' };

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.cargar(id);
    this.polling.iniciar('contrato-fl', `/contratos/${id}`, 10000, (res: any) => {
      const anterior = this.contrato()?.estado;
      this.contrato.set(res.datos?.contrato);
      this.entregas.set(res.datos?.entregas ?? []);
      const nuevo = res.datos?.contrato?.estado;
      if (anterior && anterior !== nuevo) {
        this.toast.info(`El contrato cambio a estado: ${nuevo}`);
      }
    });
  }

  ngOnDestroy(): void {
    this.polling.detener('contrato-fl');
  }

  cargar(id: string): void {
    this.cargando.set(true);
    this.api.get<any>(`/contratos/${id}`).subscribe({
      next: res => {
        this.contrato.set(res.datos?.contrato);
        this.entregas.set(res.datos?.entregas ?? []);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false)
    });
  }

  montoNeto(): number {
    const c = this.contrato();
    if (!c) return 0;
    return c.monto - (c.monto * c.porcComision / 100);
  }

  subirEntrega(): void {
    this.error.set('');
    if (!this.formEntrega.descripcion || this.formEntrega.descripcion.length < 10) {
      this.error.set('La descripcion debe tener al menos 10 caracteres');
      return;
    }
    this.subiendo.set(true);
    const payload = { ...this.formEntrega, idContrato: this.contrato()?.idContrato };
    this.api.post<any>('/contratos/entrega', payload).subscribe({
      next: res => {
        this.subiendo.set(false);
        if (res.exito) {
          this.toast.exito(res.mensaje);
          this.formEntrega = { descripcion: '', archivosUrl: '' };
          this.cargar(this.contrato().idContrato);
        } else {
          this.error.set(res.mensaje);
        }
      },
      error: () => { this.subiendo.set(false); this.error.set('Error al subir la entrega'); }
    });
  }

  retirarEntrega(e: any): void {
    if (!confirm('¿Estas seguro de retirar esta entrega? El contrato volvera a estado EN_PROGRESO.')) return;
    this.procesando.set(true);
    this.api.put<any>(`/contratos/${this.contrato().idContrato}/entregas/${e.idEntrega}/retirar`, {}).subscribe({
      next: res => {
        this.procesando.set(false);
        if (res.exito) {
          this.toast.exito(res.mensaje);
          this.cargar(this.contrato().idContrato);
        } else {
          this.toast.error(res.mensaje);
        }
      },
      error: (err) => { this.procesando.set(false); this.toast.error(err?.error?.mensaje || 'Error al retirar la entrega'); }
    });
  }

  badgeContrato(e: string): string {
    const m: Record<string, string> = { EN_PROGRESO: 'badge-azul', COMPLETADO: 'badge-verde', CANCELADO: 'badge-rojo', ENTREGA_PENDIENTE: 'badge-naranja' };
    return m[e] ?? 'badge-gris';
  }

  badgeEntrega(e: string): string {
    const m: Record<string, string> = { PENDIENTE: 'badge-naranja', APROBADA: 'badge-verde', RECHAZADA: 'badge-rojo' };
    return m[e] ?? 'badge-gris';
  }

  colorEntrega(e: string): string {
    const m: Record<string, string> = { PENDIENTE: '#f59e0b', APROBADA: '#10b981', RECHAZADA: '#ef4444' };
    return m[e] ?? '#e2e8f0';
  }
}