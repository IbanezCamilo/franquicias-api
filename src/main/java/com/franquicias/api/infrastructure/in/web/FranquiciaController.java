package com.franquicias.api.infrastructure.in.web;

import com.franquicias.api.application.usecase.ConsultarProductosDestacados;
import com.franquicias.api.application.usecase.CrearFranquicia;
import com.franquicias.api.application.usecase.RenombrarFranquicia;
import com.franquicias.api.infrastructure.in.web.dto.Peticiones;
import com.franquicias.api.infrastructure.in.web.dto.Respuestas.FranquiciaResponse;
import com.franquicias.api.infrastructure.in.web.dto.Respuestas.ProductoDestacadoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/franquicias")
@Tag(name = "Franquicias", description = "Alta, renombrado y reporte de mayor stock")
public class FranquiciaController {

    private final CrearFranquicia crearFranquicia;
    private final RenombrarFranquicia renombrarFranquicia;
    private final ConsultarProductosDestacados consultarProductosDestacados;

    public FranquiciaController(CrearFranquicia crearFranquicia,
                                RenombrarFranquicia renombrarFranquicia,
                                ConsultarProductosDestacados consultarProductosDestacados) {
        this.crearFranquicia = crearFranquicia;
        this.renombrarFranquicia = renombrarFranquicia;
        this.consultarProductosDestacados = consultarProductosDestacados;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Agrega una nueva franquicia")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Franquicia creada"),
            @ApiResponse(responseCode = "400", description = "Nombre invalido", content = @Content),
            @ApiResponse(responseCode = "409", description = "Ya existe una franquicia con ese nombre", content = @Content)
    })
    public Mono<FranquiciaResponse> crear(@Valid @RequestBody Peticiones.CrearFranquicia peticion) {
        return crearFranquicia.ejecutar(peticion.nombre()).map(FranquiciaResponse::desde);
    }

    @PatchMapping("/{franquiciaId}/nombre")
    @Operation(summary = "Actualiza el nombre de la franquicia")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nombre actualizado"),
            @ApiResponse(responseCode = "400", description = "Nombre invalido", content = @Content),
            @ApiResponse(responseCode = "404", description = "La franquicia no existe", content = @Content),
            @ApiResponse(responseCode = "409", description = "El nombre pertenece a otra franquicia", content = @Content)
    })
    public Mono<FranquiciaResponse> renombrar(@PathVariable UUID franquiciaId,
                                              @Valid @RequestBody Peticiones.CambiarNombre peticion) {
        return renombrarFranquicia.ejecutar(franquiciaId, peticion.nombre())
                .map(FranquiciaResponse::desde);
    }

    @GetMapping("/{franquiciaId}/sucursales/productos-top-stock")
    @Operation(summary = "Producto con mayor stock de cada sucursal de la franquicia",
            description = "Devuelve un producto por sucursal, indicando a que sucursal pertenece. "
                    + "Las sucursales sin productos se omiten y los empates de stock se "
                    + "resuelven por nombre ascendente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de productos destacados"),
            @ApiResponse(responseCode = "404", description = "La franquicia no existe", content = @Content)
    })
    public Flux<ProductoDestacadoResponse> productosDestacados(@PathVariable UUID franquiciaId) {
        return consultarProductosDestacados.ejecutar(franquiciaId)
                .map(ProductoDestacadoResponse::desde);
    }
}
