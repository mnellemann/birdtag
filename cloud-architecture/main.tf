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





###
### Resources
###

resource "ibm_resource_group" "demo_resource_group" {
  name     = "BirdTagging-Demo"
}

resource "ibm_resource_instance" "demo_resource_instance" {
  name              = "BirdTagging-COS"
  service           = "cloud-object-storage"
  plan              = "standard"
  location          = "global"
  resource_group_id = ibm_resource_group.demo_resource_group.id
  #tags              = ["tag1", "tag2"]

  //User can increase timeouts
  /*
  timeouts {
    create = "15m"
    update = "15m"
    delete = "15m"
  }*/

}



###
### COS Bucket
###

resource "ibm_cos_bucket" "image_bucket" {
  bucket_name          = "birdtagging-demo-image-bucket"
  resource_instance_id = ibm_resource_instance.demo_resource_instance.id
  storage_class        = "smart"
  region_location      = "eu-de"

  #activity_tracking {
  #  read_data_events     = true
  #  write_data_events    = true
  #  management_events    = true
  #}
  #metrics_monitoring {
  #  usage_metrics_enabled  = true
  #  request_metrics_enabled = true
  #}
  #allowed_ip = ["223.196.168.27", "223.196.161.38", "192.168.0.1"]
}



###
### Cloudant DB
###


resource "ibm_cloudant" "demo_cloudant_instance" {
  name     = "cloudant-birdtag-demo"
  location = "eu-de"
  plan     = "standard"
  resource_group_id = ibm_resource_group.demo_resource_group.id

  legacy_credentials  = false
  include_data_events = false
  capacity            = 1
  enable_cors         = false
}


# Creates a database in the existing instance
resource "ibm_cloudant_database" "cloudant_database" {
  instance_crn  = ibm_cloudant.demo_cloudant_instance.crn
  db            = "birdtag-demo"
}



###
### Code Engine
###

resource "ibm_code_engine_project" "code_engine_project_instance" {
   name = "BirdTagging-Demo"
   resource_group_id = ibm_resource_group.demo_resource_group.id
}

resource "ibm_code_engine_app" "ingest_app" {
  project_id      = ibm_code_engine_project.code_engine_project_instance.project_id
  name            = "birdtag-ingest-app"
  image_reference = "docker.io/mnellemann/hellotide:latest"
  #image_reference = "docker.io/mnellemann/birdtag-ingest:latest"

  run_env_variables {
    type  = "literal"
    name  = "CLOUDANT_URL"
    value = ibm_cloudant.demo_cloudant_instance.resource_id
  }

  run_env_variables {
    type      = "secret_full_reference"
    name      = "CLOUDANT_APIKEY"
    reference = "blabla"
  }

}

resource "ibm_code_engine_app" "present_app" {
  project_id      = ibm_code_engine_project.code_engine_project_instance.project_id
  name            = "birdtag-present-app"
  image_reference = "docker.io/mnellemann/hellotide:latest"
  #image_reference = "docker.io/mnellemann/birdtag-ingest:latest"
}

