# Project Outputs

output "cos_bucket_crn" {
    value = ibm_cos_bucket.image_bucket.crn
    description = "Bucket CRN"
}

output "cos_bucket_location" {
    value = ibm_cos_bucket.image_bucket.region_location
    description = "Bucket region location"
}

output "app_ingest_endpoint" {
    description = "Public endpoint / URL of deployed ingest app"
    value = ibm_code_engine_app.ingest_app.endpoint
}

#output "cloudant_foo" {
#  value = ibm_cloudant_database.this.url
#}