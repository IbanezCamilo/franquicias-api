terraform {
  required_version = ">= 1.9"

  required_providers {
    mongodbatlas = {
      source  = "mongodb/mongodbatlas"
      version = "~> 2.19"
    }
  }

  # El estado se mantiene local a proposito: esta prueba la aplica una sola persona
  # desde una maquina. En un equipo real iria en un backend remoto con bloqueo
  # (S3 + DynamoDB, Terraform Cloud o similar) para evitar escrituras concurrentes.
  # terraform.tfstate esta excluido del repositorio porque contiene credenciales.
}

provider "mongodbatlas" {
  public_key  = var.atlas_public_key
  private_key = var.atlas_private_key
}

# ---------------------------------------------------------------------------
# Proyecto
# ---------------------------------------------------------------------------
resource "mongodbatlas_project" "franquicias" {
  name   = var.nombre_proyecto
  org_id = var.atlas_org_id
}

# ---------------------------------------------------------------------------
# Cluster
#
# M0 es la capa gratuita permanente de Atlas. provider_name "TENANT" indica que
# es compartido y backing_provider_name el proveedor que lo hospeda por debajo.
# ---------------------------------------------------------------------------
resource "mongodbatlas_advanced_cluster" "franquicias" {
  project_id   = mongodbatlas_project.franquicias.id
  name         = var.nombre_cluster
  cluster_type = "REPLICASET"

  replication_specs = [
    {
      region_configs = [
        {
          electable_specs = {
            instance_size = "M0"
          }
          provider_name         = "TENANT"
          backing_provider_name = var.proveedor_subyacente
          region_name           = var.region
          priority              = 7
        }
      ]
    }
  ]
}

# ---------------------------------------------------------------------------
# Usuario de base de datos
#
# readWrite acotado a la base de la aplicacion: no se concede acceso al resto
# del cluster ni permisos administrativos.
# ---------------------------------------------------------------------------
resource "mongodbatlas_database_user" "aplicacion" {
  project_id         = mongodbatlas_project.franquicias.id
  username           = var.usuario_bd
  password           = var.password_bd
  auth_database_name = "admin"

  roles {
    role_name     = "readWrite"
    database_name = var.nombre_base_datos
  }
}

# ---------------------------------------------------------------------------
# Lista de IP permitidas
#
# ADVERTENCIA DELIBERADA: se abre a 0.0.0.0/0.
#
# El plan gratuito de Render no asigna IP de salida estatica, asi que no hay un
# rango concreto que autorizar. La proteccion efectiva queda en las credenciales
# del usuario de base de datos y en TLS, que Atlas impone siempre.
#
# En un entorno real esto NO es aceptable. La solucion correcta es VPC peering o
# AWS PrivateLink, de modo que el trafico no salga a la red publica; Atlas los
# ofrece a partir del nivel M10, que es de pago. Se documenta la limitacion en
# lugar de ocultarla.
# ---------------------------------------------------------------------------
resource "mongodbatlas_project_ip_access_list" "acceso_publico" {
  project_id = mongodbatlas_project.franquicias.id
  cidr_block = var.cidr_permitido
  comment    = "Abierto porque Render free no tiene IP de salida estatica"
}
