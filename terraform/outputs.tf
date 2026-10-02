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
  description = "MONGODB_URI completa, lista para configurar en Render. Marcada como sensible porque incluye el password"
  sensitive   = true
  value = replace(
    mongodbatlas_advanced_cluster.franquicias.connection_strings.standard_srv,
    "mongodb+srv://",
    "mongodb+srv://${var.usuario_bd}:${urlencode(var.password_bd)}@"
  )
}
