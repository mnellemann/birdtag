


###
### General
###

# IBM Cloud Resource Group
resource "ibm_resource_group" "this" {
  name     = var.prefix
}



###
### COS Bucket
###


# COS Resource
resource "ibm_resource_instance" "cos" {
  name              = var.prefix
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
  name     = var.prefix
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

resource "ibm_resource_key" "cloudant_credentials" {
  name                  = "my-cloudant-iam-key"
  role                  = "Manager"
  resource_instance_id  = ibm_cloudant.this.id
}



###
### Code Engine
###

resource "ibm_code_engine_project" "this" {
   name = var.prefix
   resource_group_id = ibm_resource_group.this.id
}

resource "ibm_code_engine_app" "ingest_app" {
  project_id      = ibm_code_engine_project.this.project_id
  name            = "${var.prefix}-ingest-app"
  image_port      = 8080
  image_reference = "docker.io/mnellemann/birdtag:latest"

  scale_min_instances = 1      # default is 0
  scale_max_instances = 3
  scale_initial_instances = 1
  scale_request_timeout = 10

  # https://cloud.ibm.com/docs/codeengine?topic=codeengine-mem-cpu-combo
  scale_cpu_limit = "0.125"
  scale_memory_limit = "500M"

  run_env_variables {
    type  = "literal"
    name  = "CLOUDANT_API_KEY"
    value = var.api_key
  }

  run_env_variables {
    type  = "literal"
    name  = "CLOUDANT_URL"
    value = format("https://%s", ibm_cloudant.this.extensions["endpoints.public"])
  }

  run_env_variables {
    type  = "literal"
    name  = "COS_API_KEY"
    value = var.api_key
  }

  run_env_variables {
    type  = "literal"
    name  = "COS_CRN"
    value = ibm_cos_bucket.image_bucket.crn
  }

  run_env_variables {
    type  = "literal"
    name  = "COS_LOCATION"
    value = ibm_cos_bucket.image_bucket.region_location
  }

  run_env_variables {
    type  = "literal"
    name  = "COS_ENDPOINT"
    value = format("https://%s", ibm_cos_bucket.image_bucket.s3_endpoint_public)
  }

  run_env_variables {
    type  = "literal"
    name  = "COS_BUCKET"
    value = ibm_cos_bucket.image_bucket.bucket_name
  }

  lifecycle {
    create_before_destroy = true
    /*ignore_changes = [
      run_env_variables,
    ]*/
  }

  # Restart container if probe fails
  #probe_liveness {
  #  type = "tcp"
  #  timeout = 5
  #  interval = 30
  #  initial_delay = 10
  #  failure_threshold = 3
  #}

  # Do not receive traffic if probe fails
  probe_readiness {
    type = "http"
    path = "/"
    timeout = 5
    interval = 60
    initial_delay = 10
    failure_threshold = 3
  }

}


resource "ibm_code_engine_app" "present_app" {
  project_id      = ibm_code_engine_project.this.project_id
  name            = "${var.prefix}-present-app"
  image_port      = 8080
  image_reference = "docker.io/mnellemann/birdtag:latest"

  scale_min_instances = 1  # default is 0
  scale_max_instances = 3
  scale_initial_instances = 1
  scale_request_timeout = 10

  scale_cpu_limit = "0.125"
  scale_memory_limit = "500M"

  // TODO: env variables to keep without changing:
  // CE_SUBDOMAIN
  // CE_PROJECT_ID
  // CE_APP
  // CE_DOMAIN
  // CE_REGION
  // CE_API_BASE_URL


  run_env_variables {
    type  = "literal"
    name  = "CLOUDANT_API_KEY"
    value = var.api_key
  }

  run_env_variables {
    type  = "literal"
    name  = "CLOUDANT_URL"
    value = format("https://%s", ibm_cloudant.this.extensions["endpoints.public"])
  }

  run_env_variables {
    type  = "literal"
    name  = "COS_API_KEY"
    value = var.api_key
  }

  run_env_variables {
    type  = "literal"
    name  = "COS_CRN"
    value = ibm_cos_bucket.image_bucket.crn
  }

  run_env_variables {
    type  = "literal"
    name  = "COS_LOCATION"
    value = ibm_cos_bucket.image_bucket.region_location
  }

  run_env_variables {
    type  = "literal"
    name  = "COS_ENDPOINT"
    value = format("https://%s", ibm_cos_bucket.image_bucket.s3_endpoint_public)
  }

  run_env_variables {
    type  = "literal"
    name  = "COS_BUCKET"
    value = ibm_cos_bucket.image_bucket.bucket_name
  }

  lifecycle {
    create_before_destroy = true
    /*ignore_changes = [
      run_env_variables,
    ]*/
  }

  # Restart container if probe fails
  #probe_liveness {
  #  type = "tcp"
  #  timeout = 5
  #  interval = 30
  #  initial_delay = 10
  #  failure_threshold = 3
  #}

  # Do not receive traffic if probe fails
  probe_readiness {
    type = "http"
    path = "/"
    timeout = 5
    interval = 60
    initial_delay = 10
    failure_threshold = 3
  }

}

