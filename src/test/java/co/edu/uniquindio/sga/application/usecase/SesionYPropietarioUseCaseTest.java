package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.EscenarioReservas;
import co.edu.uniquindio.sga.ReservasDePrueba;
import co.edu.uniquindio.sga.application.exception.AccesoDenegadoException;
import co.edu.uniquindio.sga.application.exception.CredencialesInvalidasException;
import co.edu.uniquindio.sga.application.port.CifradorContrasenas;
import co.edu.uniquindio.sga.application.port.EmisorTokens;
import co.edu.uniquindio.sga.application.exception.SolicitudUsuarioInvalidaException;
import co.edu.uniquindio.sga.application.exception.UsuarioYaRegistradoException;
import co.edu.uniquindio.sga.application.port.UsuarioRepository;
import co.edu.uniquindio.sga.application.seguridad.CorreoElectronico;
import co.edu.uniquindio.sga.application.seguridad.Rol;
import co.edu.uniquindio.sga.application.seguridad.Solicitante;
import co.edu.uniquindio.sga.application.seguridad.Usuario;
import co.edu.uniquindio.sga.domain.entity.Ocupante;
import co.edu.uniquindio.sga.domain.entity.Reserva;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.domain.valueobject.CanalOrigen;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.TipoTemporada;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Registro, inicio de sesión y verificación por propietario (7.2). */
@ExtendWith(MockitoExtension.class)
class SesionYPropietarioUseCaseTest {

    private static final CorreoElectronico CORREO = new CorreoElectronico("ana@correo.com");
    private static final Solicitante OTRO_HUESPED = new Solicitante("otro@correo.com", Rol.HUESPED,
            new DocumentoIdentidad("CC-999"));

    @Mock private UsuarioRepository usuarios;
    @Mock private CifradorContrasenas cifrador;
    @Mock private EmisorTokens emisor;
    @Mock private ReservaRepository reservas;

    private static final LocalDate NACIMIENTO = LocalDate.of(1995, 4, 12);

    @Test
    void registrarHuespedGuardaLaContrasenaCifrada() {
        when(cifrador.cifrar("Clave12345*")).thenReturn("$2a$10$cifrada");
        var useCase = new RegistrarHuespedUseCase(usuarios, cifrador);

        Usuario usuario = useCase.ejecutar(CORREO, new DocumentoIdentidad("1094"), NACIMIENTO, "Ana", "3001234567",
                "Clave12345*");

        assertEquals("$2a$10$cifrada", usuario.getContrasenaCifrada());
        assertEquals(Rol.HUESPED, usuario.getRol());
        verify(usuarios).guardar(usuario);
    }

    @Test
    void registrarHuespedConCorreoExistenteNoGuarda() {
        when(usuarios.existePorCorreo(CORREO)).thenReturn(true);
        var useCase = new RegistrarHuespedUseCase(usuarios, cifrador);

        assertThrows(UsuarioYaRegistradoException.class, () -> useCase.ejecutar(CORREO,
                new DocumentoIdentidad("1094"), NACIMIENTO, "Ana", "3001234567", "Clave12345*"));
        verify(usuarios, never()).guardar(any());
    }

    @Test
    void registrarHuespedConContrasenaInseguraNoConsultaNiGuarda() {
        var useCase = new RegistrarHuespedUseCase(usuarios, cifrador);

        assertThrows(SolicitudUsuarioInvalidaException.class, () -> useCase.ejecutar(CORREO,
                new DocumentoIdentidad("1094"), NACIMIENTO, "Ana", "3001234567", "sinmayusculas1"));
        verify(usuarios, never()).guardar(any());
    }

    @Test
    void iniciarSesionEmiteTokenSoloConCredencialesValidas() {
        Usuario usuario = Usuario.registrarHuesped(UUID.randomUUID(), CORREO, new DocumentoIdentidad("1094"),
                NACIMIENTO, "Ana", "3001234567", "hash");
        when(usuarios.obtenerPorCorreo(CORREO)).thenReturn(Optional.of(usuario));
        when(cifrador.coincide("buena", "hash")).thenReturn(true);
        when(cifrador.coincide("mala", "hash")).thenReturn(false);
        when(emisor.emitir(usuario)).thenReturn(new EmisorTokens.TokenSesion("jwt", Instant.EPOCH));
        var useCase = new IniciarSesionUseCase(usuarios, cifrador, emisor);

        assertEquals("jwt", useCase.ejecutar(CORREO, "buena").token().token());
        assertThrows(CredencialesInvalidasException.class, () -> useCase.ejecutar(CORREO, "mala"));
    }

    @Test
    void unHuespedNoConsultaLaReservaDeOtro() {
        Reserva reserva = ReservasDePrueba.pendiente();
        when(reservas.obtenerPorCodigo(reserva.getCodigo())).thenReturn(Optional.of(reserva));

        assertThrows(AccesoDenegadoException.class,
                () -> new ConsultarReservaUseCase(reservas).ejecutar(reserva.getCodigo(), OTRO_HUESPED));
    }

    @Test
    void unHuespedNoCancelaLaReservaDeOtroYNadaSeGuarda() {
        EscenarioReservas e = new EscenarioReservas();
        Reserva reserva = e.crear();
        var cancelar = new CancelarReservaUseCase(e.reservas, e.folios,
                new co.edu.uniquindio.sga.domain.service.RetencionCancelacionService(e.politicas),
                e.reloj(ReservasDePrueba.CREACION));

        assertThrows(AccesoDenegadoException.class,
                () -> cancelar.ejecutar(reserva.getCodigo(), "No es mía", OTRO_HUESPED));
        assertEquals(co.edu.uniquindio.sga.domain.valueobject.EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void unHuespedSoloReservaPorElPortalYASuNombre() {
        EscenarioReservas e = new EscenarioReservas();
        Ocupante titular = ReservasDePrueba.pendiente().getTitular();
        var crear = e.creador(ReservasDePrueba.CREACION, TipoTemporada.BASE, 100_000);
        Solicitante duenio = new Solicitante("ana@correo.com", Rol.HUESPED, titular.getDocumento());
        var estancia = ReservasDePrueba.pendiente().getEstancia();

        assertThrows(AccesoDenegadoException.class, () -> crear.ejecutar(e.apartamento.getIdentificacion(), estancia,
                titular, List.of(titular), CanalOrigen.DIRECTO, null, LocalTime.NOON, 0, duenio));
        assertThrows(AccesoDenegadoException.class, () -> crear.ejecutar(e.apartamento.getIdentificacion(), estancia,
                titular, List.of(titular), CanalOrigen.PORTAL, null, LocalTime.NOON, 0, OTRO_HUESPED));
        Reserva reserva = crear.ejecutar(e.apartamento.getIdentificacion(), estancia,
                titular, List.of(titular), CanalOrigen.PORTAL, null, LocalTime.NOON, 0, duenio);
        assertEquals(CanalOrigen.PORTAL, reserva.getCanalOrigen());
    }
}
