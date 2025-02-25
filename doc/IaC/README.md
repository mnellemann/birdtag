# Infrastructure as Code


## Prequisites

1. IBM Cloud Account
2. Login and create an API Key


## Configuration

1c. Run `terraform init`

[Take care to store](https://spacelift.io/blog/terraform-state) the **terraform.tfstate** file, as this file is needed when you apply or destroy your infrastructure.

## Plan & Apply

Ensure you provide the API Key:

```
export IC_API_KEY=<your api key>
export TF_VAR_api_key=$IC_API_KEY
export TF_VAR_prefix=<unique name eg. birdtest2>
```

1. Run `terraform plan`
2. Run `terraform apply`
