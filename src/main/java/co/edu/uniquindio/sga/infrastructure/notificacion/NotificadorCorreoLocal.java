package co.edu.uniquindio.sga.infrastructure.notificacion;

import co.edu.uniquindio.sga.application.port.NotificadorCorreo;
import co.edu.uniquindio.sga.application.seguridad.CorreoElectronico;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementación local del envío de correos (I-02): escribe el mensaje en el log
 * para operar sin internet ni credenciales. Se reemplaza por un proveedor real
 * sin tocar los casos de uso, porque estos solo conocen el puerto.
 */
@Component
public class NotificadorCorreoLocal implements NotificadorCorreo {

    private static final Logger LOG = LoggerFactory.getLogger(NotificadorCorreoLocal.class);

    @Override
    public void enviarCodigoRecuperacion(CorreoElectronico destinatario, String codigo, int vigenciaMinutos) {
        LOG.info("[correo local] Para: {} | Código de recuperación: {} | Vigencia: {} minutos",
                destinatario.valor(), codigo, vigenciaMinutos);
    }
}
