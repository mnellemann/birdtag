


###
### General
###

# IBM Cloud Resource Group
resource "ibm_resource_group" "this" {
  name     = var.group
}



###
### COS Bucket
###


# COS Resource
resource "ibm_resource_instance" "cos" {
  name              = "${var.group}-cos"
  service           = "cloud-object-storage"
  plan              = "standard"
  location          = "global"
  resource_group_id = ibm_resource_group.this.id
  #tags              = ["tag1", "tag2"]

  //User can increase timeouts
  /*
  timeouts {
    create = "15m"
    update = "15m"
    delete = "15m"
  }*/

}


# COS Bucket
resource "ibm_cos_bucket" "image_bucket" {
  bucket_name          = "${var.prefix}-images"
  resource_instance_id = ibm_resource_instance.cos.id
  storage_class        = "smart"
  region_location      = var.region

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


resource "ibm_cloudant" "this" {
  name     = "${var.prefix}-cloudant"
  location = var.region
  plan     = "standard"
  resource_group_id = ibm_resource_group.this.id

  legacy_credentials  = false
  include_data_events = false
  capacity            = 1
  enable_cors         = false
}


# Creates a database in the existing instance
resource "ibm_cloudant_database" "this" {
  instance_crn  = ibm_cloudant.this.crn
  db            = "${var.prefix}-db"
}



###
### Code Engine
###

resource "ibm_code_engine_project" "this" {
   name = "${var.prefix}-project"
   resource_group_id = ibm_resource_group.this.id
}

resource "ibm_code_engine_app" "ingest_app" {
  project_id      = ibm_code_engine_project.this.project_id
  name            = "${var.prefix}-ingest-app"
  image_reference = "docker.io/mnellemann/hellotide:latest"
  #image_reference = "docker.io/mnellemann/birdtag-ingest:latest"
  image_port      = var.ingest_port

  
  run_env_variables {
    type  = "literal"
    name  = "CLOUDANT_URL"
    value = ibm_cloudant.this.resource_id
  }

  lifecycle {
    create_before_destroy = true
    ignore_changes = [
      run_env_variables,
    ]
  }


}


resource "ibm_code_engine_app" "present_app" {
  project_id      = ibm_code_engine_project.this.project_id
  name            = "${var.prefix}-present-app"
  image_reference = "docker.io/mnellemann/hellotide:latest"
  #image_reference = "docker.io/mnellemann/birdtag-ingest:latest"

  lifecycle {
    create_before_destroy = true
    ignore_changes = [
      run_env_variables,
    ]
  }

}

