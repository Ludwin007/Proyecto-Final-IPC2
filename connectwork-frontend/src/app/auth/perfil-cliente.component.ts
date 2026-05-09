import { Component, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../core/services/api.service';
import { AuthService } from '../core/services/auth.service';
import { ToastService } from '../core/services/toast.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-perfil-cliente',
  standalone: true,
  imports: [FormsModule, CommonModule],
  template: `
    <div class="perfil-pagina">
      <div class="perfil-tarjeta">
        <div class="perfil-icono">👔</div>
        <h2>Completa tu perfil de cliente</h2>
        <p>Necesitamos un poco mas de informacion antes de que puedas publicar proyectos</p>
        @if (error()) {
          <div class="alerta alerta-error" style="margin-top:16px">{{ error() }}</div>
        }
        <div class="campo" style="margin-top:24px">
          <label>Descripcion de tu empresa o perfil</label>
          <textarea [(ngModel)]="form.descripcion" rows="4" placeholder="Describe brevemente tu empresa, a que se dedica y que tipo de servicios digitales necesitas..."></textarea>
        </div>
        <div class="campo">
          <label>Sector o industria</label>
          <input type="text" [(ngModel)]="form.sector" placeholder="Ej: Tecnologia, Salud, Educacion, Comercio..." />
        </div>
        <div class="campo">
          <label>Sitio web (opcional)</label>
          <input type="url" [(ngModel)]="form.sitioWeb" placeholder="https://tuempresa.com" />
        </div>
        <button class="btn btn-primario btn-lg" style="width:100%;justify-content:center;margin-top:8px" [disabled]="cargando()" (click)="guardar()">
          {{ cargando() ? 'Guardando...' : 'Guardar y continuar' }}
        </button>
      </div>
    </div>
  `,
  styles: [`
    .perfil-pagina { min-height: 100vh; background: linear-gradient(135deg, var(--primario) 0%, var(--acento) 100%); display: flex; align-items: center; justify-content: center; padding: 32px 16px; }
    .perfil-tarjeta { background: #fff; border-radius: var(--radio-xl); padding: 40px; width: 100%; max-width: 560px; text-align: center; box-shadow: var(--sombra-lg); }
    .perfil-icono { font-size: 56px; margin-bottom: 16px; }
    .perfil-tarjeta h2 { font-size: 24px; font-weight: 800; color: var(--primario); margin-bottom: 8px; }
    .perfil-tarjeta p { color: var(--texto-suave); margin-bottom: 8px; }
    .campo { text-align: left; }
  `]
})
export class PerfilClienteComponent {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  cargando = signal(false);
  error = signal('');
  form = { descripcion: '', sector: '', sitioWeb: '' };

  guardar(): void {
    if (!this.form.descripcion.trim() || !this.form.sector.trim()) {
      this.error.set('La descripcion y el sector son obligatorios');
      return;
    }
    this.cargando.set(true);
    this.error.set('');
    this.api.post<any>('/auth/perfil-cliente', this.form).subscribe({
      next: res => {
        this.cargando.set(false);
        if (res.exito) {
          this.auth.actualizarPerfilCompleto();
          this.toast.exito('Perfil completado exitosamente');
          this.router.navigate(['/cliente/dashboard']);
        } else {
          this.error.set(res.mensaje);
        }
      },
      error: () => { this.cargando.set(false); this.error.set('Error al guardar el perfil'); }
    });
  }
}
