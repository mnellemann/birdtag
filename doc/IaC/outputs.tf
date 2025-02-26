# Project Outputs

output "app_ingest_endpoint" {
    description = "Public endpoint / URL of Ingest app."
    value = ibm_code_engine_app.ingest_app.endpoint
}

output "app_present_endpoint" {
    description = "Public endpoint / URL of Presenter app."
    value = ibm_code_engine_app.present_app.endpoint
}

output "cos_endpoint" {
  description = "Cloud Object Storage Endpoint."
  value = format("https://%s", ibm_cos_bucket.image_bucket.s3_endpoint_public)
}

output "cos_bucket" {
  description = "Cloud Object Storage Bucket."
  value = ibm_cos_bucket.image_bucket.bucket_name
}

output "cloudant_endpoint" {
  description = "Cloudant Database Endpoint."
  value = format("https://%s", ibm_cloudant.this.extensions["endpoints.public"])
}
