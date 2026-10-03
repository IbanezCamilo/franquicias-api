output "id_proyecto" {
  description = "Identificador del proyecto de Atlas creado"
  value       = mongodbatlas_project.franquicias.id
}

output "nombre_cluster" {
  description = "Nombre del cluster creado"
  value       = mongodbatlas_advanced_cluster.franquicias.name
}

output "cadena_conexion_estandar" {
  description = "Cadena de conexion SRV sin credenciales. Hay que insertar usuario y password antes de usarla como MONGODB_URI"
  value       = mongodbatlas_advanced_cluster.franquicias.connection_strings.standard_srv
}

output "mongodb_uri" {
  description = "MONGODB_URI completa, lista para configurar en Render. Incluye credenciales, base de datos y opciones de escritura. Marcada como sensible"
  sensitive   = true
  value = format(
    "%s/%s?retryWrites=true&w=majority",
    replace(
      mongodbatlas_advanced_cluster.franquicias.connection_strings.standard_srv,
      "mongodb+srv://",
      "mongodb+srv://${var.usuario_bd}:${urlencode(var.password_bd)}@"
    ),
    var.nombre_base_datos
  )
}
