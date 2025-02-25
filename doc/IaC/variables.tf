# Project Variables

variable "region" {
  description = "Region in IBM Cloud."
  type        = string
  default     = "eu-de"
}

variable "prefix" {
  description = "Unique prefix to avoid name collisions."
  type        = string
}

variable "api_key" {
  description = "IBM Cloud API Key"
  sensitive   = true
  type        = string
}
