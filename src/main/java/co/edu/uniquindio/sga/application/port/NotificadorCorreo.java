package co.edu.uniquindio.sga.application.port;

import co.edu.uniquindio.sga.application.seguridad.CorreoElectronico;

/**
 * Puerto de salida para enviar correos (10.3.3, I-01). La implementación local
 * (I-02) permite operar sin internet ni credenciales; un fallo del envío no
 * detiene la operación de negocio (I-05).
 */
public interface NotificadorCorreo {

    void enviarCodigoRecuperacion(CorreoElectronico destinatario, String codigo, int vigenciaMinutos);
}
