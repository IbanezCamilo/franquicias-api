package com.franquicias.api.infrastructure.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Cuerpos de peticion de la API, agrupados para que el contrato de entrada se lea
 * de un vistazo.
 *
 * <p>La validacion se declara aqui ademas de en el dominio: Bean Validation produce
 * un 400 con el detalle por campo antes de llegar al caso de uso, y el dominio
 * protege sus invariantes aunque se le invoque desde otro adaptador.
 */
public final class Peticiones {

    private Peticiones() {
    }

    @Schema(name = "CrearFranquicia", description = "Datos para dar de alta una franquicia")
    public record CrearFranquicia(
            @Schema(example = "Cafes del Valle")
            @NotBlank(message = "El nombre es obligatorio")
            @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
            String nombre) {
    }

    @Schema(name = "CrearSucursal", description = "Datos para agregar una sucursal")
    public record CrearSucursal(
            @Schema(example = "Sucursal Norte")
            @NotBlank(message = "El nombre es obligatorio")
            @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
            String nombre) {
    }

    @Schema(name = "CrearProducto", description = "Datos para agregar un producto a una sucursal")
    public record CrearProducto(
            @Schema(example = "Cafe Molido")
            @NotBlank(message = "El nombre es obligatorio")
            @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
            String nombre,

            @Schema(example = "120")
            @NotNull(message = "El stock es obligatorio")
            @PositiveOrZero(message = "El stock no puede ser negativo")
            Integer stock) {
    }

    @Schema(name = "ActualizarStock", description = "Nuevo stock de un producto")
    public record ActualizarStock(
            @Schema(example = "250")
            @NotNull(message = "El stock es obligatorio")
            @PositiveOrZero(message = "El stock no puede ser negativo")
            Integer stock) {
    }

    @Schema(name = "CambiarNombre", description = "Nuevo nombre de un recurso existente")
    public record CambiarNombre(
            @Schema(example = "Cafes del Rio")
            @NotBlank(message = "El nombre es obligatorio")
            @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
            String nombre) {
    }
}
