import { useEffect, useState } from "react";
import type { UserResponseDTO } from "../lib/api/types";
import BaseModal from "./BaseModal";
import { IoIosCloseCircle } from "react-icons/io";
import PageLoading from "./PageLoading";
import formatToJapaneseDateTime from "../utilities/formatToJst";
import formatAddressJp from "../utilities/formatAddressJp";

type ProviderUserInfoModalProps = {
  open: boolean;
  onClose: () => void;
  fetchProviderInfo: () => Promise<UserResponseDTO>;
};

const ProviderUserInfoModal = ({
  open,
  onClose,
  fetchProviderInfo,
}: ProviderUserInfoModalProps) => {
  const [loading, setLoading] = useState(false);
  const [providerUser, setProviderUser] = useState<UserResponseDTO | null>(
    null,
  );

  useEffect(() => {
    if (!open) {
      setLoading(false);
      setProviderUser(null);
      return;
    }

    let cancelled = false;

    const run = async () => {
      setLoading(true);
      setProviderUser(null);
      try {
        const data = await fetchProviderInfo();
        if (!cancelled) setProviderUser(data);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    run();

    return () => {
      cancelled = true;
    };
  }, [open, fetchProviderInfo]);

  return (
    <BaseModal
      open={open}
      onClose={onClose}
      panelClassName="min-w-[420px] px-8 py-6"
    >
      {({ close }) => (
        <div className="m-10 max-w-sm text-center text-md text-gray-500 font-medium">
          USER PROFILE
          <div className="flex justify-end gap-2 pt-2">
            <button
              className="mb-1 rounded-full px-2 py-2 text-sm"
              onClick={close}
            >
              <IoIosCloseCircle className="text-xl text-red-500 hover:text-red-700" />
            </button>
          </div>
          {loading ? (
            <div className="text-sm text-slate-600">
              <PageLoading text="Loading provided user info..." />
            </div>
          ) : !providerUser ? (
            <div className="text-sm text-slate-600">
              No provider user info found.
            </div>
          ) : (
            <div className="rounded-lg border bg-white px-4 pb-10 pt-8 shadow-lg">
              <div className="relative mx-auto w-36 rounded-full">
                <span className="absolute right-0 m-3 h-3 w-3 rounded-full bg-green-500 ring-2 ring-green-300 ring-offset-2" />
                <img
                  className="mx-auto h-auto w-full rounded-full"
                  src={providerUser.imageUrl ?? ""}
                  alt="Provider"
                />
              </div>

              <h1 className="my-1 text-center text-xl font-bold leading-8 text-gray-900">
                {providerUser.firstName} {providerUser.lastName}
              </h1>

              <h3 className="text-semibold text-center font-lg leading-6 text-gray-600">
                {providerUser.description}
              </h3>

              <p className="text-center text-sm leading-6 text-gray-500 hover:text-gray-600">
                {formatAddressJp(providerUser.address)}
              </p>

              <ul className="mt-3 divide-y rounded bg-gray-100 px-3 py-2 text-gray-600 shadow-sm hover:text-gray-700 hover:shadow">
                <li className="flex items-center py-3 text-sm">
                  <span>Email</span>
                  <span className="ml-auto rounded-full px-2 py-1 text-sm font-medium">
                    {providerUser.email}
                  </span>
                </li>
                <li className="flex items-center py-3 text-sm">
                  <span>Phone</span>
                  <span className="ml-auto">{providerUser.phNo}</span>
                </li>
                <li className="flex items-center py-3 text-sm">
                  <span>Joined On</span>
                  <span className="ml-auto">
                    {providerUser.joinedOn
                      ? formatToJapaneseDateTime(providerUser.joinedOn)
                      : "-"}
                  </span>
                </li>
              </ul>
            </div>
          )}
        </div>
      )}
    </BaseModal>
  );
};

export default ProviderUserInfoModal;
