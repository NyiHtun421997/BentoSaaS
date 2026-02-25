import { useEffect, useState } from "react";

import { Elements } from "@stripe/react-stripe-js";
import CheckoutForm from "./CheckoutForm";
import { loadStripe } from "@stripe/stripe-js";
import { apiPost } from "../lib/api/http";
import { useParams } from "react-router-dom";

function Payment() {
  const { invoiceId } = useParams<{ invoiceId: string }>();
  const stripePromise = loadStripe(import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY);
  const [clientSecret, setClientSecret] = useState("");

  useEffect(() => {
    const createPaymentIntent = async () => {
      const clientSecret = await apiPost<string>(
        `/invoice/api/v1/${invoiceId}/pay`,
        {},
        { "Idempotency-Key": crypto.randomUUID() },
      );
      setClientSecret(clientSecret);
    };
    createPaymentIntent();
  }, [invoiceId]);

  return (
    <>
      {clientSecret && stripePromise && (
        <Elements stripe={stripePromise} options={{ clientSecret }}>
          <CheckoutForm />
        </Elements>
      )}
    </>
  );
}

export default Payment;
