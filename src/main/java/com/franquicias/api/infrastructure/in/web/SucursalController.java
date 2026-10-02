package com.franquicias.api.infrastructure.in.web;

import com.franquicias.api.application.usecase.AgregarSucursal;
import com.franquicias.api.application.usecase.RenombrarSucursal;
import com.franquicias.api.infrastructure.in.web.dto.Peticiones;
import com.franquicias.api.infrastructure.in.web.dto.Respuestas.SucursalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/franquicias/{franquiciaId}/sucursales")
@Tag(name = "Sucursales", description = "Alta y renombrado de sucursales de una franquicia")
public class SucursalController {

    private final AgregarSucursal agregarSucursal;
    private final RenombrarSucursal renombrarSucursal;

    public SucursalController(AgregarSucursal agregarSucursal, RenombrarSucursal renombrarSucursal) {
        this.agregarSucursal = agregarSucursal;
        this.renombrarSucursal = renombrarSucursal;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Agrega una nueva sucursal a la franquicia")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sucursal creada"),
            @ApiResponse(responseCode = "400", description = "Nombre invalido", content = @Content),
            @ApiResponse(responseCode = "404", description = "La franquicia no existe", content = @Content),
            @ApiResponse(responseCode = "409", description = "La franquicia ya tiene una sucursal con ese nombre", content = @Content)
    })
    public Mono<SucursalResponse> agregar(@PathVariable UUID franquiciaId,
                                          @Valid @RequestBody Peticiones.CrearSucursal peticion) {
        return agregarSucursal.ejecutar(franquiciaId, peticion.nombre())
                .map(SucursalResponse::desde);
    }

    @PatchMapping("/{sucursalId}/nombre")
    @Operation(summary = "Actualiza el nombre de la sucursal")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nombre actualizado"),
            @ApiResponse(responseCode = "400", description = "Nombre invalido", content = @Content),
            @ApiResponse(responseCode = "404", description = "La franquicia o la sucursal no existen", content = @Content),
            @ApiResponse(responseCode = "409", description = "El nombre pertenece a otra sucursal", content = @Content)
    })
    public Mono<SucursalResponse> renombrar(@PathVariable UUID franquiciaId,
                                            @PathVariable UUID sucursalId,
                                            @Valid @RequestBody Peticiones.CambiarNombre peticion) {
        return renombrarSucursal.ejecutar(franquiciaId, sucursalId, peticion.nombre())
                .map(SucursalResponse::desde);
    }
}
