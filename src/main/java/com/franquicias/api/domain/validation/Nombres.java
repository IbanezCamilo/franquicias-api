package com.franquicias.api.domain.validation;

import com.franquicias.api.domain.exception.ReglaDeNegocioException;

import java.util.Locale;

/**
 * Reglas de formato y comparacion de los nombres del dominio (franquicia, sucursal
 * y producto), centralizadas para que las tres entidades se comporten igual.
 */
public final class Nombres {

    public static final int LONGITUD_MINIMA = 2;
    public static final int LONGITUD_MAXIMA = 100;

    private Nombres() {
    }

    /**
     * Normaliza un nombre recortando los espacios de los extremos y valida su formato.
     *
     * @param campo  nombre del campo, usado para construir un mensaje de error util
     * @param valor  valor recibido, posiblemente nulo o con espacios sobrantes
     * @return el nombre ya recortado
     * @throws ReglaDeNegocioException si es nulo, esta en blanco o excede los limites
     */
    public static String normalizarYValidar(String campo, String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDeNegocioException("El " + campo + " es obligatorio");
        }
        String normalizado = valor.strip();
        if (normalizado.length() < LONGITUD_MINIMA || normalizado.length() > LONGITUD_MAXIMA) {
            throw new ReglaDeNegocioException("El " + campo + " debe tener entre " + LONGITUD_MINIMA
                    + " y " + LONGITUD_MAXIMA + " caracteres");
        }
        return normalizado;
    }

    /**
     * Compara dos nombres para efectos de unicidad. La comparacion ignora mayusculas y
     * espacios en los extremos: "Sucursal Norte" y "sucursal norte " son el mismo nombre.
     */
    public static boolean sonEquivalentes(String uno, String otro) {
        return clave(uno).equals(clave(otro));
    }

    private static String clave(String valor) {
        return valor == null ? "" : valor.strip().toLowerCase(Locale.ROOT);
    }
}
