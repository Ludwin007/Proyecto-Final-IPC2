import { Component, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../core/services/api.service';
import { ToastService } from '../core/services/toast.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [FormsModule, RouterLink, CommonModule],
  template: `
    <div class="registro-pagina">
      <div class="registro-cabecera">
        <h1>ConnectWork</h1>
        <p>Crea tu cuenta y empieza a colaborar</p>
      </div>
      <div class="registro-tarjeta">
        @if (error()) {
          <div class="alerta alerta-error">{{ error() }}</div>
        }
        <div class="tipo-selector">
          <button [class]="'tipo-btn' + (form.tipoRol === 'CLIENTE' ? ' activo' : '')" (click)="form.tipoRol = 'CLIENTE'">
            <span>👔</span> Soy Cliente
          </button>
          <button [class]="'tipo-btn' + (form.tipoRol === 'FREELANCER' ? ' activo' : '')" (click)="form.tipoRol = 'FREELANCER'">
            <span>💻</span> Soy Freelancer
          </button>
        </div>
        <div class="grid-2">
          <div class="campo">
            <label>Nombre completo</label>
            <input type="text" [(ngModel)]="form.nombreCompleto" placeholder="Tu nombre completo" />
          </div>
          <div class="campo">
            <label>Username</label>
            <input type="text" [(ngModel)]="form.username" placeholder="nombre_usuario" />
          </div>
        </div>
        <div class="grid-2">
          <div class="campo">
            <label>Correo electronico</label>
            <input type="email" [(ngModel)]="form.correo" placeholder="correo@ejemplo.com" />
          </div>
          <div class="campo">
            <label>Contrasena</label>
            <input type="password" [(ngModel)]="form.contrasena" placeholder="Minimo 8 caracteres" />
          </div>
        </div>
        <div class="grid-2">
          <div class="campo">
            <label>Telefono</label>
            <input type="text" [(ngModel)]="form.telefono" placeholder="5555-5555" />
          </div>
          <div class="campo">
            <label>CUI</label>
            <input type="text" [(ngModel)]="form.cui" placeholder="0000 00000 0101" />
          </div>
        </div>
        <div class="grid-2">
          <div class="campo">
            <label>Fecha de nacimiento</label>
            <input type="date" [(ngModel)]="form.fechaNacimiento" />
          </div>
          <div class="campo">
            <label>Direccion</label>
            <input type="text" [(ngModel)]="form.direccion" placeholder="Ciudad, departamento" />
          </div>
        </div>
        <button class="btn btn-primario" style="width:100%;justify-content:center;margin-top:8px" [disabled]="cargando()" (click)="registrar()">
          {{ cargando() ? 'Registrando...' : 'Crear cuenta' }}
        </button>
        <p style="text-align:center;margin-top:16px;color:var(--texto-suave)">
          ¿Ya tienes cuenta? <a routerLink="/login" style="color:var(--destacado);font-weight:600">Inicia sesion</a>
        </p>
      </div>
    </div>
  `,
  styles: [`
    .registro-pagina { min-height: 100vh; background: linear-gradient(135deg, var(--primario) 0%, var(--acento) 100%); display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 32px 16px; }
    .registro-cabecera { text-align: center; color: #fff; margin-bottom: 24px; }
    .registro-cabecera h1 { font-size: 36px; font-weight: 800; color: var(--destacado); }
    .registro-cabecera p { margin-top: 6px; color: rgba(255,255,255,.7); font-size: 16px; }
    .registro-tarjeta { background: #fff; border-radius: var(--radio-xl); padding: 36px; width: 100%; max-width: 700px; box-shadow: var(--sombra-lg); }
    .tipo-selector { display: flex; gap: 12px; margin-bottom: 24px; }
    .tipo-btn { flex: 1; padding: 14px; border: 2px solid var(--borde); border-radius: var(--radio); background: var(--fondo); font-size: 15px; font-weight: 600; cursor: pointer; transition: var(--transicion); display: flex; align-items: center; justify-content: center; gap: 8px; }
    .tipo-btn.activo { border-color: var(--destacado); background: #fff5f7; color: var(--destacado); }
    .tipo-btn:hover:not(.activo) { border-color: var(--acento); }
  `]
})
export class RegistroComponent {
  private api = inject(ApiService);
  private router = inject(Router);
  private toast = inject(ToastService);

  cargando = signal(false);
  error = signal('');

  form = {
    tipoRol: 'CLIENTE',
    nombreCompleto: '',
    username: '',
    contrasena: '',
    correo: '',
    telefono: '',
    direccion: '',
    cui: '',
    fechaNacimiento: ''
  };

  registrar(): void {
    this.error.set('');
    if (!this.form.nombreCompleto || !this.form.username || !this.form.contrasena || !this.form.correo) {
      this.error.set('Por favor completa todos los campos obligatorios');
      return;
    }
    this.cargando.set(true);
    this.api.post<any>('/auth/registro', this.form).subscribe({
      next: res => {
        this.cargando.set(false);
        if (res.exito) {
          this.toast.exito('Cuenta creada exitosamente. Ahora puedes ingresar');
          this.router.navigate(['/login']);
        } else {
          this.error.set(res.mensaje);
        }
      },
      error: () => { this.cargando.set(false); this.error.set('Error al conectar con el servidor'); }
    });
  }
}
