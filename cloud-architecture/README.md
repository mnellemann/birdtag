# IBM Cloud Ansible: VPC Virtual Server Instance

### IBM Cloud Provider for Terraform

See [docs](https://registry.terraform.io/providers/IBM-Cloud/ibm/latest/docs/).


### Set API Key and Region

1. [Obtain an IBM Cloud API key].

2. Export your API key to the `IBMCLOUD_API_KEY` environment variable:

```
export IBMCLOUD_API_KEY=<YOUR_API_KEY_HERE>
```

Note: Modules also support the 'ibmcloud_api_key' parameter, but it is
recommended to only use this when encrypting your API key value.

3. Export desired IBM Cloud region to the `IBMCLOUD_ZONE` environment variable:

```
export IBMCLOUD_ZONE=<ZONE_NAME_HERE>
```

Note: Modules also support the 'ibmcloud_region' parameter.


###

Terraform plan