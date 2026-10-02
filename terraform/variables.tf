variable "atlas_public_key" {
  description = "Clave publica de la API de MongoDB Atlas (Organization Access Manager)"
  type        = string
  sensitive   = true
}

variable "atlas_private_key" {
  description = "Clave privada de la API de MongoDB Atlas"
  type        = string
  sensitive   = true
}

variable "atlas_org_id" {
  description = "Identificador de la organizacion de Atlas donde se crea el proyecto"
  type        = string
}

variable "nombre_proyecto" {
  description = "Nombre del proyecto de Atlas"
  type        = string
  default     = "franquicias-api"
}

variable "nombre_cluster" {
  description = "Nombre del cluster"
  type        = string
  default     = "franquicias-cluster"
}

variable "nombre_base_datos" {
  description = "Base de datos sobre la que el usuario de la aplicacion tiene readWrite"
  type        = string
  default     = "franquicias"
}

variable "region" {
  description = "Region del proveedor subyacente. Conviene elegir la mas cercana al runtime de la aplicacion para reducir latencia"
  type        = string
  default     = "US_EAST_1"
}

variable "proveedor_subyacente" {
  description = "Proveedor que hospeda el cluster compartido M0"
  type        = string
  default     = "AWS"

  validation {
    condition     = contains(["AWS", "GCP", "AZURE"], var.proveedor_subyacente)
    error_message = "El proveedor subyacente debe ser AWS, GCP o AZURE."
  }
}

variable "usuario_bd" {
  description = "Usuario de base de datos que usara la aplicacion"
  type        = string
  default     = "franquicias_app"
}

variable "password_bd" {
  description = "Password del usuario de base de datos. Nunca se versiona: se pasa por TF_VAR_password_bd o por terraform.tfvars, que esta en .gitignore"
  type        = string
  sensitive   = true

  validation {
    condition     = length(var.password_bd) >= 12
    error_message = "El password debe tener al menos 12 caracteres."
  }
}

variable "cidr_permitido" {
  description = "Rango autorizado a conectarse. Por defecto abierto, porque Render free no tiene IP de salida estatica. Si se despliega donde si la haya, conviene restringirlo"
  type        = string
  default     = "0.0.0.0/0"
}
