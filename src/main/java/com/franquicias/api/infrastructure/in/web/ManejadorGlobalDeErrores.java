package com.franquicias.api.infrastructure.in.web;

import com.franquicias.api.domain.exception.NombreDuplicadoException;
import com.franquicias.api.domain.exception.RecursoNoEncontradoException;
import com.franquicias.api.domain.exception.ReglaDeNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce las excepciones del dominio y de la capa web a respuestas
 * {@code application/problem+json} conforme a la RFC 9457.
 *
 * <p>Es el unico punto donde el proyecto decide codigos HTTP: el dominio lanza
 * excepciones con significado de negocio y no sabe nada de HTTP.
 */
@RestControllerAdvice
public class ManejadorGlobalDeErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalDeErrores.class);

    private static final URI TIPO_NO_ENCONTRADO = URI.create("/errores/recurso-no-encontrado");
    private static final URI TIPO_CONFLICTO = URI.create("/errores/nombre-duplicado");
    private static final URI TIPO_REGLA_NEGOCIO = URI.create("/errores/regla-de-negocio");
    private static final URI TIPO_VALIDACION = URI.create("/errores/validacion");
    private static final URI TIPO_PETICION_INVALIDA = URI.create("/errores/peticion-invalida");
    private static final URI TIPO_INESPERADO = URI.create("/errores/error-inesperado");

    @ExceptionHandler(RecursoNoEncontradoException.class)
    ProblemDetail noEncontrado(RecursoNoEncontradoException excepcion) {
        return problema(HttpStatus.NOT_FOUND, "Recurso no encontrado",
                excepcion.getMessage(), TIPO_NO_ENCONTRADO);
    }

    @ExceptionHandler(NombreDuplicadoException.class)
    ProblemDetail nombreDuplicado(NombreDuplicadoException excepcion) {
        return problema(HttpStatus.CONFLICT, "Nombre duplicado",
                excepcion.getMessage(), TIPO_CONFLICTO);
    }

    @ExceptionHandler(ReglaDeNegocioException.class)
    ProblemDetail reglaDeNegocio(ReglaDeNegocioException excepcion) {
        return problema(HttpStatus.BAD_REQUEST, "Regla de negocio incumplida",
                excepcion.getMessage(), TIPO_REGLA_NEGOCIO);
    }

    /** Fallo de Bean Validation sobre el cuerpo de la peticion: se detalla campo a campo. */
    @ExceptionHandler(WebExchangeBindException.class)
    ProblemDetail validacion(WebExchangeBindException excepcion) {
        ProblemDetail detalle = problema(HttpStatus.BAD_REQUEST, "Peticion invalida",
                "Uno o mas campos no superan la validacion", TIPO_VALIDACION);

        Map<String, String> errores = new LinkedHashMap<>();
        excepcion.getFieldErrors().forEach(error ->
                errores.put(error.getField(), mensajeDe(error)));
        detalle.setProperty("errors", errores);
        return detalle;
    }

    /**
     * Cuerpo ilegible o variable de ruta con formato incorrecto, por ejemplo un
     * identificador que no es un UUID.
     */
    @ExceptionHandler(ServerWebInputException.class)
    ProblemDetail entradaInvalida(ServerWebInputException excepcion) {
        return problema(HttpStatus.BAD_REQUEST, "Peticion invalida",
                "El cuerpo o los parametros de la peticion no tienen el formato esperado",
                TIPO_PETICION_INVALIDA);
    }

    /**
     * Errores que ya traen un estado HTTP decidido por el framework, sobre todo el 404
     * de una ruta no mapeada. Sin este handler los absorberia el de {@link Exception} y
     * cualquier URL inexistente respondria 500.
     */
    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail estadoYaDecidido(ResponseStatusException excepcion) {
        HttpStatus estado = HttpStatus.valueOf(excepcion.getStatusCode().value());
        if (estado == HttpStatus.NOT_FOUND) {
            return problema(estado, "Recurso no encontrado",
                    "La ruta solicitada no existe en esta API", TIPO_NO_ENCONTRADO);
        }
        String detalle = excepcion.getReason() == null
                ? estado.getReasonPhrase()
                : excepcion.getReason();
        return problema(estado, estado.getReasonPhrase(), detalle, TIPO_PETICION_INVALIDA);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail inesperado(Exception excepcion) {
        // Se registra con traza, pero al cliente no se le filtra el detalle interno.
        log.error("Error no controlado atendiendo la peticion", excepcion);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrio un error inesperado procesando la peticion", TIPO_INESPERADO);
    }

    private static ProblemDetail problema(HttpStatus estado, String titulo, String detalle, URI tipo) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setTitle(titulo);
        problema.setType(tipo);
        problema.setProperty("timestamp", Instant.now());
        return problema;
    }

    private static String mensajeDe(FieldError error) {
        return error.getDefaultMessage() == null ? "Valor invalido" : error.getDefaultMessage();
    }
}
