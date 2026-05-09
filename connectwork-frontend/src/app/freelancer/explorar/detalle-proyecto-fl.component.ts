import { Component, signal, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-detalle-proyecto-fl',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="topbar">
      <div class="topbar-titulo">
        <h2>{{ proyecto()?.titulo }}</h2>
        <p>{{ proyecto()?.nombreCategoria }}</p>
      </div>
      <a routerLink="/freelancer/explorar" class="btn btn-contorno">← Volver</a>
    </div>

    @if (cargando()) {
      <div class="cargando-contenedor"><div class="spinner"></div></div>
    } @else {
      <div class="grid-2" style="align-items:start">
        <div class="card">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
            <span class="badge badge-verde">ABIERTO</span>
            <strong style="font-size:22px;color:var(--verde)">Q {{ proyecto()?.presupuestoMax | number:'1.2-2' }}</strong>
          </div>
          <p style="color:var(--texto-suave);line-height:1.7;margin-bottom:16px">{{ proyecto()?.descripcion }}</p>
          <div class="proyecto-card-meta" style="margin-bottom:16px">
            <span>👤 {{ proyecto()?.nombreCliente }}</span>
            <span>📅 Limite: {{ proyecto()?.fechaLimite | date:'dd/MM/yyyy' }}</span>
            <span>📌 {{ proyecto()?.fechaPublicacion | date:'dd/MM/yyyy' }}</span>
          </div>
          @if (proyecto()?.habilidades?.length > 0) {
            <div>
              <p style="font-size:12px;font-weight:600;color:var(--texto-suave);text-transform:uppercase;letter-spacing:.5px;margin-bottom:8px">Habilidades requeridas</p>
              <div class="chips-lista">
                @for (h of proyecto()?.habilidades; track h.idHabilidad) {
                  <span class="chip">{{ h.nombre }}</span>
                }
              </div>
            </div>
          }
        </div>

        <div class="card">
          @if (propuestaEnviada()) {
            <div class="alerta alerta-info" style="margin-bottom:0">
              <div>
                <strong>Ya enviaste una propuesta</strong>
                <p style="margin-top:6px">Tu propuesta esta en estado: <strong>{{ propuestaEnviada()?.estado }}</strong></p>
                <p>Monto ofertado: Q {{ propuestaEnviada()?.montoOfertado | number:'1.2-2' }}</p>
                @if (propuestaEnviada()?.estado === 'PENDIENTE') {
                  <button class="btn btn-peligro btn-sm" style="margin-top:12px" [disabled]="procesando()" (click)="retirar()">
                    Retirar propuesta
                  </button>
                }
              </div>
            </div>
          } @else {
            <div class="card-header"><span class="card-titulo">Enviar propuesta</span></div>
            @if (error()) {
              <div class="alerta alerta-error">{{ error() }}</div>
            }
            <div class="campo">
              <label>Monto ofertado (Q)</label>
              <input type="number" [(ngModel)]="form.montoOfertado" [max]="proyecto()?.presupuestoMax" min="0" step="0.01"
                     [placeholder]="'Max: Q ' + (proyecto()?.presupuestoMax | number:'1.2-2')" />
              <p style="font-size:11px;color:var(--texto-suave);margin-top:4px">Presupuesto maximo: Q {{ proyecto()?.presupuestoMax | number:'1.2-2' }}</p>
            </div>
            <div class="campo">
              <label>Plazo de entrega (dias)</label>
              <input type="number" [(ngModel)]="form.plazoDias" min="1" placeholder="Ej: 14" />
            </div>
            <div class="campo">
              <label>Carta de presentacion</label>
              <textarea [(ngModel)]="form.cartaPresentacion" rows="5"
                        placeholder="Describe como abordaras el proyecto, tu experiencia relevante y por que eres la mejor opcion..."></textarea>
            </div>
            <button class="btn btn-primario" style="width:100%;justify-content:center" [disabled]="enviando()" (click)="enviar()">
              {{ enviando() ? 'Enviando...' : 'Enviar propuesta' }}
            </button>
          }
        </div>
      </div>
    }
  `
})
export class DetalleProyectoFlComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private api = inject(ApiService);
  private router = inject(Router);
  private toast = inject(ToastService);

  cargando = signal(true);
  enviando = signal(false);
  procesando = signal(false);
  proyecto = signal<any>(null);
  propuestaEnviada = signal<any>(null);
  error = signal('');
  form = { montoOfertado: 0, plazoDias: 0, cartaPresentacion: '' };

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.api.get<any>(`/proyectos/${id}`).subscribe({
      next: res => {
        this.proyecto.set(res.datos?.proyecto);
        this.cargando.set(false);
        this.verificarPropuesta(Number(id));
      },
      error: () => this.cargando.set(false)
    });
  }

  verificarPropuesta(idProyecto: number): void {
    this.api.get<any>('/propuestas').subscribe(res => {
      const lista = res.datos?.propuestas ?? [];
      const encontrada = lista.find((p: any) => p.idProyecto === idProyecto);
      this.propuestaEnviada.set(encontrada ?? null);
    });
  }

  enviar(): void {
    this.error.set('');
    if (!this.form.montoOfertado || this.form.montoOfertado <= 0) { this.error.set('El monto debe ser mayor a cero'); return; }
    if (this.form.montoOfertado > this.proyecto()?.presupuestoMax) { this.error.set('El monto no puede superar el presupuesto maximo'); return; }
    if (!this.form.plazoDias || this.form.plazoDias <= 0) { this.error.set('El plazo debe ser mayor a cero'); return; }
    if (!this.form.cartaPresentacion.trim()) { this.error.set('La carta de presentacion es obligatoria'); return; }

    this.enviando.set(true);
    const payload = { ...this.form, idProyecto: this.proyecto()?.idProyecto };
    this.api.post<any>('/propuestas', payload).subscribe({
      next: res => {
        this.enviando.set(false);
        if (res.exito) { this.toast.exito('Propuesta enviada exitosamente'); this.router.navigate(['/freelancer/propuestas']); }
        else this.error.set(res.mensaje);
      },
      error: () => { this.enviando.set(false); this.error.set('Error al enviar la propuesta'); }
    });
  }

  retirar(): void {
    if (!confirm('¿Retirar tu propuesta de este proyecto?')) return;
    this.procesando.set(true);
    this.api.put<any>(`/propuestas/${this.propuestaEnviada()?.idPropuesta}/retirar`).subscribe({
      next: res => {
        this.procesando.set(false);
        if (res.exito) { this.toast.exito('Propuesta retirada'); this.propuestaEnviada.set(null); }
        else this.toast.error(res.mensaje);
      },
      error: () => { this.procesando.set(false); this.toast.error('Error al retirar'); }
    });
  }
}
