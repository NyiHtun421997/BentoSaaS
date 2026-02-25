import { PaymentElement } from "@stripe/react-stripe-js";
import { useState } from "react";
import { useStripe, useElements } from "@stripe/react-stripe-js";
import { useNavigate } from "react-router-dom";
import LoadingButton from "./LoadingButton";

export default function CheckoutForm() {
  const stripe = useStripe();
  const elements = useElements();

  const [message, setMessage] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);

  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!stripe || !elements) {
      // Stripe.js has not yet loaded.
      return;
    }

    setIsProcessing(true);

    const { error, paymentIntent } = await stripe.confirmPayment({
      elements,
      confirmParams: {
        return_url: `${window.location.origin}/completion`,
      },
      redirect: "if_required",
    });

    if (error?.type === "card_error" || error?.type === "validation_error") {
      setMessage(error.message ?? "An error occurred.");
      setIsProcessing(false);
    } else if (paymentIntent && paymentIntent.status === "succeeded") {
      setMessage("PAYMENT SUCCEEDED!");
      setSuccess(true);
      setIsProcessing(false);
      setTimeout(() => {
        navigate("/app/invoices");
      }, 1000);
    } else {
      setMessage("An unexpected error occured.");
      setIsProcessing(false);
    }
  };

  return (
    <div className="mx-auto w-full max-w-xl rounded-2xl border border-slate-200 bg-white p-6 shadow-sm md:p-8">
      <div className="mb-6">
        <h2 className="text-xl font-bold text-slate-900">Checkout</h2>
        <p className="mt-1 text-sm text-slate-500">
          Complete your payment securely with Stripe.
        </p>
      </div>

      <form id="payment-form" onSubmit={handleSubmit} className="space-y-5">
        <div
          id="payment-element"
          className="rounded-xl border border-slate-200 bg-slate-50 p-4"
        >
          <PaymentElement />
        </div>

        <LoadingButton
          type="submit"
          loading={isProcessing}
          disabled={!stripe || !elements}
          loadingText="Processing..."
          className="inline-flex w-full items-center justify-center rounded-xl bg-slate-900 px-4 py-3 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {" "}
          Pay
        </LoadingButton>

        {message ? (
          <div
            id="payment-message"
            className={`rounded-xl border border-slate-200 px-3 py-2 text-sm text-center ${success ? "bg-emerald-50 text-emerald-700" : "bg-red-50 text-red-700"}`}
          >
            {message}
          </div>
        ) : null}
      </form>
    </div>
  );
}
