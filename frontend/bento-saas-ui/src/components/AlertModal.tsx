// AlertModal.tsx  (your current modal becomes this "extension")
import { MdErrorOutline } from "react-icons/md";
import { IoIosInformationCircleOutline } from "react-icons/io";
import BaseModal from "./BaseModal";

type AlertModalProps = {
  open: boolean;
  onClose: () => void;
  msg: string;
  isSuccess: boolean;
};

export default function AlertModal({
  open,
  onClose,
  msg,
  isSuccess,
}: AlertModalProps) {
  return (
    <BaseModal open={open} onClose={onClose}>
      {({ close }) => (
        <div className="flex flex-col items-center justify-center text-center gap-4 px-8 py-6">
          {isSuccess ? (
            <IoIosInformationCircleOutline className="text-6xl text-green-600" />
          ) : (
            <MdErrorOutline className="text-6xl text-red-600" />
          )}

          <p
            className={`text-md font-medium ${isSuccess ? "text-green-600" : "text-red-600"}`}
          >
            {msg}
          </p>

          <div className="w-3/4">
            <button
              className="w-full text-sm text-stone-50 hover:text-slate-700 bg-emerald-500 hover:bg-emerald-600 px-4 py-1.5 rounded-md transition"
              onClick={close}
            >
              Close
            </button>
          </div>
        </div>
      )}
    </BaseModal>
  );
}
