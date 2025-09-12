# LoanValidationsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**validateLoan**](LoanValidationsApi.md#validateLoan) | **POST** /loan-validations | Valida un préstamo |


<a id="validateLoan"></a>
# **validateLoan**
> LoanValidationResult validateLoan(loanValidationRequest)

Valida un préstamo

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.LoanValidationsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost");

    LoanValidationsApi apiInstance = new LoanValidationsApi(defaultClient);
    LoanValidationRequest loanValidationRequest = new LoanValidationRequest(); // LoanValidationRequest | 
    try {
      LoanValidationResult result = apiInstance.validateLoan(loanValidationRequest);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling LoanValidationsApi#validateLoan");
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

[**LoanValidationResult**](LoanValidationResult.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Resultado de validación |  -  |

