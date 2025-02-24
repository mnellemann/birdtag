# Project Outputs

output "app_ingest_endpoint" {
    description = "Public endpoint / URL of deployed ingest app"
    value = ibm_code_engine_app.ingest_app.endpoint
}

output "app_present_endpoint" {
    description = "Public endpoint / URL of deployed presentor app"
    value = ibm_code_engine_app.present_app.endpoint
}

#output "cloudant_foo" {
#  value = ibm_cloudant_database.this.url
#}