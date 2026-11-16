package co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** Código de recuperación de contraseña (7.2); el código se guarda cifrado. */
@Entity
@Table(name = "codigos_recuperacion")
@Getter @Setter @NoArgsConstructor
public class CodigoRecuperacionEntity {

    @Id
    @Column(name = "identificacion", nullable = false)
    private UUID identificacion;

    @Column(name = "correo", nullable = false, length = 120)
    private String correo;

    @Column(name = "codigo", nullable = false, length = 100)
    private String codigoCifrado;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    @Column(name = "usado", nullable = false)
    private boolean usado;
}
