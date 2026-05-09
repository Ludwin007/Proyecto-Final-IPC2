import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ToastService } from './core/services/toast.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule],
  template: `
    <router-outlet />
    <div class="toast-contenedor">
      @for (t of toast.toasts(); track t.id) {
        <div class="toast" [class]="'toast-' + t.tipo">
          <span>{{ t.tipo === 'exito' ? '✓' : t.tipo === 'error' ? '✕' : 'ℹ' }}</span>
          {{ t.mensaje }}
        </div>
      }
    </div>
  `
})
export class AppComponent {
  toast = inject(ToastService);
}
