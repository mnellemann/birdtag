# Project Outputs

output "app_ingest_endpoint" {
    description = "Public endpoint / URL of Ingest app."
    value = ibm_code_engine_app.ingest_app.endpoint
}

output "app_present_endpoint" {
    description = "Public endpoint / URL of Presenter app."
    value = ibm_code_engine_app.present_app.endpoint
}
