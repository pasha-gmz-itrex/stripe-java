// This is a public sample test API key.
// To avoid exposing it, don't submit any personally identifiable information through requests with this API key.
// Sign in to see your own test API key embedded in code samples.
const stripe = Stripe("pk_test_51K6wNoJJ3Q3hxjxGxRzq3jon7Nm52pptZO3fNskGdpPuxa2dUxM8P5DsJLnrJTvcjcjecDi9ZEhxXf7dDcQzRXNz00UROIepRn");

initialize();

// Fetches a payment intent and captures the client secret
async function initialize() {
  const response = await fetch("/user1/create-payment-method", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ }),
  });
  const { clientSecret } = await response.json();

  const appearance = {
    theme: 'stripe',
  };
  const elements = stripe.elements({ appearance, clientSecret });

  const card = elements.create("card");
  card.mount("#payment-element");

  console.log(clientSecret);

  // Handle payment submission when user clicks the pay button.
  var button = document.getElementById("submit");
  button.addEventListener("click", function(event) {
    event.preventDefault();
    // setLoading(true);
    // var email = document.getElementById("email").value;

    stripe
      .confirmCardSetup(clientSecret, {
        payment_method: {
          card: card,
          // billing_details: { email: email }
        },
      })
      .then(function(result) {
        console.log(result);
        if (result.error) {
          console.log("failed")
          setLoading(false);
          const displayError = document.getElementById("payment-message");
          displayError.textContent = result.error.message;
        } else {
          console.log("success");
          // The PaymentMethod was successfully set up
          complete(stripe, clientSecret);
        }
      })
      .catch(function (error) {
        console.log("error");
        console.log(error);
      });
  });
}

// Show a spinner on payment submission
function setLoading(isLoading) {
  if (isLoading) {
    // Disable the button and show a spinner
    document.querySelector("#submit").disabled = true;
    document.querySelector("#spinner").classList.remove("hidden");
    document.querySelector("#button-text").classList.add("hidden");
  } else {
    document.querySelector("#submit").disabled = false;
    document.querySelector("#spinner").classList.add("hidden");
    document.querySelector("#button-text").classList.remove("hidden");
  }
}

async function complete(stripe, clientSecret) {
    const { setupIntent } = await stripe.retrieveSetupIntent(clientSecret);

    console.log("setupIntent");
    console.log(setupIntent);

    const messageContainer = document.querySelector("#payment-message");
    messageContainer.classList.remove("hidden");
    messageContainer.textContent = setupIntent.status;

    setTimeout(function () {
      messageContainer.classList.add("hidden");
      messageContainer.textContent  = "";
    }, 5000);
}
