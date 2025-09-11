# DefaultApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**validateLoanEligibility**](DefaultApi.md#validateLoanEligibility) | **POST** /api/loan-eligibility | Validate loan eligibility |


<a id="validateLoanEligibility"></a>
# **validateLoanEligibility**
> LoanValidationResponse validateLoanEligibility(loanValidationRequest)

Validate loan eligibility

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.DefaultApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    DefaultApi apiInstance = new DefaultApi(defaultClient);
    LoanValidationRequest loanValidationRequest = new LoanValidationRequest(); // LoanValidationRequest | 
    try {
      LoanValidationResponse result = apiInstance.validateLoanEligibility(loanValidationRequest);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DefaultApi#validateLoanEligibility");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **loanValidationRequest** | [**LoanValidationRequest**](LoanValidationRequest.md)|  | |

### Return type

[**LoanValidationResponse**](LoanValidationResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Eligibility evaluation |  -  |

