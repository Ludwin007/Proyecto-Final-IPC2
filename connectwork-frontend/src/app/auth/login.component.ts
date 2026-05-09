import { Component, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/services/auth.service';
import { ToastService } from '../core/services/toast.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink, CommonModule],
  template: `
    <div class="login-pagina">
      <div class="login-lateral">
        <div class="login-lateral-contenido">
          <h1>ConnectWork</h1>
          <p>La plataforma que conecta a clientes con los mejores freelancers digitales de Guatemala.</p>
          <div class="login-stats">
            <div class="login-stat"><strong>500+</strong><span>Freelancers activos</span></div>
            <div class="login-stat"><strong>1,200+</strong><span>Proyectos completados</span></div>
            <div class="login-stat"><strong>98%</strong><span>Satisfaccion</span></div>
          </div>
        </div>
      </div>
      <div class="login-formulario">
        <div class="login-caja">
          <div class="login-encabezado">
            <h2>Bienvenido</h2>
            <p>Ingresa tus credenciales para continuar</p>
          </div>
          @if (error()) {
            <div class="alerta alerta-error">{{ error() }}</div>
          }
          <div class="campo">
            <label>Usuario</label>
            <input type="text" [(ngModel)]="username" placeholder="Ingresa tu nombre de usuario" (keyup.enter)="ingresar()" />
          </div>
          <div class="campo">
            <label>Contrasena</label>
            <input type="password" [(ngModel)]="contrasena" placeholder="Ingresa tu contrasena" (keyup.enter)="ingresar()" />
          </div>
          <button class="btn btn-primario" style="width:100%;justify-content:center;margin-top:8px" [disabled]="cargando()" (click)="ingresar()">
            {{ cargando() ? 'Ingresando...' : 'Ingresar' }}
          </button>
          <p style="text-align:center;margin-top:20px;color:var(--texto-suave)">
            ¿No tienes cuenta? <a routerLink="/registro" style="color:var(--destacado);font-weight:600">Registrate aqui</a>
          </p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-pagina { display: flex; min-height: 100vh; }
    .login-lateral {
      flex: 1; background: linear-gradient(135deg, var(--primario) 0%, var(--acento) 100%);
      display: flex; align-items: center; justify-content: center; padding: 48px;
    }
    .login-lateral-contenido { color: #fff; max-width: 440px; }
    .login-lateral-contenido h1 { font-size: 42px; font-weight: 800; color: var(--destacado); margin-bottom: 16px; }
    .login-lateral-contenido p { font-size: 18px; line-height: 1.7; color: rgba(255,255,255,.8); margin-bottom: 40px; }
    .login-stats { display: flex; gap: 32px; }
    .login-stat { text-align: center; }
    .login-stat strong { display: block; font-size: 28px; font-weight: 800; }
    .login-stat span { font-size: 12px; color: rgba(255,255,255,.6); }
    .login-formulario {
      width: 480px; display: flex; align-items: center;
      justify-content: center; padding: 48px; background: var(--fondo);
    }
    .login-caja { width: 100%; max-width: 380px; }
    .login-encabezado { margin-bottom: 32px; }
    .login-encabezado h2 { font-size: 28px; font-weight: 800; color: var(--primario); }
    .login-encabezado p { color: var(--texto-suave); margin-top: 6px; }
    @media (max-width: 768px) {
      .login-lateral { display: none; }
      .login-formulario { width: 100%; padding: 32px 24px; }
    }
  `]
})
export class LoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  username = '';
  contrasena = '';
  cargando = signal(false);
  error = signal('');

  ingresar(): void {
    if (!this.username.trim() || !this.contrasena.trim()) {
      this.error.set('Por favor completa todos los campos');
      return;
    }
    this.cargando.set(true);
    this.error.set('');
    this.auth.login(this.username.trim(), this.contrasena).subscribe({
      next: res => {
        this.cargando.set(false);
        if (res.exito) {
          const usuario = res.datos.usuario;
          if (!usuario.perfilCompleto) {
            const ruta = usuario.tipoRol === 'CLIENTE' ? '/completar-perfil/cliente' : '/completar-perfil/freelancer';
            this.router.navigate([ruta]);
          } else {
            const rutas: Record<string, string> = { ADMIN: '/admin', CLIENTE: '/cliente', FREELANCER: '/freelancer' };
            this.router.navigate([rutas[usuario.tipoRol] || '/login']);
          }
        } else {
          this.error.set(res.mensaje);
        }
      },
      error: () => { this.cargando.set(false); this.error.set('Error al conectar con el servidor'); }
    });
  }
}
