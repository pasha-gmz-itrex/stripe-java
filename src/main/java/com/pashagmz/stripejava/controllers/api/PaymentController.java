package com.pashagmz.stripejava.controllers.api;

import com.pashagmz.stripejava.models.ConfirmPaymentRequest;
import com.pashagmz.stripejava.models.CreatePaymentRequest;
import com.pashagmz.stripejava.models.StripeKeyResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerCollection;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.model.SetupIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerListParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodListParams;
import com.stripe.param.SetupIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@CrossOrigin
@RestController
@RequestMapping
public class PaymentController {

    @GetMapping("/customers/{userId}")
    public Customer getCustomerByUserId(@PathVariable String userId) throws StripeException {
        String email = userId.hashCode() + "@meetmegroup.com";
        CustomerCollection list = Customer.list(CustomerListParams.builder().setEmail(email).build());
        List<Customer> data = list.getData();

        if (CollectionUtils.isEmpty(data)) {
            throw new RuntimeException("Not found!");
        }

        return data.get(0);
    }

    @PostMapping("/customers")
    public Customer createCustomer(@RequestBody
                                       Map<String, Object> values) throws StripeException {

        Object userId = values.get("userId");
        HashMap<Object, Object> metadata = new HashMap<>();
        metadata.put("internalUserID", userId);
        String email = userId.hashCode() + "@meetmegroup.com";

        HashMap<String, Object> params = new HashMap<>();
        params.put("metadata", metadata);
        params.put("email", email);
        Customer customer = Customer.create(params);
        return customer;
    }

    @GetMapping("/public-key")
    public StripeKeyResponse getPublicKey() {
        return StripeKeyResponse.builder()
            .publicKey("pk_test_51K6wNoJJ3Q3hxjxGxRzq3jon7Nm52pptZO3fNskGdpPuxa2dUxM8P5DsJLnrJTvcjcjecDi9ZEhxXf7dDcQzRXNz00UROIepRn")
            .build();
    }

    @PostMapping("/{userId}/payment-methods")
    public SetupIntent createPaymentMethod(@PathVariable String userId) throws StripeException {
//        String customerId = getStripeCustomer(userId);

        // The PaymentMethod will be stored to this Customer for later use.
        Customer customer = Customer.retrieve(userId);
        SetupIntentCreateParams params = new SetupIntentCreateParams.Builder()
            .setCustomer(customer.getId())
            .build();

        SetupIntent setupIntent = SetupIntent.create(params);

        return setupIntent;
    }


    @GetMapping("/{userId}/payment-methods")
    public PaymentMethodCollection getPaymentMethods(@PathVariable String userId) throws StripeException {
        // List the customer's payment methods to find one to charge

//        String customerId = getStripeCustomer(userId);
        PaymentMethodListParams listParams = new PaymentMethodListParams.Builder()
            .setCustomer(userId)
            .setType(PaymentMethodListParams.Type.CARD)
            .build();

        return PaymentMethod.list(listParams);
    }

    @DeleteMapping("/{userId}/payment-methods")
    public void deletePaymentMethods() throws StripeException {
        String customerId = getStripeCustomer("sads");
        PaymentMethodListParams listParams = new PaymentMethodListParams.Builder()
            .setCustomer(customerId)
            .setType(PaymentMethodListParams.Type.CARD)
            .build();

        PaymentMethodCollection methods = PaymentMethod.list(listParams);
        List<PaymentMethod> data = methods.getData();
        for (PaymentMethod datum : data) {
            datum.detach();
        }
    }

    @PostMapping("/{customerId}/create-payment")
    public PaymentIntent createPayment(@PathVariable String customerId, @RequestBody CreatePaymentRequest createPaymentRequest) throws StripeException {

        PaymentIntentCreateParams createParams = PaymentIntentCreateParams.builder()
            .setAmount(10 * 100L) //todo:  calculate based on products ..
            .setCurrency("usd")
            .setConfirm(createPaymentRequest.getIsConfirmed())
            .setPaymentMethod(createPaymentRequest.getPaymentMethodId())
            .addPaymentMethodType("card")
            .addPaymentMethodType("wechat_pay")
            .setCustomer(customerId)
            .setSetupFutureUsage(PaymentIntentCreateParams.SetupFutureUsage.OFF_SESSION)
            .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.MANUAL)
            .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL)
            .build();

        return PaymentIntent.create(createParams);
    }

    @PostMapping("/capture-payment")
    public void capture(@RequestBody ConfirmPaymentRequest request) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(request.getPaymentIntentId());
        paymentIntent.capture();
    }

    @PutMapping("/payments/{id}")
    public PaymentIntent confirmPayment(@PathVariable String id) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(id);
        return paymentIntent.confirm();
    }

    @DeleteMapping("/payments/{id}")
    public PaymentIntent cancelPayment(@PathVariable String id) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(id);
        return paymentIntent.cancel();
    }

    @GetMapping("/payment/{paymentIntentId}")
    public PaymentIntent getPaymentIntentInfo(@PathVariable String paymentIntentId) throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId);
    }

    @PostMapping("/checkout/create-session")
    public ModelAndView createSession(HttpServletRequest request, HttpServletResponse response) throws StripeException {
        //        Object productId = body.get("productId");
        String productId = request.getParameter("productID");
        String YOUR_DOMAIN = "http://localhost:3000";
        SessionCreateParams params =
            SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(YOUR_DOMAIN + "/checkout-success")
                .setCancelUrl(YOUR_DOMAIN + "/checkout-failed")
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName("Test product")
                                .build()
                            )
                            .setCurrency("USD")
                            .setUnitAmount(1 * 100L)
                            .build()
                        )
                        // Provide the exact Price ID (for example, pr_1234) of the product you want to sell
                        .build())
                .build();
        Session session = Session.create(params);

        //        RedirectView redirectView = new RedirectView();
        //        redirectView.setUrl(session.getUrl());

        ModelAndView model = new ModelAndView();
        model.setViewName("redirect:" + session.getUrl());
        return model;
    }

    @PostMapping("/checkout/webhook")
    @ResponseBody
    public ResponseEntity<?> handleWebhooks(@RequestBody
                                                Map<String, Object> body, HttpServletResponse response) {
        Object type = body.get("type");
        System.out.println(type);
        if (type.toString().equals("payment_intent.succeeded")) {
            System.out.println("bad request ----- ");
            response.setStatus(400);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        }

        response.setStatus(200);
        return ResponseEntity.status(HttpStatus.OK).body(false);
        //        throw new RuntimeException();
    }

    private String getStripeCustomer(String userId) {
        return "cus_Kmr2PCZVPE1qgb";
    }
}
