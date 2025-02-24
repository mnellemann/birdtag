# Project Providers

terraform {
  required_providers {
    docker = {
      source = "kreuzwerker/docker"
      version = "~> 3.0.1"
    }
    ibm = {
      source = "IBM-Cloud/ibm"
      version = ">= 1.12.0"
    }
  }
}

//provider "docker" {}


# Configure the IBM Provider
provider "ibm" {
  region = "eu-de"
}
