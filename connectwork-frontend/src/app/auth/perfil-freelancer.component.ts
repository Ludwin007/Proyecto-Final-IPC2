import { Component, signal, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../core/services/api.service';
import { AuthService } from '../core/services/auth.service';
import { ToastService } from '../core/services/toast.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-perfil-freelancer',
  standalone: true,
  imports: [FormsModule, CommonModule],
  template: `
    <div class="perfil-pagina">
      <div class="perfil-tarjeta">
        <div class="perfil-icono">💻</div>
        <h2>Completa tu perfil de freelancer</h2>
        <p>Esta informacion ayudara a los clientes a encontrarte y evaluar tu experiencia</p>
        @if (error()) {
          <div class="alerta alerta-error" style="margin-top:16px">{{ error() }}</div>
        }
        <div class="campo" style="margin-top:24px;text-align:left">
          <label>Biografia profesional</label>
          <textarea [(ngModel)]="form.biografia" rows="4" placeholder="Describe tu experiencia, especialidades y que te hace diferente..."></textarea>
        </div>
        <div class="grid-2" style="text-align:left">
          <div class="campo">
            <label>Nivel de experiencia</label>
            <select [(ngModel)]="form.nivelExperiencia">
              <option value="">Seleccionar...</option>
              <option value="JUNIOR">Junior</option>
              <option value="SEMI_SENIOR">Semi-Senior</option>
              <option value="SENIOR">Senior</option>
            </select>
          </div>
          <div class="campo">
            <label>Tarifa por hora (Q)</label>
            <input type="number" [(ngModel)]="form.tarifaHora" placeholder="0.00" min="0" step="0.01" />
          </div>
        </div>
        <div class="campo" style="text-align:left">
          <label>Habilidades que posees</label>
          @if (cargandoHabilidades()) {
            <div style="color:var(--texto-suave);padding:8px">Cargando habilidades...</div>
          } @else {
            <div class="habilidades-grid">
              @for (h of habilidades(); track h.idHabilidad) {
                <label class="hab-check" [class.seleccionada]="seleccionadas().includes(h.idHabilidad)">
                  <input type="checkbox" [checked]="seleccionadas().includes(h.idHabilidad)" (change)="toggleHabilidad(h.idHabilidad)" />
                  {{ h.nombre }}
                </label>
              }
            </div>
          }
          @if (seleccionadas().length > 0) {
            <div class="chips-lista" style="margin-top:12px">
              @for (id of seleccionadas(); track id) {
                <span class="chip">{{ nombreHabilidad(id) }}</span>
              }
            </div>
          }
        </div>
        <button class="btn btn-primario btn-lg" style="width:100%;justify-content:center;margin-top:16px;text-align:center" [disabled]="cargando()" (click)="guardar()">
          {{ cargando() ? 'Guardando...' : 'Guardar y continuar' }}
        </button>
      </div>
    </div>
  `,
  styles: [`
    .perfil-pagina { min-height: 100vh; background: linear-gradient(135deg, var(--primario) 0%, var(--acento) 100%); display: flex; align-items: center; justify-content: center; padding: 32px 16px; }
    .perfil-tarjeta { background: #fff; border-radius: var(--radio-xl); padding: 40px; width: 100%; max-width: 680px; text-align: center; box-shadow: var(--sombra-lg); }
    .perfil-icono { font-size: 56px; margin-bottom: 16px; }
    .perfil-tarjeta h2 { font-size: 24px; font-weight: 800; color: var(--primario); margin-bottom: 8px; }
    .perfil-tarjeta p { color: var(--texto-suave); }
    .habilidades-grid { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 10px; }
    .hab-check { display: flex; align-items: center; gap: 6px; padding: 6px 12px; border: 2px solid var(--borde); border-radius: 20px; cursor: pointer; font-size: 13px; font-weight: 500; transition: var(--transicion); }
    .hab-check.seleccionada { border-color: var(--acento); background: #eff6ff; color: var(--acento); }
    .hab-check input { display: none; }
    .hab-check:hover { border-color: var(--acento); }
  `]
})
export class PerfilFreelancerComponent implements OnInit {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  cargando = signal(false);
  cargandoHabilidades = signal(true);
  error = signal('');
  habilidades = signal<any[]>([]);
  seleccionadas = signal<number[]>([]);

  form = { biografia: '', nivelExperiencia: '', tarifaHora: 0 };

  ngOnInit(): void {
    this.api.get<any>('/catalogo/habilidades').subscribe({
      next: res => { this.habilidades.set(res.datos?.habilidades ?? []); this.cargandoHabilidades.set(false); },
      error: () => this.cargandoHabilidades.set(false)
    });
  }

  toggleHabilidad(id: number): void {
    const lista = this.seleccionadas();
    if (lista.includes(id)) {
      this.seleccionadas.set(lista.filter(x => x !== id));
    } else {
      this.seleccionadas.set([...lista, id]);
    }
  }

  nombreHabilidad(id: number): string {
    return this.habilidades().find(h => h.idHabilidad === id)?.nombre ?? '';
  }

  guardar(): void {
    if (!this.form.biografia.trim()) { this.error.set('La biografia es obligatoria'); return; }
    if (!this.form.nivelExperiencia) { this.error.set('Selecciona tu nivel de experiencia'); return; }
    if (!this.form.tarifaHora || this.form.tarifaHora <= 0) { this.error.set('La tarifa por hora debe ser mayor a cero'); return; }
    if (this.seleccionadas().length === 0) { this.error.set('Selecciona al menos una habilidad'); return; }
    this.cargando.set(true);
    this.error.set('');
    this.api.post<any>('/auth/perfil-freelancer', { perfil: this.form, idHabilidades: this.seleccionadas() }).subscribe({
      next: res => {
        this.cargando.set(false);
        if (res.exito) {
          this.auth.actualizarPerfilCompleto();
          this.toast.exito('Perfil completado exitosamente');
          this.router.navigate(['/freelancer/dashboard']);
        } else {
          this.error.set(res.mensaje);
        }
      },
      error: () => { this.cargando.set(false); this.error.set('Error al guardar el perfil'); }
    });
  }
}
