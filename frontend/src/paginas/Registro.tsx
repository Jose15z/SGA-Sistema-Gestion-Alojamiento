import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router';
import { Campo } from '../componentes/Campo';
import { Icono } from '../componentes/Icono';
import { esquemaRegistro, type FormularioRegistro } from '../esquemas/usuario';
import { iniciarSesion, registrarHuesped } from '../servicios/authServicio';
import { guardarSesion } from '../servicios/sesion';
import { hoyISO } from '../utilidades/fechas';
import { atributosDeError, mostrarErrorDelServidor } from '../utilidades/formularios';

const CAMPOS = ['nombreCompleto', 'correo', 'documento', 'telefono', 'fechaNacimiento', 'contrasena'] as const;

export default function Registro() {
  const navegar = useNavigate();
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormularioRegistro>({
    resolver: zodResolver(esquemaRegistro),
    mode: 'onTouched',
    defaultValues: {
      nombreCompleto: '',
      correo: '',
      documento: '',
      telefono: '',
      fechaNacimiento: '',
      contrasena: '',
      confirmacion: '',
    },
  });

  async function alEnviar({ confirmacion: _confirmacion, ...datos }: FormularioRegistro) {
    try {
      // El registro público no envía rol: el backend siempre crea un HUESPED.
      await registrarHuesped(datos);
      // Con la cuenta creada, se inicia sesión de una vez y el huésped llega a buscar apartamentos.
      const sesion = await iniciarSesion({ correo: datos.correo, contrasena: datos.contrasena });
      guardarSesion(sesion, true);
      navegar('/apartamentos', { replace: true });
    } catch (error) {
      mostrarErrorDelServidor(error, setError, CAMPOS);
    }
  }

  return (
    <>
      <div className="auth-heading">
        <span className="eyebrow">NUEVA CUENTA</span>
        <h2>Crear cuenta de huésped</h2>
        <p>El registro público siempre asigna el rol Huésped.</p>
      </div>

      <form className="auth-form register-form" onSubmit={handleSubmit(alEnviar)} noValidate>
        <Campo
          id="nombreCompleto"
          etiqueta="Nombre completo"
          icono="usuarios"
          className="wide"
          error={errors.nombreCompleto?.message}
        >
          <input
            id="nombreCompleto"
            autoComplete="name"
            placeholder="Nombre y apellidos"
            {...atributosDeError('nombreCompleto', errors.nombreCompleto?.message)}
            {...register('nombreCompleto')}
          />
        </Campo>

        <Campo id="correo" etiqueta="Correo electrónico" error={errors.correo?.message}>
          <input
            id="correo"
            type="email"
            autoComplete="email"
            placeholder="nombre@correo.com"
            {...atributosDeError('correo', errors.correo?.message)}
            {...register('correo')}
          />
        </Campo>

        <Campo id="documento" etiqueta="Documento de identidad" error={errors.documento?.message}>
          <input
            id="documento"
            placeholder="Número de documento"
            {...atributosDeError('documento', errors.documento?.message)}
            {...register('documento')}
          />
        </Campo>

        <Campo id="telefono" etiqueta="Teléfono" error={errors.telefono?.message}>
          <input
            id="telefono"
            type="tel"
            autoComplete="tel"
            placeholder="+57 300 000 0000"
            {...atributosDeError('telefono', errors.telefono?.message)}
            {...register('telefono')}
          />
        </Campo>

        <Campo id="fechaNacimiento" etiqueta="Fecha de nacimiento" error={errors.fechaNacimiento?.message}>
          <input
            id="fechaNacimiento"
            type="date"
            autoComplete="bday"
            max={hoyISO()}
            {...atributosDeError('fechaNacimiento', errors.fechaNacimiento?.message)}
            {...register('fechaNacimiento')}
          />
        </Campo>

        <Campo id="contrasena" etiqueta="Contraseña segura" error={errors.contrasena?.message}>
          <input
            id="contrasena"
            type="password"
            autoComplete="new-password"
            placeholder="Mínimo 8 caracteres"
            {...atributosDeError('contrasena', errors.contrasena?.message)}
            {...register('contrasena')}
          />
        </Campo>

        <Campo id="confirmacion" etiqueta="Confirmar contraseña" error={errors.confirmacion?.message}>
          <input
            id="confirmacion"
            type="password"
            autoComplete="new-password"
            placeholder="Repite la contraseña"
            {...atributosDeError('confirmacion', errors.confirmacion?.message)}
            {...register('confirmacion')}
          />
        </Campo>

        <p className="password-rule wide">
          <Icono nombre="escudo" tamano={14} /> Mínimo 8 caracteres, con una mayúscula y un número.
        </p>

        {errors.root?.servidor && (
          <p className="form-error wide" role="alert">
            <Icono nombre="alerta" tamano={14} /> {errors.root.servidor.message}
          </p>
        )}

        <button type="submit" className="primary full auth-submit wide" disabled={isSubmitting}>
          {isSubmitting ? 'Creando cuenta…' : 'Crear cuenta'} <Icono nombre="flecha" tamano={18} />
        </button>
      </form>

      <p className="auth-switch">
        ¿Ya tienes cuenta? <Link to="/login">Ingresa</Link>
      </p>
    </>
  );
}
