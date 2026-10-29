package co.edu.uniquindio.sga.application.port;

/** Parámetros de seguridad que vienen de la configuración externa. */
public interface ConfiguracionSeguridad {

    /** Vigencia del código de recuperación de contraseña (7.2: 15 minutos). */
    int vigenciaCodigoRecuperacionMinutos();
}
