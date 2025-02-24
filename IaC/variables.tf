# Project Variables

variable "region" {
    description = "Region in IBM Cloud."
    type        = string
    default     = "eu-de"
}

variable "group" {
    description = "Resource group for all resources created."
    type        = string
    default     = "BirdTagging-Demo"
}

variable "prefix" {
    description = "String to prefix resources to avoid name collisions."
    type        = string
    default     = "birdtag"
}

variable "ingest_port" {
    description = "Internal port number of ingest container."
    type        = number
    default     = 8080
}

variable "api_key" {
    description = "IBM Cloud API Key"
    type = string
    sensitive = true
}
