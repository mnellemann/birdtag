# Project Providers

terraform {
  required_providers {
    ibm = {
      source = "IBM-Cloud/ibm"
      version = ">= 1.12.0"
    }
  }
}

# Configure the IBM Provider
# https://registry.terraform.io/providers/IBM-Cloud/ibm/latest
provider "ibm" {
  region = var.region
  // ibmcloud_api_key = ""
}
