import { Component, signal, inject, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { PollingService } from '../../core/services/polling.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-detalle-contrato-cliente',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="topbar">
      <div class="topbar-titulo">
        <h2>Detalle del contrato</h2>
        <p>{{ contrato()?.tituloProyecto }}</p>
      </div>
      <a routerLink="/cliente/contratos" class="btn btn-contorno">← Volver</a>
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
                <span style="color:var(--texto-suave)">Freelancer</span>
                <strong>{{ contrato()?.nombreFreelancer }}</strong>
              </div>
              <div style="display:flex;justify-content:space-between">
                <span style="color:var(--texto-suave)">Monto del contrato</span>
                <strong>Q {{ contrato()?.monto | number:'1.2-2' }}</strong>
              </div>
              <div style="display:flex;justify-content:space-between">
                <span style="color:var(--texto-suave)">Comision plataforma</span>
                <span>{{ contrato()?.porcComision }}%</span>
              </div>
              <div style="display:flex;justify-content:space-between">
                <span style="color:var(--texto-suave)">Monto al freelancer</span>
                <strong style="color:var(--verde)">Q {{ montoNeto() | number:'1.2-2' }}</strong>
              </div>
              <div style="display:flex;justify-content:space-between">
                <span style="color:var(--texto-suave)">Fecha inicio</span>
                <span>{{ contrato()?.fechaInicio | date:'dd/MM/yyyy' }}</span>
              </div>
              @if (contrato()?.fechaFin) {
                <div style="display:flex;justify-content:space-between">
                  <span style="color:var(--texto-suave)">Fecha fin</span>
                  <span>{{ contrato()?.fechaFin | date:'dd/MM/yyyy' }}</span>
                </div>
              }
            </div>
          </div>

          @if (calificacion()) {
            <div class="card">
              <div class="card-header" style="display:flex;justify-content:space-between;align-items:center">
                <span class="card-titulo">Calificacion emitida</span>
                <button class="btn btn-peligro btn-sm" (click)="eliminarCalificacion()">Eliminar</button>
              </div>
              <div class="estrellas">
                @for (i of [1,2,3,4,5]; track i) {
                  <span class="estrella" [class.activa]="i <= calificacion().puntuacion">★</span>
                }
              </div>
              <p style="margin-top:8px;color:var(--texto-suave)">{{ calificacion().comentario }}</p>
            </div>
          }

          @if (contrato()?.estado === 'COMPLETADO' && !calificacion()) {
            <div class="card">
              <div class="card-header"><span class="card-titulo">Calificar al freelancer</span></div>
              <div class="estrellas" style="margin-bottom:12px">
                @for (i of [1,2,3,4,5]; track i) {
                  <span class="estrella" [class.activa]="i <= puntuacion()" (click)="puntuacion.set(i)" style="cursor:pointer">★</span>
                }
              </div>
              <div class="campo">
                <label>Comentario (opcional)</label>
                <textarea [(ngModel)]="comentario" rows="3" placeholder="Describe tu experiencia con este freelancer..."></textarea>
              </div>
              <button class="btn btn-primario" style="width:100%;justify-content:center" [disabled]="calificando() || puntuacion() === 0" (click)="calificar()">
                {{ calificando() ? 'Enviando...' : 'Enviar calificacion' }}
              </button>
            </div>
          }
        </div>

        <div style="display:flex;flex-direction:column;gap:16px">
          <div class="card">
            <div class="card-header">
              <span class="card-titulo">Historial de entregas</span>
            </div>
            @if (entregas().length === 0) {
              <div class="estado-vacio" style="padding:24px">
                <span class="icono-vacio">📦</span>
                <h3>Sin entregas aun</h3>
                <p>El freelancer subira sus entregas aqui</p>
              </div>
            } @else {
              <div style="display:flex;flex-direction:column;gap:16px">
                @for (e of entregas(); track e.idEntrega) {
                  <div [style.borderLeft]="'4px solid ' + colorEntrega(e.estado)"
                       style="padding:16px;background:var(--fondo);border-radius:var(--radio-sm)">
                    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:10px">
                      <strong style="font-size:14px">Entrega #{{ e.idEntrega }}</strong>
                      <span class="badge" [class]="badgeEntrega(e.estado)">{{ e.estado }}</span>
                    </div>
                    <p style="font-size:13px;color:var(--texto-suave);margin-bottom:10px">{{ e.descripcion }}</p>
                    @if (e.archivosUrl) {
                      <p style="font-size:12px"><strong>Archivos:</strong> {{ e.archivosUrl }}</p>
                    }
                    <p style="font-size:12px;color:var(--texto-suave)">{{ e.fechaSubida | date:'dd/MM/yyyy HH:mm' }}</p>
                    @if (e.motivoRechazo) {
                      <div class="alerta alerta-error" style="margin-top:8px;margin-bottom:0">
                        <strong>Motivo de rechazo:</strong> {{ e.motivoRechazo }}
                      </div>
                    }
                    @if (e.estado === 'PENDIENTE') {
                      <div style="display:flex;gap:8px;margin-top:12px;flex-wrap:wrap">
                        <button class="btn btn-exito btn-sm" [disabled]="procesando()" (click)="aprobar(e)">✓ Aprobar entrega</button>
                        <button class="btn btn-alerta btn-sm" [disabled]="procesando()" (click)="abrirRechazo(e)">✕ Rechazar</button>
                        <button class="btn btn-peligro btn-sm" [disabled]="procesando()" (click)="abrirCancelacion()">Cancelar contrato</button>
                      </div>
                    }
                  </div>
                }
              </div>
            }
          </div>
        </div>
      </div>
    }

    @if (modalRechazo()) {
      <div class="overlay-modal" (click)="modalRechazo.set(false)">
        <div class="modal" (click)="$event.stopPropagation()">
          <div class="modal-cabecera">
            <h3>Rechazar entrega</h3>
            <button class="btn-cerrar" (click)="modalRechazo.set(false)">✕</button>
          </div>
          <div class="campo">
            <label>Motivo del rechazo (obligatorio)</label>
            <textarea [(ngModel)]="motivoAccion" rows="4" placeholder="Explica al freelancer que debe corregir..."></textarea>
          </div>
          <div class="modal-acciones">
            <button class="btn btn-contorno" (click)="modalRechazo.set(false)">Cancelar</button>
            <button class="btn btn-peligro" [disabled]="procesando()" (click)="rechazar()">
              {{ procesando() ? 'Procesando...' : 'Rechazar entrega' }}
            </button>
          </div>
        </div>
      </div>
    }

    @if (modalCancelacion()) {
      <div class="overlay-modal" (click)="modalCancelacion.set(false)">
        <div class="modal" (click)="$event.stopPropagation()">
          <div class="modal-cabecera">
            <h3>Cancelar contrato</h3>
            <button class="btn-cerrar" (click)="modalCancelacion.set(false)">✕</button>
          </div>
          <div class="alerta alerta-alerta">Al cancelar el contrato el monto bloqueado sera devuelto a tu saldo disponible.</div>
          <div class="campo">
            <label>Motivo de cancelacion (obligatorio)</label>
            <textarea [(ngModel)]="motivoAccion" rows="4" placeholder="Indica por que cancelas el contrato..."></textarea>
          </div>
          <div class="modal-acciones">
            <button class="btn btn-contorno" (click)="modalCancelacion.set(false)">Volver</button>
            <button class="btn btn-peligro" [disabled]="procesando()" (click)="cancelar()">
              {{ procesando() ? 'Procesando...' : 'Cancelar contrato' }}
            </button>
          </div>
        </div>
      </div>
    }
  `
})
export class DetalleContratoClienteComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private api = inject(ApiService);
  private router = inject(Router);
  private auth = inject(AuthService);
  private toast = inject(ToastService);
  private polling = inject(PollingService);

  cargando = signal(true);
  procesando = signal(false);
  calificando = signal(false);
  contrato = signal<any>(null);
  entregas = signal<any[]>([]);
  calificacion = signal<any>(null);
  modalRechazo = signal(false);
  modalCancelacion = signal(false);
  puntuacion = signal(0);
  comentario = '';
  motivoAccion = '';
  entregaActual: any = null;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.cargar(id);
    this.polling.iniciar('contrato-cliente', `/contratos/${id}`, 10000, (res: any) => {
      const anterior = this.contrato()?.estado;
      this.contrato.set(res.datos?.contrato);
      this.entregas.set(res.datos?.entregas ?? []);
      this.calificacion.set(res.datos?.calificacion ?? null);
      const nuevo = res.datos?.contrato?.estado;
      if (anterior && anterior !== nuevo) {
        this.toast.info(`El contrato cambio a estado: ${nuevo}`);
      }
    });
  }

  ngOnDestroy(): void {
    this.polling.detener('contrato-cliente');
  }

  cargar(id: string): void {
    this.cargando.set(true);
    this.api.get<any>(`/contratos/${id}`).subscribe({
      next: res => {
        this.contrato.set(res.datos?.contrato);
        this.entregas.set(res.datos?.entregas ?? []);
        this.calificacion.set(res.datos?.calificacion ?? null);
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

  abrirRechazo(e: any): void { this.entregaActual = e; this.motivoAccion = ''; this.modalRechazo.set(true); }
  abrirCancelacion(): void { this.motivoAccion = ''; this.modalCancelacion.set(true); }

  aprobar(e: any): void {
    if (!confirm('¿Aprobar esta entrega y liberar el pago al freelancer?')) return;
    this.procesando.set(true);
    this.api.put<any>(`/contratos/${this.contrato().idContrato}/entregas/${e.idEntrega}/aprobar`, {}).subscribe({
      next: res => {
        this.procesando.set(false);
        if (res.exito) { 
          this.toast.exito(res.mensaje); 
          if (res.nuevoSaldoCliente !== undefined) {
            this.auth.actualizarSaldo(res.nuevoSaldoCliente);
          }
          this.cargar(this.contrato().idContrato); 
        }
        else this.toast.error(res.mensaje);
      },
      error: (err) => { 
        this.procesando.set(false); 
        this.toast.error(err?.error?.mensaje || 'Error al aprobar'); 
      }
    });
  }

  rechazar(): void {
    if (!this.motivoAccion.trim()) { this.toast.error('El motivo es obligatorio'); return; }
    this.procesando.set(true);
    this.api.put<any>(`/contratos/${this.contrato().idContrato}/entregas/${this.entregaActual.idEntrega}/rechazar`, { motivo: this.motivoAccion }).subscribe({
      next: res => {
        this.procesando.set(false);
        this.modalRechazo.set(false);
        if (res.exito) { this.toast.exito(res.mensaje); this.cargar(this.contrato().idContrato); }
        else this.toast.error(res.mensaje);
      },
      error: (err) => { 
        this.procesando.set(false); 
        this.modalRechazo.set(false);
        this.toast.error(err?.error?.mensaje || 'Error al rechazar'); 
      }
    });
  }

  cancelar(): void {
    if (!this.motivoAccion.trim()) { this.toast.error('El motivo es obligatorio'); return; }
    this.procesando.set(true);
    this.api.put<any>(`/contratos/${this.contrato().idContrato}/cancelar`, { motivo: this.motivoAccion }).subscribe({
      next: res => {
        this.procesando.set(false);
        this.modalCancelacion.set(false);
        if (res.exito) { this.toast.exito(res.mensaje); this.router.navigate(['/cliente/contratos']); }
        else this.toast.error(res.mensaje);
      },
      error: (err) => { 
        this.procesando.set(false); 
        this.modalCancelacion.set(false);
        this.toast.error(err?.error?.mensaje || 'Error al cancelar'); 
      }
    });
  }

  calificar(): void {
    if (this.puntuacion() === 0) return;
    this.calificando.set(true);
    this.api.post<any>('/contratos/calificar', { idContrato: this.contrato().idContrato, puntuacion: this.puntuacion(), comentario: this.comentario }).subscribe({
      next: res => {
        this.calificando.set(false);
        if (res.exito) { this.toast.exito('Calificacion enviada'); this.cargar(this.contrato().idContrato); }
        else this.toast.error(res.mensaje);
      },
      error: () => { this.calificando.set(false); this.toast.error('Error al calificar'); }
    });
  }

  eliminarCalificacion(): void {
    if (!confirm('¿Estas seguro de eliminar tu calificacion? Esto tambien actualizara el promedio del freelancer.')) return;
    this.procesando.set(true);
    this.api.put<any>(`/contratos/${this.contrato().idContrato}/eliminar-calificacion`, {}).subscribe({
      next: res => {
        this.procesando.set(false);
        if (res.exito) { this.toast.exito(res.mensaje); this.cargar(this.contrato().idContrato); }
        else this.toast.error(res.mensaje);
      },
      error: (err) => { 
        this.procesando.set(false); 
        this.toast.error(err?.error?.mensaje || 'Error al eliminar calificacion'); 
      }
    });
  }

  badgeContrato(e: string): string {
    const m: Record<string, string> = { EN_PROGRESO:'badge-azul', COMPLETADO:'badge-verde', CANCELADO:'badge-rojo', ENTREGA_PENDIENTE:'badge-naranja' };
    return m[e] ?? 'badge-gris';
  }

  badgeEntrega(e: string): string {
    const m: Record<string, string> = { PENDIENTE:'badge-naranja', APROBADA:'badge-verde', RECHAZADA:'badge-rojo' };
    return m[e] ?? 'badge-gris';
  }

  colorEntrega(e: string): string {
    const m: Record<string, string> = { PENDIENTE:'#f59e0b', APROBADA:'#10b981', RECHAZADA:'#ef4444' };
    return m[e] ?? '#e2e8f0';
  }
}